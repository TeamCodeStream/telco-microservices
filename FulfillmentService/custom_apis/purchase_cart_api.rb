require 'sinatra/base'
require 'date'
require 'json'
require 'digest'
require_relative '../nrAgentFacade.rb'
require_relative '../utils/rest_client.rb'
require_relative '../rubytron.rb'
require_relative '../logger'

module Sinatra
  module PurchaseCartApi

    def self.registered(app)
      app.post "/purchaseCart" do
        Logger.info('/purchaseCart POST')
        errored = false
        request.body.rewind
        body = JSON.parse request.body.read
        headers = PurchaseCartApi.get_headers(request)

        ## DEBUG
        shipping_error = request.env['HTTP_X_DEMOTRON_SHIPMENT_ERROR']
        NrAgentFacade.add_custom_attributes({"ruby_shipping_error": shipping_error})
        if !shipping_error.nil? && shipping_error == "error"
          NrAgentFacade.add_custom_attributes({"shipping_error": "ruby_yes"})
        else
          NrAgentFacade.add_custom_attributes({"shipping_error": "ruby_no"})
        end
        ## DEBUG

        PurchaseCartApi.validate_request(request)

        Logger.info('sending to credit card service')
        errored = PurchaseCartApi.send_to_creditcard_service(body, headers, errored)

        unless errored
          Logger.info('sending to billing service')
          errored = PurchaseCartApi.send_to_billing_service(headers, errored)
        end

        unless errored
          Logger.info('recording purchase')
          errored = PurchaseCartApi.send_purchase_to_logger(body,headers, errored)
        end

        ok = PurchaseCartApi.status_check(errored)
        if ok
          return {"success" => "true"}.to_json
        else
          return {"success" => "false"}.to_json
        end
      end
    end

    def self.validate_request(request)
      if request.env['HTTP_X_DEMOTRON_APM_TIMINGS']
        begin
          timings = JSON.parse(request.env['HTTP_X_DEMOTRON_APM_TIMINGS'])
          if timings['FulfillmentService']
            sleep timings['FulfillmentService'].to_f
          end
        rescue
        end
      end
    end

    def self.send_to_creditcard_service(body, headers, errored)
      url = Rubytron::env_vars "creditcard_URL"
      unless url.nil?
        phoneId = body['phoneId'] || 1
        quantity = body['quantity'] || 1
        # TODO
        # Need to either pass in the user info or access it via the login service
        # Need to either pass in the price or access it via the phone service
        credit_card_body = {
          "token": Digest::SHA256.hexdigest(rand.to_s),
          "first_name": "First Name",
          "last_name": "Last Name",
          "email": "email@example.com",
          "price": phoneId * quantity
        }

        begin
          response = RestClient.post(url+'/processPayment', credit_card_body, headers)
          res = ["Payment Authorized", "Authorization Delay", ]
        rescue Exception => e
          return true
        end

        return false
      end
    end

    def self.send_to_billing_service(headers, errored)
      url = Rubytron::env_vars "billing_URL"
      unless url.nil?
        billing_body = {
          "order_number": "0000001",
          "account_number": "1126",
          "phoneId": "321",
          "total": "300.00",
          "payment_method": "Credit Card"
        }
        begin
          response = RestClient.post(url+"/billing", billing_body, headers)
        rescue Exception => e
          if(errored == false)
            begin
              raise RuntimeError, "Process failure at shipping"
            rescue Exception => error
              NrAgentFacade.notice_error(error)
            end
          end
          return true
        end
      end
      return false
    end

    def self.send_purchase_to_logger(body, headers, errored)
      url = Rubytron::env_vars "lambda_URL"
      unless url.nil?
        purchase_hash = {
          'date': Time.now(),
          'coupon_id': rand(1000000000...3000000000),
          'id': rand(20000),
          'ship': body['shipping'],
          'taxes': body['tax'],
          'items': body['item_count'],
          'items_total': body['items_total'],
          'grand_total': body['grand_total']
        }
        begin
          RestClient.post(url, purchase_hash, headers)
        rescue Exception => e
          if(errored == false)
            NrAgentFacade.notice_error(e)
          end
          return true
        end
      end

      return false
    end

    def self.status_check(errored)
      if(errored == true)
        return false
      end
      randomNum = rand(1..1000)
      begin
        case randomNum
          when 1
            raise JSON::ParserError
          when 2
            raise ActiveModel::MissingAttributeError
          when 3
            raise Encoding::UndefinedConversionError
          when 4
            raise Hash::DisallowedType
          when 5
            raise Loop2loop::SyntaxError
          when 6
            raise Getit::BadAlias
          else
            return true
        end
      rescue Exception => e
        NrAgentFacade.notice_error(e)
      end
      return false
    end

    def self.get_headers(request)
      headers = {}

      shipping_error = request.env['HTTP_X_DEMOTRON_SHIPMENT_ERROR']
      if !shipping_error.nil? && shipping_error == "error"
        headers["X-DEMOTRON-SHIPMENT-ERROR"] = shipping_error
      end

      username = request.env['HTTP_X_TELCO_USERNAME']
      unless username.nil? || username.empty?
        NrAgentFacade.add_custom_attributes({ 'username': username })
        headers["X-TELCO-USERNAME"] = username
      end

      userid = request.env['HTTP_X_TELCO_USERID']
      unless userid.nil? || userid.empty?
        NrAgentFacade.add_custom_attributes({ 'userid': userid })
        headers["X-TELCO-USERID"] = userid
      end

      return headers
    end

  end

  register PurchaseCartApi
end
