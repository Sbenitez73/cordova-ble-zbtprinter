var exec = require('cordova/exec');

var PLUGIN = 'ZbtPrinter';


module.exports = {

    discoverPrinters: function () {
        return new Promise(function(resolve, reject) {
            exec(resolve, reject, PLUGIN, 'discoverPrinters', []);
        });
    },
    
    print: function (MACAddress, str) {
        return new Promise( function(resolve, reject) {
            exec(resolve, reject, PLUGIN, 'print', [MACAddress, str]);
        });
    },

    printImage: function(base64, MACAddress) {
        return new Promise(function(resolve,reject) {
            exec(resolve, reject, PLUGIN, 'printImage', [base64, MACAddress]);
        })
    },

    getPrinterName: function(MACAddress) {
        return new Promise(function(resolve,reject) {
            exec(resolve, reject, PLUGIN, 'getPrinterName', [MACAddress]);
        })
    },

    getStatus: function(MACAddress) {
        return new Promise(function(resolve,reject) {
            exec(resolve, reject, PLUGIN, 'getStatus', [MACAddress]);
        })
    },

    getZPLfromImage: function (base64String, addHeaderFooter, blacknessPercentage) {
        return new Promise(function(resolve,reject) {
            exec(resolve, reject, PLUGIN, 'getZPLfromImage', [base64String, addHeaderFooter, blacknessPercentage]);
        })
    },

    convertImage: function(str, y) {
        return new Promise(function(resolve,reject) {
            exec(resolve, reject, PLUGIN, 'convertImage', [str, y]);
        })
    }

}