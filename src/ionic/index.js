var plugin = function () {
    return window.ZbtPrinter;
}

var ZbtPrinter = /** @class */ (function () {
    function ZbtPrinter() {
    }

    ZbtPrinter.discoverPrinters = function () {
        var zbtPinter = plugin();
        return zbtPinter.discoverPrinters.apply(zbtPinter, arguments);
    };

    ZbtPrinter.print = function () {
        var zbtPinter = plugin();
        return zbtPinter.print.apply(zbtPinter, arguments);
    };

    ZbtPrinter.printImage = function () {
        var zbtPinter = plugin();
        return zbtPinter.printImage.apply(zbtPinter, arguments);
    };

    ZbtPrinter.getPrinterName = function () {
        var zbtPinter = plugin();
        return zbtPinter.getPrinterName.apply(zbtPinter, arguments);
    };

    ZbtPrinter.getStatus = function () {
        var zbtPinter = plugin();
        return zbtPinter.getStatus.apply(zbtPinter, arguments);
    };

    ZbtPrinter.getZPLfromImage = function () {
        var zbtPinter = plugin();
        return zbtPinter.getZPLfromImage.apply(zbtPinter, arguments);
    };

    ZbtPrinter.convertImage = function () {
        var zbtPinter = plugin();
        return zbtPinter.convertImage.apply(zbtPinter, arguments);
    };

    return ZbtPrinter;
}());

export default ZbtPrinter;