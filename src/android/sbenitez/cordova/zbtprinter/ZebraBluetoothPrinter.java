package sbenitez.cordova.zbtprinter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Looper;
import android.util.Base64;
import java.nio.charset.Charset;
import android.util.Log;
import sbenitez.cordova.zbtprinter.ZPLConverter;
import org.json.JSONObject;
import org.apache.cordova.PluginResult;
import com.zebra.sdk.comm.BluetoothConnectionInsecure;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import android.graphics.Color;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.lang.*;
import java.util.Set;
import com.zebra.android.discovery.*;
import com.zebra.sdk.comm.*;
import com.zebra.sdk.printer.*;

class ParseBitmap {
    private static final String TAG = "ParseBitmap";
    private String m_data;
    private Bitmap m_bmp;
    
    public ParseBitmap(Bitmap _bmp){
        try{
            m_bmp = _bmp;
            Log.d(TAG,"Height:"+Integer.toString(m_bmp.getHeight()));
            Log.d(TAG,"Widht:"+Integer.toString(m_bmp.getWidth()));
        }catch(Exception e){
            Log.d(TAG, e.getMessage());
        }
    }
    
    public String ExtractGraphicsDataForCPCL(int _xpos,int _ypos){
        m_data = "";
        int color = 0,bit = 0,currentValue = 0,redValue = 0, blueValue = 0, greenValue = 0;
        try{
            //Make sure the width is divisible by 8
            int loopWidth = 8 - (m_bmp.getWidth() % 8);
            if (loopWidth == 8) {
                loopWidth = m_bmp.getWidth();
            } else {
                loopWidth += m_bmp.getWidth();   
            }
                          
            m_data = "EG" + " " + 
            Integer.toString((loopWidth / 8)) + " " +
            Integer.toString(m_bmp.getHeight()) + " " +
            Integer.toString(_xpos) + " " +
            Integer.toString(_ypos) + " ";
            for (int y = 0; y < m_bmp.getHeight(); y++)
            {
                bit = 128;
                currentValue = 0;
                for (int x = 0; x < loopWidth; x++)
                {
                    int intensity = 0;

                    if (x < m_bmp.getWidth())
                    {
                        color = m_bmp.getPixel(x, y);
                       
                        redValue = Color.red(color);
                        blueValue = Color.blue(color);
                        greenValue = Color.green(color);
                       
                        intensity = 255 - ((redValue + greenValue + blueValue) / 3);
                    }
                    else
                        intensity = 0;
                   
                   
                    if (intensity >= 128)
                        currentValue |= bit;
                    bit = bit >> 1;
                    if (bit == 0)
                    {
                     String hex = Integer.toHexString(currentValue);
                     hex = LeftPad(hex);
                     m_data = m_data + hex.toUpperCase();
                        bit = 128;
                        currentValue = 0;
                     
                        /****
                        String dbg = "x,y" + "-"+ Integer.toString(x) + "," + Integer.toString(y) + "-" +
                          "Col:" + Integer.toString(color) + "-" +
                          "Red: " +  Integer.toString(redValue) + "-" +
                          "Blue: " +  Integer.toString(blueValue) + "-" +
                          "Green: " +  Integer.toString(greenValue) + "-" +
                          "Hex: " + hex;
                       
                        Log.d(TAG,dbg);
                        *****/
                      
                    }
                }//x
            }//y
            m_data = m_data + "\r\n";
                
        }catch(Exception e){
            m_data = e.getMessage();
            return m_data;   
        }

        return m_data;
    }
    
    private String LeftPad(String _num){
        String str = _num;
         
        if (_num.length() == 1) {
          str = "0" + _num;
        }
      
        return str;
    }
}


public class ZebraBluetoothPrinter extends CordovaPlugin implements DiscoveryHandler {

    private static final String LOG_TAG = "ZebraBluetoothPrinter";
    private CallbackContext callbackContext;
    private boolean printerFound;
    private Connection thePrinterConn;
    private PrinterStatus printerStatus;
    private ZebraPrinter printer;
    private Connection printerConnection;
    private final int MAX_PRINT_RETRIES = 1;

