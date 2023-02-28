require 'sinatra/base'
require 'json'
require 'net/http'
require 'thread'

require "sinatra/activerecord"
require_relative 'custom_apis/user_login_api.rb'
require_relative 'custom_apis/purchase_cart_api.rb'
require_relative 'env_vars.rb'
require_relative './logger'

require_relative 'nrAgentFacade.rb'

class Rubytron < Sinatra::Base

  def self.env_vars(key)
    begin
      @env_vars ||= JSON.parse(File.read("env_vars.json"))
      @env_vars[key]
    rescue
      nil
    end
  end

  def k8s_custom_params()
    custom_params = {
      'clusterName': ENV['K8S_CLUSTER_NAME'],
      'k8s.clusterName': ENV['K8S_CLUSTER_NAME'],
      'namespace': ENV['K8S_POD_NAMESPACE'],
      'k8s.namespace': ENV['K8S_POD_NAMESPACE'],
      'podName': ENV['K8S_POD_NAME'],
      'k8s.podName': ENV['K8S_POD_NAME'],
      'containerName': ENV['K8S_CONTAINER_NAME'],
      'k8s.containerName': ENV['K8S_CONTAINER_NAME'],
      'nodeName': ENV['K8S_NODE_NAME'],
      'k8s.nodeName': ENV['K8S_NODE_NAME'],
      'deploymentName': ENV['K8S_DEPLOYMENT_NAME'],
      'k8s.deploymentName': ENV['K8S_DEPLOYMENT_NAME']
    }.compact
    custom(custom_params)
  end

  start_time_utc = Time.now.utc

  if ENV['K8S_CLUSTER_NAME'].nil?
    instance = NrAgentFacade.build_logger("application.log.json")
  else
    instance = NrAgentFacade.build_logger(STDOUT)
  end

  Logger.init(instance)

  register Sinatra::UserLoginApi if ENV['USERLOGIN'] == 'ENABLED'

  if Rubytron::env_vars('customApi') == 'fulfillmentservice'
    register Sinatra::PurchaseCartApi
  end

  register Sinatra::ActiveRecordExtension
  set :database_file, "config/database.yml"

  set :public_folder, 'public'
  set :bind, '0.0.0.0'

  helpers do

    def log
      @@mutex ||= Mutex.new
      @@mutex.synchronize do
        @@log ||= [{:start => Time.now}]
        @@log.shift while @@log.length > 50
      end
      @@log
    end

    def transaction(name)
      NrAgentFacade.set_transaction_name(name)
    end

    def custom(params)
      NrAgentFacade.add_custom_attributes(params)
    end

    def duration(seconds)
      sleep seconds unless seconds.nil?
    end

    def post(url, body, request_headers={})
      start = Time.now
      result = {:time => Time.now, :url => url, :request => body}
      begin
        uri = URI(url)
        http = Net::HTTP.new(uri.host, uri.port)
        req = Net::HTTP::Post.new(uri.path, 'Content-Type' => 'application/json')
        req.body = body.to_json
        res = http.request(req)
        response_body = JSON.parse(res.body)
      rescue JSON::ParserError
        response_body = response.body
      rescue Exception => e
        result[:response] = {:code => 500, :message => "error:#{e}"}
        NrAgentFacade.notice_error(e)
      end
      result[:duration] = Time.now - start
      return result
    end

    def sequence(requests)
      begin
        requests.each do |request|
          post request['url'], request['body']
        end
      rescue Exception => e
        NrAgentFacade.notice_error(e)
      end
    end

    def fire(requests)
      begin
        requests.each do |request|
          FAF.post request['url'], request['body'], {'Content-Type' => 'application/json'}
        end
      rescue Exception => e
        NrAgentFacade.notice_error(e)
      end
    end

    def map_transaction(error)
      begin
        case error
          when 1 || "JSON::ParserError"
            raise JSON::ParserError
          when 2 || "ActiveModel::MissingAttributeError"
            raise ActiveModel::MissingAttributeError
          when 3 || "Encoding::UndefinedConversionError"
            raise Encoding::UndefinedConversionError
          when 4 || "Hash::DisallowedType"
            raise Hash::DisallowedType
          when 5 || "Loop2loop::SyntaxError"
           raise Loop2loop::SyntaxError
          when 6 || "Getit::BadAlias"
            raise Getit::BadAlias
        end
      rescue Exception => e
        NrAgentFacade.notice_error(e)
      end
    end

    def respond status, options
      if status == '500'
        if message = options['httpErrorMessage']
          NrAgentFacade.notice_error(message)
        else
          raise "expected error"
        end
      elsif status == 'custom'
        if error_type = options['httpErrorMessage']
          NrAgentFacade.add_custom_attributes({ "error_type" => error_type })
          begin
            error = Object.const_get(error_type)
          raise error
          rescue Exception => e
            NrAgentFacade.notice_error(e)
          end
        end
      end
    end

    def datastore body
      mysql body['mysql'] if body.has_key? 'mysql'
    end

    def datastoreSegment body
      @agent_version = Rubytron::env_vars('agentVersion')
      txt = NrAgentFacade.tl_current()
      unless txt.nil?
        params = ()
        segment =  NrAgentFacade.make_datastore_segment(body['product'], body['operation'], body['collection'], body['host'], body['port_path_or_id'], body['database_name'])
        segment.transaction = txt
        segment.record_on_finish = true
        segment.notice_sql body['sql_statement']
        segment.start
        sleep body['duration']
        segment.finish
      end
    end
  end

  post '/api' do
    begin
      request.body.rewind
      body = JSON.parse request.body.read
      log << {:time => Time.now, :body => body}
      k8s_custom_params()
      datastore body['datastoreRequest']        if body.has_key? 'datastoreRequest'
      datastoreSegment body['datastoreSegment'] if body.has_key? 'datastoreSegment'
      transaction body['transactionName']       if body.has_key? 'transactionName'
      custom body['customParameters']           if body.has_key? 'customParameters'
      duration body['transactionDuration']      if body.has_key? 'transactionDuration'
      fire body['fire']                         if body.has_key? 'fire'
      sequence body['sync']                     if body.has_key? 'sync'
      send_log body['logMessage']               if body.has_key? 'logMessage'
      respond body['httpResponseCode'],body     if body.has_key? 'httpResponseCode'
      process body['cpu'],body                  if body.has_key? 'cpu'
      map_transaction body['raiseError']        if body.has_key? 'raiseError'
        content_type :json
        {:status => "ok"}.to_json

    rescue Exception => e
      NrAgentFacade.notice_error(e)
    end
  end

  def send_log message
    if !message.nil?
      Logger.info(message)
    end
  end

  def mysql body
    body.each do |database_name, queries|
      begin
        pool = ActiveRecord::Base.establish_connection(database_name.to_sym)
        db = pool.checkout
        queries.each do |query|
          begin
            db.execute query
          rescue Exception => e
            # TODO - send up to staging account
            NrAgentFacade.add_custom_attributes({ failedDatabaseName: database_name, failedQuery: query, exceptionMessage: e.message })
          end
        end
        NrAgentFacade.add_custom_attributes({ failedDatabaseName: database_name, exceptionMessage: "Everything fine" })
      rescue Exception => e
        NrAgentFacade.add_custom_attributes({ failedDatabaseName: database_name, exceptionMessage: e.message })
      ensure
        pool.checkin db unless pool.nil?
      end
    end
  end

  get '/' do
    Logger.info('/ GET')
    NrAgentFacade.ignore_transaction()
    "#{ENV['NEW_RELIC_APP_NAME']} (rubytron) is feeling good.<br/>Started at #{start_time_utc.to_s}."
  end

  get '/process' do
    Logger.info('/process GET')
    process(500, {})
  end

  def process(spike, body)
    if spike > 0
      randomizer = Random.new
      maxMillisecond = spike +randomizer.rand(spike*2)
      now = Time.now
      untilTime = now +maxMillisecond/1000.0
      count = 0
      threadCount = 0
      begin
        while now < untilTime
          count += 1
          for concurrentIndex in 0..9
            threadCount += 1
            fork { Calculate(50) }
          end
          Process.waitall
          now = Time.now
        end
        return "Process completed on #{count} iterations (threadCount:#{threadCount}) for #{maxMillisecond}ms."
      rescue Exception => e
        result = "Process error " +e.to_s
        return result
      end
    end
  end

  def Calculate(max)
    base = 10000
    max += base
    for index in base..max
      calculatedValue = Math.sin(index * Math::PI)
      calculatedValue += Time.now.to_i
      result = Math.log10(calculatedValue) ** (Time.now.day+10)
    end
  end

  get '/log' do
    Logger.info('/log GET')
    "<h3>Rubytron #{ENV['NEW_RELIC_APP_NAME']}</h3>" +
    log.map{|e|e.inspect}.join('<hr>').gsub(/http:\/\/(.*?:\d+)/,'<a href="http://\1/log">\1</a>')
  end

  get '/log.json' do
    Logger.info('/log.json GET')
    headers 'Access-Control-Allow-Origin' => "*"
    content_type :json
    story = log.map do |e|
      if e[:body] && e[:body]['payloadId']
        payloadId = e[:body]['payloadId']
      elsif e['payload'] && e['payload']['payloadId']
        payloadId = e['payload']['payloadId']
      else
        payloadId = rand(10000000).to_s
      end
      {:type => 'payload', :payload => e, :id => payloadId}
    end
    {:title => "Log (#{ENV['NEW_RELIC_APP_NAME']})", :story => story}.to_json
  end

end

at_exit {NrAgentFacade.terminate()}
