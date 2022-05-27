#  `@sbenitez73/cordova-ble-zbtprinter`

[![npm version](https://img.shields.io/npm/v/@sbenitez73/cordova-ble-zbtprinter?label=VERSION&style=plastic)](https://www.npmjs.com/package/@sbenitez73/cordova-ble-zbtprinter)

This plugin is built so that it can be used with Zebra thermal printers, through bluetooth, we can print images and whatever we want with Zebra's ZPL or CPCL programming language.


## Installation

`npm i @sbenitez73/cordova-ble-zbtprinter`
## Usage

Import printer instance from `@sbenitez73/cordova-ble-zbtprinter`:

```javascript
import ZbtPrinter from '@sbenitez73/cordova-ble-zbtprinter';
```

You can send data in ZPL, CPCL Zebra Programing Language:

```js
let printText = "^XA^FO20,20^A0N,25,25^FDThis is a ZPL test.^FS^XZ";
ZbtPrinter.print( MACAddress, printText, 
    (success) => {
        alert("Printed successfully");
    }, ( error ) => {
        alert( error );
    }
);
```

Converts the image and returns it with the position indicated in the properties (only available with CPCL code):

```js
ZbtPrinter.convertImage(imageRoute, position,
    (printerName) => { 
        alert("Printer name: " + printerName); 
    }, (fail) => { 
        alert(fail); 
    }
);
```

Discover nearby bluetooth Zebra printers:

```js
ZbtPrinter.discoverPrinters(
    (MACAddress) => { 
        alert("discovered a new printer: " + MACAddress); 
    }, (error) => { 
        alert(error); 
    }
);
```

You can get a status response from a connected Zebra printer using:
```js
ZbtPrinter.getStatus(address,
    (success) => {
        alert("Zbtprinter status: " + success);
    },(error) => {
        alert("Zbtprinter error: " + failerror);
    }
);
```

Retrieve the currently connected printer name:

```js
ZbtPrinter.getPrinterName(MACAddress,
    (printerName) => { 
        alert("Printer name: " + printerName); 
    }, (fail) => { 
        alert(fail); 
    }
);
```

## ZPL equivalent code from Base64
Get ZPL equivalent code from the base64 Image string (return only ZPL code):

```js

let base64Image: string = base64String;
let addHeaderFooter: boolean = false;    	//Want to add header/footer ZPL code or not
let blacknessPercentage: number = 50;		//Blackness Percentage

ZbtPrinter.getZPLfromImage(base64Image, addHeaderFooter, blacknessPercentage,
    (zplCode) => {
        alert("ZPL Code : " + zplCode);
    }, (error) => {
	    alert(error);
    }
);
```

## ZPL - Zebra Programming Language
For more information about ZPL please see the  [PDF Official Manual](https://support.zebra.com/cpws/docs/zpl/zpl_manual.pdf)

## CPCL - Zebra Programming Language
For more information about CPCL please see the  [PDF Official Manual](https://support.zebra.com/cpws/docs/comtec/PROMAN-CPCL_RevY.pdf)
