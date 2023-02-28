var logger = require("../logger")

module.exports = function (app, configFile) {
  var _ = require('underscore');
  var newrelicFacade = require('../instrumentation/nodeagentfacade');
  var inventoryDataAccess = require('./inventoryDataAccess');

  inventoryDataAccess.setPoolCluster(initializeDatabaseConnection());

  // Named phoneHelper to look more realistic in trace
  var phoneHelper = function () {
    var rand = Math.floor((Math.random() * 4500) + 1);
    switch (true) {
      case (rand == 5):
        try { eval("vat since = 0;"); }
        catch (e) { newrelicFacade.noticeError(e); }
        break;
      case (rand > 10 && rand < 30):
        try { fetch(); }
        catch (e) { newrelicFacade.noticeError(e); }
        break;
      case (rand > 50 && rand < 60):
        try { 2.34.toFixed(-100); }
        catch (e) { newrelicFacade.noticeError(e); }
        break;
      case (rand > 200 && rand < 210):
        try { throw new Error('undefined problem'); }
        catch (e) { newrelicFacade.noticeError(e); }
        break;
      case (rand == 1):
        try { throw new Error('internal server error'); }
        catch (e) { newrelicFacade.noticeError(e); }
        break;
    }
  }



  // Named phonesHelper to look more realistic in trace
  var phonesHelper = function () {
    var rand = Math.floor((Math.random() * 4000) + 1);
    switch (true) {
      case (rand == 5):
        try { var list = Array(Number.MAX_VALUE); }
        catch (e) { newrelicFacade.noticeError(e); }
        break;
      case (rand > 10 && rand < 15):
        try { parseFloat('FF2'); }
        catch (e) { newrelicFacade.noticeError(e); }
        break;
      case (rand > 50 && rand < 60):
        try { decodeURI("%%%"); }
        catch (e) { newrelicFacade.noticeError(e); }
        break;
      case (rand > 200 && rand < 205):
        try { throw new Error(403); }
        catch (e) { newrelicFacade.noticeError(e); }
        break;
      case (rand == 1):
        try { null.f(); }
        catch (e) { newrelicFacade.noticeError(e); }
        break;
    }
  }

  // We send the response object regardless of error condition.  TODO - define ultimate UI experience and adjust if needed
  app.get('/api/v1/phones/:id', function (req, res) {
    var id = req.params.id;
    logger.info(`/api/v1/phones/${id}`, 'get')

    inventoryDataAccess.getPhone(id, function onSuccess(phone) {
      var responseObj = { "phones": [phone] };
      res.send(responseObj);
    });
    addUsernameUserid(req);
    phoneHelper();
  });


  app.get('/api/v1/phones', function (req, res) {
    logger.info(`/api/v1/phones`, 'get')
    inventoryDataAccess.getAllPhones(function (phones) {
      var responseObj = { "phones": phones };
      res.send(responseObj);
    });
    addUsernameUserid(req);
    phonesHelper();
  });

  function addUsernameUserid(req){
    username = req.get('X-TELCO-USERNAME');
    if (username) {
      newrelicFacade.addCustomParameter('username', username);
    }
    userid = req.get('X-TELCO-USERID');
    if (userid) {
        newrelicFacade.addCustomParameter('userid', userid);
    }
  }

  function initializeDatabaseConnection() {
    var mysql = require('mysql');
    var poolCluster = mysql.createPoolCluster();

    var DATABASE_CONFIG = {
      host: configFile["inventoryDB_URL"],
      port: "3306",
      database: "inventoryDB",
      user: "inventory_user",
      password: "123"
    };
    poolCluster.add('DB_CONNECTION', DATABASE_CONFIG); // add a named configuration

    return poolCluster;
  }
}