    public ZebraBluetoothPrinter() {

    }


    //    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        if (action.equals("printImage")) {
            try {
                JSONArray labels = args.getJSONArray(0);
                String MACAddress = args.getString(1);
                sendImage(labels, MACAddress);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return true;
        } else if (action.equals("print")) {
            try {
                String MACAddress = args.getString(0);
                String msg = args.getString(1);
                sendData(callbackContext, MACAddress, msg);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return true;
        } else if (action.equals("discoverPrinters")) {
            discoverPrinters();
            return true;
        } else if (action.equals("getPrinterName")) {
            String mac = args.getString(0);
            getPrinterName(mac);
            return true;
        } else if (action.equals("getStatus")) {
            try {
                String mac = args.getString(0);
                getPrinterStatus(callbackContext, mac);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return true;
        } else if(action.equals("getZPLfromImage")){
            try {
                String base64String = args.getString(0);
                boolean addHeaderFooter = args.getBoolean(1);
                int blacknessPercentage = args.getInt(2);
                getZPLfromImage(callbackContext, base64String, blacknessPercentage, addHeaderFooter);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
        }  else if (action.equals("convertImage")) {
            try {				
                String ruta = args.getString(0);
				int y = Integer.parseInt(args.getString(1));

                ConvertImage(callbackContext, ruta, y);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    void getZPLfromImage(final CallbackContext callbackContext, final String base64Image, final int blacknessPercentage, final boolean addHeaderFooter) throws Exception {

        String zplCode = "";

        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        ZebraImageAndroid zebraimage = new ZebraImageAndroid(decodedByte);
        String base64Dithered = new String(zebraimage.getDitheredB64EncodedPng(), "UTF-8");

        byte[] ditheredB64Png = Base64.decode(base64Dithered, Base64.DEFAULT);
        Bitmap ditheredPng = BitmapFactory.decodeByteArray(ditheredB64Png, 0, ditheredB64Png.length);
        
        if(ditheredPng.getHeight() > ditheredPng.getWidth())
            ditheredPng = Bitmap.createScaledBitmap(ditheredPng, 300, 540, true);
        
        ZPLConverter zplConveter = new ZPLConverter();
        zplConveter.setCompressHex(false);
        zplConveter.setBlacknessLimitPercentage(blacknessPercentage);

        //Bitmap grayBitmap = toGrayScale(decodedByte);

        try {
            zplCode = zplConveter.convertFromImage(ditheredPng, addHeaderFooter);
            callbackContext.success(zplCode);
        } catch (Exception e){
            callbackContext.error(e.getMessage());
        }

    }

    void getPrinterStatus(final CallbackContext callbackContext, final String mac) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    Connection thePrinterConn = new BluetoothConnectionInsecure(mac);

                    Looper.prepare();

                    thePrinterConn.open();

                    ZebraPrinter zPrinter = ZebraPrinterFactory.getInstance(thePrinterConn);
                    PrinterStatus printerStatus = zPrinter.getCurrentStatus();

                    if (printerStatus.isReadyToPrint){
                        callbackContext.success("Printer is ready for use");
                    }

                    else if(printerStatus.isPaused){
                        callbackContext.error("Printer is currently paused");
                    }

                    else if(printerStatus.isPaperOut){
                        callbackContext.error("Printer is out of paper");
                    }

                    else if(printerStatus.isHeadOpen){
                        callbackContext.error("Printer head is open");
                    }
                    
                    else{
                        callbackContext.error("Cannot print, unknown error");
                    }

                    thePrinterConn.close();

                    Looper.myLooper().quit();
                } catch (Exception e){
                    callbackContext.error(e.getMessage());
                }
            }
        }).start();

    }

