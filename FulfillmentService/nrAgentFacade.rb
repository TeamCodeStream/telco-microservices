require_relative "./logger"
require "newrelic_rpm"

class NrAgentFacade

  def self.ignore_transaction()
    return NewRelic::Agent.ignore_transaction()
  end

  def self.set_transaction_name(name)
    return NewRelic::Agent.set_transaction_name(name)
  end

  def self.add_custom_attributes(params)
    return NewRelic::Agent.add_custom_attributes(params)
  end

  def self.notice_error(message)
    Logger.error(message)
    return NewRelic::Agent.notice_error(message)
  end

  def self.tl_current()
    return NewRelic::Agent::Transaction.tl_current
  end

  def self.make_datastore_segment(product, operation, collection, host, port_path_or_id, database_name)
    return NewRelic::Agent::Transaction::DatastoreSegment.new(product, operation, collection, host, port_path_or_id, database_name)
  end

  def self.build_logger(log_location = "application.log.json")
    return NewRelic::Agent::Logging::DecoratingLogger.new(log_location)
  end

  def self.terminate()
    NewRelic::Agent.shutdown()
  end
end
