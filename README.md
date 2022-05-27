#  `@sbenitez73/cordova-ble-zbtprinter`

[![npm version](https://img.shields.io/npm/v/@sbenitez73/cordova-ble-zbtprinter/picker.svg)](https://www.npmjs.com/package/@sbenitez73/cordova-ble-zbtprinter)
[![Build](https://github.com/react-native-picker/picker/workflows/Build/badge.svg)](https://github.com/react-native-picker/picker/actions) ![Supports Android, iOS, MacOS, and Windows](https://img.shields.io/badge/platforms-android%20|%20ios|%20macos|%20windows-lightgrey.svg) ![MIT License](https://img.shields.io/npm/l/@react-native-picker/picker.svg) [![Lean Core Extracted](https://img.shields.io/badge/Lean%20Core-Extracted-brightgreen.svg)](https://github.com/facebook/react-native/issues/23313)

# @sbenitez73/cordova-ble-zbtprinter
This plugin is built so that it can be used with Zebra thermal printers, through bluetooth, we can print images and whatever we want with Zebra's ZPL or CPCL programming language.


## Installation

`npm i @sbenitez73/cordova-ble-zbtprinter`


## Usage
Images can be printed on a Zebra printer in base64 format:

```js
var base64StringArray = [base64String1, base64String2];

cordova.plugins.zbtprinter.printImage(base64StringArray, MACAddress,
    function(success) { 
        alert("Print ok"); 
    }, function(fail) { 
        alert(fail); 
    }
);
```

You can send data in ZPL Zebra Programing Language:

```js
var printText = "^XA"
		+ "^FO20,20^A0N,25,25^FDThis is a ZPL test.^FS"
		+ "^XZ";

cordova.plugins.zbtprinter.print(MACAddress, printText,
    function(success) { 
        alert("Print ok"); 
    }, function(fail) { 
        alert(fail); 
    }
);
```

Discover nearby bluetooth Zebra printers:

```js
cordova.plugins.zbtprinter.discoverPrinters(
    function(MACAddress) { 
        alert("discovered a new printer: " + MACAddress); 
    }, function(fail) { 
        alert(fail); 
    }
);
```

You can get a status response from a connected Zebra printer using:
```js
cordova.plugins.zbtprinter.getStatus(address,
    function(success){
        alert("Zbtprinter status: " + success);
    }, function(fail) {
        alert("Zbtprinter error: " + fail);
    }
);
```

Retrieve the currently connected printer name:

```js
cordova.plugins.zbtprinter.getPrinterName(MACAddress,
    function(printerName) { 
        alert("Printer name: " + printerName); 
    }, function(fail) { 
        alert(fail); 
    }
);
```

## ZPL equivalent code from Base64
Get ZPL equivalent code from the base64 Image string :

```js

var base64Image     	= base64String;
var addHeaderFooter 	= false;    	//Want to add header/footer ZPL code or not
var blacknessPercentage = 50;		//Blackness Percentage

cordova.plugins.zbtprinter.getZPLfromImage(base64Image, addHeaderFooter, blacknessPercentage,
    function(zplCode) {
        alert("ZPL Code : " + zplCode);
    }, function(error) {
	alert(error);
    }
);
```

## ZPL - Zebra Programming Language
For more information about ZPL please see the  [PDF Official Manual](https://support.zebra.com/cpws/docs/zpl/zpl_manual.pdf)

