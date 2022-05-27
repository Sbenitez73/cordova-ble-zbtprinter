var API = require('./API');


module.exports = {

    discoverPrinters: function (success, failure) {
        if (success == null && failure == null) {
            return API.discoverPrinters();
        } else {
            API.discoverPrinters().then(success).catch(failure);
        }
    },
    
    print: function (MACAddress, str, success, failure) {
        if (success == null && failure == null) {
            return API.print(MACAddress, str);
        } else {
            API.print(MACAddress, str).then(success).catch(failure);
        }
    },

    printImage: function (base64, MACAddress,success, failure) {
        if (success == null && failure == null) {
            return API.printImage(base64, MACAddress);
        } else {
            API.printImage(base64, MACAddress).then(success).catch(failure);
        }
    },

    getPrinterName: function (MACAddress, success, failure) {
        if (success == null && failure == null) {
            return API.getPrinterName(MACAddress);
        } else {
            API.getPrinterName(MACAddress).then(success).catch(failure);
        }
    },

    getStatus: function (MACAddress, success, failure) {
        if (success == null && failure == null) {
            return API.getStatus(MACAddress);
        } else {
            API.getStatus(MACAddress).then(success).catch(failure);
        }
    },

    getZPLfromImage: function (base64String, addHeaderFooter, blacknessPercentage, success, failure) {
        if (success == null && failure == null) {
            return API.getZPLfromImage(base64String, addHeaderFooter, blacknessPercentage);
        } else {
            API.getZPLfromImage(base64String, addHeaderFooter, blacknessPercentage).then(success).catch(failure);
        }
    },

    convertImage: function (str, y, success, failure) {
        if (success == null && failure == null) {
            return API.convertImage(str, y);
        } else {
            API.convertImage(str, y).then(success).catch(failure);
        }
    },
}