    public ZebraPrinter connect(String mac, final CallbackContext callbackContext) {
        printerConnection = null;
        printerConnection = new BluetoothConnection(mac);

        try {
            printerConnection.open();
        } catch (ConnectionException e) {
            sleep(1000);
			callbackContext.error(e.getMessage());
            disconnect();
        }

        ZebraPrinter printer = null;

        if (printerConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(printerConnection);
                PrinterLanguage pl = printer.getPrinterControlLanguage();
            } catch (ConnectionException e) {
                printer = null;
                sleep(500);
				callbackContext.error(e.getMessage());
                disconnect();
            } catch (ZebraPrinterLanguageUnknownException e) {
                printer = null;
                sleep(500);
				callbackContext.error(e.getMessage());
                disconnect();
            }
        }

        return printer;
    }
    
    public void disconnect() {
        try {
            if (printerConnection != null) {
                printerConnection.close();
            }
        } catch (ConnectionException e) {
        } 
    }

    private void sendLabel(String msg,final CallbackContext callbackContext) {
        try {
            byte[] configLabel = getConfigLabel(msg);
            printerConnection.write(configLabel);
            sleep(1500);
        } catch (ConnectionException e) {
			callbackContext.error(e.getMessage());
        } finally {
            disconnect();
        }
    }

    private byte[] getConfigLabel(String msg) {
        PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();

        byte[] configLabel = null;
        if (printerLanguage == PrinterLanguage.ZPL) {
            configLabel = msg.getBytes();
        } else if (printerLanguage == PrinterLanguage.CPCL) {
            //String cpclConfigLabel = "! 0 200 200 406 1\r\n" + "ON-FEED IGNORE\r\n" + "BOX 20 20 380 380 8\r\n" + "T 0 6 137 177 TEST\r\n" + "PRINT\r\n";
            configLabel = msg.getBytes(Charset.forName("ISO-8859-1"));
        }
        return configLabel;
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /*
     * This will send data to be printed by the bluetooth printer
     */
    void sendData(final CallbackContext callbackContext, final String mac, final String msg) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {

                printer = connect(mac,callbackContext);

                if (printer != null) {
                    sendLabel(msg,callbackContext);
                    callbackContext.success(msg);
                } else {
                    disconnect();
                    //callbackContext.error("Error");
                }
            }
        }).start();
    }

    
    private void sendImage(final JSONArray labels, final String MACAddress) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                printLabels(labels, MACAddress);
            }
        }).start();
    }

    private void printLabels(JSONArray labels, String MACAddress) {
        try {

            boolean isConnected = openBluetoothConnection(MACAddress);

            if (isConnected) {
                initializePrinter();

                boolean isPrinterReady = getPrinterStatus(0);

                if (isPrinterReady) {

                    printLabel(labels);

                    //Sufficient waiting for the label to print before we start a new printer operation.
                    Thread.sleep(15000);

                    thePrinterConn.close();

                    callbackContext.success();
                } else {
                    Log.e(LOG_TAG, "Printer not ready");
                    callbackContext.error("Printer is not yet ready.");
                }

            }

        } catch (ConnectionException e) {
            Log.e(LOG_TAG, "Connection exception: " + e.getMessage());

            //The connection between the printer & the device has been lost.
            if (e.getMessage().toLowerCase().contains("broken pipe")) {
                callbackContext.error("The connection between the device and the printer has been lost. Please try again.");

            //No printer found via Bluetooth, -1 return so that new printers are searched for.
            } else if (e.getMessage().toLowerCase().contains("socket might closed")) {
                int SEARCH_NEW_PRINTERS = -1;
                callbackContext.error(SEARCH_NEW_PRINTERS);
            } else {
                callbackContext.error("Unknown printer error occurred. Restart the printer and try again please.");
            }

        } catch (ZebraPrinterLanguageUnknownException e) {
            Log.e(LOG_TAG, "ZebraPrinterLanguageUnknown exception: " + e.getMessage());
            callbackContext.error("Unknown printer error occurred. Restart the printer and try again please.");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void initializePrinter() throws ConnectionException, ZebraPrinterLanguageUnknownException {
        Log.d(LOG_TAG, "Initializing printer...");
        printer = ZebraPrinterFactory.getInstance(thePrinterConn);
        String printerLanguage = SGD.GET("device.languages", thePrinterConn);
        if (!printerLanguage.contains("zpl")) {
            SGD.SET("device.languages", "hybrid_xml_zpl", thePrinterConn);
            Log.d(LOG_TAG, "printer language set...");
        }
    }

    private boolean openBluetoothConnection(String MACAddress) throws ConnectionException {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter.isEnabled()) {
            Log.d(LOG_TAG, "Creating a bluetooth-connection for mac-address " + MACAddress);

            thePrinterConn = new BluetoothConnectionInsecure(MACAddress);

            Log.d(LOG_TAG, "Opening connection...");
            thePrinterConn.open();
            Log.d(LOG_TAG, "connection successfully opened...");

            return true;
        } else {
            Log.d(LOG_TAG, "Bluetooth is disabled...");
            callbackContext.error("Bluetooth is not on.");
        }

        return false;
    }

    public static Bitmap toGrayScale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap grayScale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(grayScale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return grayScale;
    }

    private void printLabel(JSONArray labels) throws Exception {
        ZebraPrinterLinkOs zebraPrinterLinkOs = ZebraPrinterFactory.createLinkOsPrinter(printer);

        for (int i = labels.length() - 1; i >= 0; i--) {
            String base64Image = labels.get(i).toString();
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ZebraImageAndroid zebraimage = new ZebraImageAndroid(decodedByte);

            //Lengte van het label eerst instellen om te kleine of te grote afdruk te voorkomen
            if (zebraPrinterLinkOs != null && i == labels.length() - 1) {
                setLabelLength(zebraimage);
            }

            if (zebraPrinterLinkOs != null) {
                printer.printImage(zebraimage, 150, 0, zebraimage.getWidth(), zebraimage.getHeight(), false);
            } else {
                Log.d(LOG_TAG, "Storing label on printer...");
                printer.storeImage("wgkimage.pcx", zebraimage, -1, -1);
                printImageTheOldWay(zebraimage);
            }
        }

    }

    private void printImageTheOldWay(ZebraImageAndroid zebraimage) throws Exception {

        Log.d(LOG_TAG, "Printing image...");

        String cpcl = "! 0 200 200 ";
        cpcl += zebraimage.getHeight();
        cpcl += " 1\r\n";
        cpcl += "PW 750\r\nTONE 0\r\nSPEED 6\r\nSETFF 203 5\r\nON - FEED FEED\r\nAUTO - PACE\r\nJOURNAL\r\n";
        cpcl += "PCX 150 0 !<wgkimage.pcx\r\n";
        cpcl += "FORM\r\n";
        cpcl += "PRINT\r\n";
        thePrinterConn.write(cpcl.getBytes());

    }

    private boolean getPrinterStatus(int retryAttempt) throws Exception {
        try {
            printerStatus = printer.getCurrentStatus();

            if (printerStatus.isReadyToPrint) {
                Log.d(LOG_TAG, "Printer is ready to print...");
                return true;
            } else {
                if (printerStatus.isPaused) {
                    throw new Exception("Printer is paused. Please activate it first.");
                } else if (printerStatus.isHeadOpen) {
                    throw new Exception("Printer is open. Please close it first.");
                } else if (printerStatus.isPaperOut) {
                    throw new Exception("Please complete the labels first.");
                } else {
                    throw new Exception("Could not get the printer status. Please try again. " +
                        "If this problem persists, restart the printer.");
                }
            }
        } catch (ConnectionException e) {
            if (retryAttempt < MAX_PRINT_RETRIES) {
                Thread.sleep(5000);
                return getPrinterStatus(++retryAttempt);
            } else {
               throw new Exception("Could not get the printer status. Please try again. " +
                        "If this problem persists, restart the printer.");
            }
        }

    }

    /**
     * Use the Zebra Android SDK to determine the length if the printer supports LINK-OS
     *
     * @param zebraimage
     * @throws Exception
     */
    private void setLabelLength(ZebraImageAndroid zebraimage) throws Exception {
        ZebraPrinterLinkOs zebraPrinterLinkOs = ZebraPrinterFactory.createLinkOsPrinter(printer);

        if (zebraPrinterLinkOs != null) {
            String currentLabelLength = zebraPrinterLinkOs.getSettingValue("zpl.label_length");
            if (!currentLabelLength.equals(String.valueOf(zebraimage.getHeight()))) {
                zebraPrinterLinkOs.setSetting("zpl.label_length", zebraimage.getHeight() + "");
            }
        }
    }

    private void discoverPrinters() {
        printerFound = false;

        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {

                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (bluetoothAdapter.isEnabled()) {
                        Log.d(LOG_TAG, "Searching for printers...");
                        BluetoothDiscoverer.findPrinters(cordova.getActivity().getApplicationContext(), ZebraBluetoothPrinter.this);
                    } else {
                        Log.d(LOG_TAG, "Bluetooth is disabled...");
                        callbackContext.error("Bluetooth is not on.");
                    }

                } catch (ConnectionException e) {
                    Log.e(LOG_TAG, "Connection exception: " + e.getMessage());
                    callbackContext.error(e.getMessage());
                } finally {
                    Looper.myLooper().quit();
                }
            }
        }).start();
    }

    private void getPrinterName(final String macAddress) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String printerName = searchPrinterNameForMacAddress(macAddress);

                if (printerName != null) {
                    Log.d(LOG_TAG, "Successfully found connected printer with name " + printerName);
                    callbackContext.success(printerName);
                } else {
                    callbackContext.error("No printer found.");
                }
            }
        }).start();
    }

    private String searchPrinterNameForMacAddress(String macAddress) {
        Log.d(LOG_TAG, "Connecting with printer " + macAddress + " over bluetooth...");

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                Log.d(LOG_TAG, "Paired device found: " + device.getName());
                if (device.getAddress().equalsIgnoreCase(macAddress)) {
                    return device.getName();
                }
            }
        }

        return null;
    }

    @Override
    public void foundPrinter(DiscoveredPrinter discoveredPrinter) {
        Log.d(LOG_TAG, "Printer found: " + discoveredPrinter.address);
        if (!printerFound) {
            printerFound = true;
            callbackContext.success(discoveredPrinter.address);
        }
    }


    @Override
    public void discoveryFinished() {
        Log.d(LOG_TAG, "Finished searching for printers...");
        if (!printerFound) {
            callbackContext.error("No printer found. If this problem persists, restart the printer.");
        }
    }

    @Override
    public void discoveryError(String s) {
        Log.e(LOG_TAG, "An error occurred while searching for printers. Message: " + s);
        callbackContext.error(s);
    }

    void ConvertImage(final CallbackContext callbackContext, final String ruta, final int y) throws IOException {
       
        try {
            
            BitmapFactory.Options options = new BitmapFactory.Options();		
            options.inScaled = false;
            options.inDither = false;

            FileInputStream in;
            BufferedInputStream buf;

            String str = Environment.getExternalStorageDirectory().getAbsoluteFile()+ ruta;
            in = new FileInputStream(str);
            buf = new BufferedInputStream(in);
            
            Bitmap m_bmp = BitmapFactory.decodeStream(buf);;	
            ParseBitmap m_BmpParser = new ParseBitmap(m_bmp);
            
            String imgstr = m_BmpParser.ExtractGraphicsDataForCPCL(0,y);   
            
            JSONObject objimg = new JSONObject();
            try {
              objimg.put("imageStr", imgstr);
              objimg.put("height", m_bmp.getHeight());
              
              PluginResult result = new PluginResult(PluginResult.Status.OK, objimg.toString());
              result.setKeepCallback(false);
              callbackContext.sendPluginResult(result);
              
            } catch (JSONException e) {
            }


        } catch (Exception e) {
            // Handle communications error here.
            callbackContext.error(e.getMessage());
        }

    }
}

