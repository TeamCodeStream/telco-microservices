var newrelicFacade = require('./instrumentation/nodeagentfacade');
var logger = require("./logger")
var express = require('express')
var mysql = require('mysql')
var bodyParser = require('body-parser')
var fs = require('fs')
var tron_helpers = require('./tron_helpers');

logger.init()

var poolCluster = mysql.createPoolCluster()

var app = express()
app.use(bodyParser.json())
app.use(bodyParser.urlencoded({
  extended: false
}))

app.get('/', function (req, res) {
  logger.info('/', 'get')
  res.end("Nodetron is in good health");
})

app.post('/api', function (req, res) {
  logger.info('/', 'post')
  var payload = req.body
  var returnCode = 200
  var keys = Object.keys(payload)

  if (payload.hasOwnProperty('datastoreRequest')) {
    var datastoreRequest = payload['datastoreRequest']
    logger.info('datastoreRequest', datastoreRequest)
    tron_helpers.datastore(datastoreRequest)
  }

  if (payload.hasOwnProperty('transactionDuration')) {
    var transactionDuration = payload['transactionDuration']
    logger.info('transactionDuration', transactionDuration)
    tron_helpers.duration(transactionDuration)
  }

  if (payload.hasOwnProperty('sync')) {
    var sync = payload['sync']
    logger.info('sync', sync)
    tron_helpers.sync(sync)
  }

  if (payload.hasOwnProperty('httpResponseCode')) {
    logger.info('respond', payload['httpResponseCode'])
    returnCode = tron_helpers.responseCode(payload)
  }

  if (payload.hasOwnProperty('fire')) {
    var fire = payload['fire']
    logger.info('fire', fire)
    tron_helpers.fire(fire)
  }

  if (payload.hasOwnProperty('customParameters')) {
    var customParameters = payload['customParameters']
    logger.info('customParameters', customParameters)
    tron_helpers.custom(customParameters)
  }

  if (payload.hasOwnProperty('transactionName')) {
    var transactionName = payload['transactionName']
    logger.info('transactionName', transactionName)
    tron_helpers.transactionname(transactionName)
  }

  res.sendStatus(returnCode)
})


function configService() {
  var obj
  fs.readFile('env_vars.json', 'utf8', function (err, data) {
    if (err) throw err
    obj = JSON.parse(data)

    if (obj["customApi"] == "inventoryservice") {
      require('./apis/inventoryAPI.js')(app, obj)
    }
  })
}
configService()

var server = app.listen(8081, function () {
  var host = server.address().address
  var port = server.address().port
  logger.info(`Started NODETRON server on host ${host} and port: ${port}`)
})

module.exports = app
