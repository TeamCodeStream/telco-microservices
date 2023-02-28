let mocha = require('mocha');
let chai = require('chai');
var expect = require('chai').expect;
let sinon = require('sinon');
var sinonChai = require("sinon-chai");
var sinonTestFactory = require('sinon-test');
var sinonTest = sinonTestFactory(sinon);
chai.use(sinonChai);

var inventoryDataAccess = require('../../apis/inventoryDataAccess');

describe('inventoryDataAccess', () => {
  describe('getPhone', () => {
    it('should construct valid query and call onSuccess with phone', sinonTest(function () {
      // arrange
      var id = 99;
      var queryResult = { id: 1 };
      var onSuccess = sinon.spy();
      var executeDBQuery_stub = this.stub(inventoryDataAccess, "executeDBQuery").callsFake(function (q, cb) { cb([queryResult]); });

      // act
      inventoryDataAccess.getPhone(id, onSuccess);

      // assert
      executeDBQuery_stub.should.have.been.calledWith("select * from inventory_table where phoneId=" + id + ";");
      onSuccess.should.have.been.calledWith(queryResult);
    }));
  });

  describe('getAllPhones', () => {
    it('should construct valid query and call onSuccess with phone', sinonTest(function () {
      // arrange
      var queryResult = [{ phoneId: 1, active: 1 }];
      var onSuccess = sinon.spy();
      var executeDBQuery_stub = this.stub(inventoryDataAccess, "executeDBQuery").callsArgWith(1, queryResult);
      var getPhone_stub = this.stub(inventoryDataAccess, "getPhone").callsArgWith(1, queryResult[0]);


      // act
      inventoryDataAccess.getAllPhones(onSuccess);
      // assert
      executeDBQuery_stub.should.have.been.calledWith("select * from phones_table as pt1 inner join phones_table as pt2 on pt1.phone_id > 0;");

      onSuccess.should.have.been.calledWith(queryResult);
    }));
  });
});