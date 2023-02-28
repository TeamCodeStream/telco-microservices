var async = require('async');
var tron_helpers = require('../tron_helpers');
var logger = require("../logger")

module.exports = {
  setPoolCluster: function (poolCluster) {
    this.poolCluster = poolCluster;
  },

  getAllPhones: function (onSuccess) {
    var countDownToRunSlowQuery = 5;
    var shouldRunSlowQuery = false;
    if(Math.floor(Math.random() * Math.floor(100)) < 20){
      shouldRunSlowQuery = true;
      tron_helpers.custom({"slow": "slow20"});
    }else{
      tron_helpers.custom({"slow": "none"});
    }

    var query = "select * from phones_table as pt1 inner join phones_table as pt2 on pt1.phone_id > 0;"
    logger.info('getAllPhones', 'executeDBQuery', query)
    module.exports.executeDBQuery(query, function (rows) {
      var filteredRows = new Array();
      async.each(rows, function (row, callback) {

        // make the 5th DB call be the slow one
        var runSlowQuery = false;
        if(shouldRunSlowQuery && countDownToRunSlowQuery == 0){
          shouldRunSlowQuery = false;
          runSlowQuery = true;
        }
        countDownToRunSlowQuery = countDownToRunSlowQuery - 1;

        module.exports.getPhone(row.phone_id, function (phone) {
          if (phone.active) {
            filteredRows.push(phone);
          }
          callback(null);
        }, runSlowQuery);
      }, function (error) {
        if (error) {
          console.log("ERROR");
        }
        //Only return unique results from the join above
        var uniquePhones = module.exports.uniqueArray(filteredRows, (o1, o2) => o1.phoneId === o2.phoneId);
        onSuccess(uniquePhones);
      });
    });

  },


  getPhone: function (id, onSuccess, other) {
    var query = "select * from inventory_table where phoneId=" + id + ";";
    if(other == true){
      query = "select * from inventory_table where phoneId=" + id + " AND EXISTS(SELECT SLEEP(0.65) UNION SELECT 1);";
    }
    module.exports.executeDBQuery(query, function (item) {
      onSuccess(item[0]);
    });
  },

  uniqueArray: function uniqueBy(a, cond) {
    return a.filter((e, i) => a.findIndex(e2 => cond(e, e2)) === i);
  },

  executeDBQuery: function (query, callback) {

    this.poolCluster.getConnection('DB_CONNECTION', function (err, connection) {

      if (err) throw err;
      connection.query(query, function (err, rows, fields) {
        if (err) throw err;
        callback(rows);
        connection.release();
      });
    });
  }
}
