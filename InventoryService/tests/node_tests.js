process.env.NODE_ENV = 'test';
var assert = require('assert');
let sinon = require('sinon');
var sinonChai = require("sinon-chai");
var fs = require('fs');


let mocha = require('mocha');
let chai = require('chai');
let chaiHttp = require('chai-http');
let should = chai.should();
chai.use(chaiHttp);

var fsReadFile_stub = sinon.stub(fs, "readFile")
    .callsFake(function (file, encoding, cb) {
        console.log("fsReadFile_stub");
        cb(null, '{ "customApi": "inventoryservice" }');
    });
var server = require('../nodetron.js');

var inventoryDataAccess = require('../apis/inventoryDataAccess');

let singlePhone = {
    "phoneId": 12324433,
    "phoneDescription": "The Acme VOIP phone is your do-everything choice for voice over IP communications.  It features crystal-clear audio with all the bells and whistles at a value price point.",
    "phoneName": "Acme VOIP",
    "phonePrice": 699,
    "SKU": "XSV-DSDS123",
    "phoneImage": "phone1.jpg"
};


describe('Tron endpoint tests', () => {
    it('should return health status', (done) => {
        chai.request(server)
            .get('/')
            .end((err, res) => {
                res.should.have.status(200);
                res.text.should.equal("Nodetron is in good health");
                done();
            });
    });
});

describe('Inventory Endpoint Tests', () => {
    it('should return list of phones', (done) => {
        var getAllPhones_stub = sinon.stub(inventoryDataAccess, "getAllPhones")
            .callsFake(function (onSuccess) { onSuccess([]); });

        chai.request(server)
            .get('/api/v1/phones')
            .end((err, res) => {
                res.should.have.status(200);
                res.should.be.json;
                res.body.should.have.property('phones');
                done();
            });
    });

    it('should return a specific phone', (done) => {
        var getPhone_stub = sinon.stub(inventoryDataAccess, "getPhone")
            .callsFake(function (id, onSuccess) { onSuccess(singlePhone); });

        chai.request(server)
            .get('/api/v1/phones/12324433')
            .end((err, res) => {
                res.should.have.status(200);
                res.should.be.json;
                res.body.should.have.property('phones');
                let phone = res.body['phones'][0];
                phone.should.have.property('phoneId');
                phone.should.deep.equal(singlePhone);
                done();
            });
    });
});
