class NrAgentFacadeEmpty

  def self.ignore_transaction()
    return nil
  end

  def self.set_transaction_name(name)
    return nil
  end

  def self.add_custom_attributes(params)
    return nil
  end

  def self.notice_error(message)
      return nil
  end

  def self.tl_current()
    return nil
  end

  def self.make_datastore_segment(product, operation, collection, host, port_path_or_id, database_name)
    return nil
  end

  def self.build_logger()
    return nil
  end

  def self.terminate()
  end

end