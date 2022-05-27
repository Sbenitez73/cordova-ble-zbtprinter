declare module '@sbenitez73/cordova-ble-zbtprinter' {
    export class ZbtPrinter {

        static discoverPrinters( success?: Function, failure?: Function ): Promise<any>;

        static print( MACAddress: string, str: string, success?: Function, failure?: Function ): Promise<any>;

        static printImage( base64: string, MACAddress: string, success?: Function, failure?: Function ): Promise<any>;

        static getPrinterName( MACAddress: string, success?: Function, failure?: Function ): Promise<any>;

        static getStatus( MACAddress: string, success?: Function, failure?: Function ): Promise<any>;

        static getZPLfromImage( base64String: string, blacknessPercentage: number, addHeaderFooter?: boolean, success?: Function, failure?: Function ): Promise<any>;

        static convertImage( str: string, y: number, success?: Function, failure?: Function ): Promise<any>;

    }

    export default ZbtPrinter;
}