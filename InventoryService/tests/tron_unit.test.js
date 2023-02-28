let mocha = require('mocha');
let chai = require('chai');
var expect = require('chai').expect;

var tron = require('../tron_helpers.js');

describe('Tron unit tests', () => {
    it('should return a valid responseCode', (done) => {
        // console.log(Object.getOwnPropertyNames(tron));
        expect(tron.responseCode({ "httpResponseCode": 200 })).to.equal(200);
        done();
    });
});