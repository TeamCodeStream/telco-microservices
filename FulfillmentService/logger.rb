require 'logger'

class Logger
  @@logger = nil

  def self.init(instance = nil)
    if (@@logger.nil?)
      instance ||= Logger.new(STDOUT)
      @@logger = instance
      @@logger.level = Logger::INFO
    end
  end

  def self.debug(message)
    unless @@logger.nil?
      @@logger.debug(message)
    end
  end

  def self.info(message)
    unless @@logger.nil?
      @@logger.info(message)
    end
  end

  def self.warn(message)
    unless @@logger.nil?
      @@logger.warn(message)
    end
  end

  def self.error(message)
    unless @@logger.nil?
      @@logger.error(message)
    end
  end

end
