try{
    var newrelic = require('newrelic'); // NR needs to be loaded on first line of main module to effectively instrument the app.  https://discuss.newrelic.com/t/apm-application-shows-warning-and-not-able-to-trace-application/12855
}
catch(error){
}

var logger = require("../logger")

module.exports = {
    noticeError: function  (error){
        logger.error(error)
        if (typeof newrelic !== "undefined"){
            newrelic.noticeError(error);
        }
    },
    addCustomParameter: function (key, value){
        if (typeof newrelic !== "undefined") {
            newrelic.addCustomAttributes(key, value);
        }
    },
    setTransactionName: function (payload){
        logger.info('setTransactionName', payload)
        if (typeof newrelic !== "undefined") {
            newrelic.setTransactionName(payload)
        }
    }
}

