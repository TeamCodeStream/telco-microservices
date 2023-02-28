
class EnvVars
  def self.env_vars(key)
    begin
      @env_vars ||= JSON.parse(File.read("env_vars.json"))
      @env_vars[key]
    rescue
      nil
    end
  end
end