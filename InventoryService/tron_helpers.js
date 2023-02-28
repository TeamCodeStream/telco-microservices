var newrelicFacade = require('./instrumentation/nodeagentfacade');
var async = require('async');
var http = require('http');
var url = require('url');

var tron_helpers = {
    // All purpose rest call given a URL, payload and the callback for event handling
    restcall: function (url, payload, callback) {
        var options = {
            port: url.port,
            host: url.hostname,
            path: url.path,
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        };

        var req = http.request(options,
            function onResponse(res) {
                if (res.statusCode !== 200) {
                    return callback(res.statusCode, null);
                }
                res.setEncoding('utf8');
                res.on('data', function (data) {
                    callback(res.statusCode, data);
                })
            });

        req.on('error', function (err) {
            console.log('the request errored: ', err);
        });

        req.write(JSON.stringify(payload));
        req.end();
    },

    datastore: function (payload) {

        if (payload.hasOwnProperty('mysql')) {
            mysqlPayload = payload['mysql'];
            //MediaRights DB
            if (mysqlPayload.hasOwnProperty('RIGHTS')) {
                var mediaRightsPayload = mysqlPayload['RIGHTS'];
                for (var i = 0; i < mediaRightsPayload.length; i++) {
                    var query = mediaRightsPayload[i];
                    var mediaRightsConnection = poolCluster.getConnection('RIGHTS', function (err, connection) {
                        if (err) throw err;
                        connection.query(query, function (err, rows, fields) {
                            if (err) throw err;
                            console.log(rows);
                            connection.release();
                        });
                    });
                }
            }

            //MediaCatalog DB
            if (mysqlPayload.hasOwnProperty('CATALOG')) {
                var mediaCatalogPayload = mysqlPayload['CATALOG'];
                for (var i = 0; i < mediaCatalogPayload.length; i++) {
                    var query = mediaCatalogPayload[i];
                    var mediaCatalogConnection = poolCluster.getConnection('CATALOG', function (err, connection) {
                        if (err) throw err;
                        connection.query(query, function (err, rows, fields) {
                            if (err) throw err;
                            connection.release();
                        });
                    });
                }
            }

            //PreRollView DB
            if (mysqlPayload.hasOwnProperty('PREROLL')) {
                var preRollViewPayload = mysqlPayload['PREROLL'];
                for (var i = 0; i < preRollViewPayload.length; i++) {
                    var query = preRollViewPayload[i];
                    var preRollViewConnection = poolCluster.getConnection('PREROLL', function (err, connection) {
                        if (err) throw err;
                        connection.query(query, function (err, rows, fields) {
                            if (err) throw err;
                            connection.release();
                        });
                    });
                }
            }

            //MediaUsers DB
            if (mysqlPayload.hasOwnProperty('USERS')) {
                var mediaUsersPayload = mysqlPayload['USERS'];
                for (var i = 0; i < mediaUsersPayload.length; i++) {
                    var query = mediaUsersPayload[i];
                    var mediaUsersConnection = poolCluster.getConnection('USERS', function (err, connection) {
                        if (err) throw err;
                        connection.query(query, function (err, rows, fields) {
                            if (err) throw err;
                            connection.release();
                        });
                    });
                }
            }
            if (mysqlPayload.hasOwnProperty('USERS')) {
                var mediaTrackerPayload = mysqlPayload['USERS'];
                for (var i = 0; i < mediaTrackerPayload.length; i++) {
                    var query = mediaUsersPayload[i];
                    var mediaTrackerConnection = poolCluster.getConnection('USERS', function (err, connection) {
                        if (err) throw err;
                        connection.query(query, function (err, rows, fields) {
                            if (err) throw err;
                            connection.release();
                        });
                    });
                }
            }

            if (mysqlPayload.hasOwnProperty('CLICKS')) {
                var adClicksPayload = mysqlPayload['CLICKS'];
                for (var i = 0; i < adClicksPayload.length; i++) {
                    var query = adClicksPayload[i];
                    var adClicksConnection = poolCluster.getConnection('CLICKS', function (err, connection) {
                        if (err) throw err;
                        connection.query(query, function (err, rows, fields) {
                            if (err) throw err;
                            connection.release();
                        });
                    });
                }
            }
        }
    },

    //Duration
    duration: function (payload) {
        var start = new Date().getTime();
        for (var i = 0; i < 1e7; i++) {
            if ((new Date().getTime() - start) > (payload * 1000)) {
                break;
            }
        }
    },

    //Transaction Name
    transactionname: function (payload) {
        newrelicFacade.setTransactionName(payload);
    },

    //Response Code
    responseCode: function (payload) {
        var responseCode = payload['httpResponseCode'];
        if (payload.hasOwnProperty('httpErrorMessage')) {
            newrelicFacade.setTransactionName(payload['httpErrorMessage']);
        }
        return responseCode;
    },

    //Sync
    sync: function (payload) {
        var keysArray = Object.keys(payload);
        for (var i = 0; i < keysArray.length; i++) {
            var key = keysArray[i]; // here is "name" of object property
            var value = payload[key]; // here get value "by name" as it expected with objects
            async.forEachOf(payload, this.requestfunction, function (err) {
                console.log("Finished!");
            });
        }
    },

    //This function is needed by the sync functionality.
    requestfunction: function (value, key, callback) {
        var requesturl = url.parse(value.url);
        var payload = value.body;
        module.exports.restcall(requesturl, payload, callback);
    },

    //Fire
    fire: function (payload) {
        var keys = Object.keys(payload);
        var firePayload;
        for (var i = 0; i < keys.length; i++) {
            firePayload = payload[keys[i]];
            this.restcall(url.parse(firePayload.url), firePayload.body, function (err) {
                //console.log("Finished!");
            });
        }
    },

    //Custom Parameters
    custom: function (payload) {
        var keys = Object.keys(payload);
        for (var i = 0; i < keys.length; i++) {
            newrelicFacade.addCustomParameter(keys[i], payload[keys[i]]);
        }
    }

}

module.exports = tron_helpers;