'use strict'

/**
 * New Relic agent configuration.
 *
 * See lib/config.defaults.js in the agent distribution for a more complete
 * description of configuration variables and their potential values.
 */
exports.config = {
  /**
   * Array of application names.
   */
  app_name: ["NEW_RELIC_APP_NAME"],
  /**
   * Your New Relic license key.
   */
  license_key: "NEW_RELIC_LICENSE_KEY",
  host: "NEW_RELIC_HOST_URL",
  api_host: "NEW_RELIC_API_URL",
  //labels: "NEW_RELIC_LABELS",
  
  //NR_DT_FLAG
  logging: {
    /**
     * Level at which to log. 'trace' is most useful to New Relic when diagnosing
     * issues with the agent, 'info' and higher will impose the least overhead on
     * production applications.
     */
    level: 'info'
  },
  transaction_tracer: {
    record_sql: "obfuscated"
  }
}
