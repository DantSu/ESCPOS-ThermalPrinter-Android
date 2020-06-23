package com.dantsu.thermalprinter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.tcp.TcpConnection;
import com.dantsu.escposprinter.connection.usb.UsbConnection;
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.dantsu.thermalprinter.async.AsyncBluetoothEscPosPrint;
import com.dantsu.thermalprinter.async.AsyncEscPosPrinter;
import com.dantsu.thermalprinter.async.AsyncTcpEscPosPrint;
import com.dantsu.thermalprinter.async.AsyncUsbEscPosPrint;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) this.findViewById(R.id.button_bluetooth);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printBluetooth();
            }
        });
        button = (Button) this.findViewById(R.id.button_usb);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printUsb();
            }
        });
        button = (Button) this.findViewById(R.id.button_tcp);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printTcp();
            }
        });
    }


    /*==============================================================================================
    ======================================BLUETOOTH PART============================================
    ==============================================================================================*/

    public static final int PERMISSION_BLUETOOTH = 1;

    public void printBluetooth() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, MainActivity.PERMISSION_BLUETOOTH);
        } else {
            // this.printIt(BluetoothPrintersConnections.selectFirstPaired());
            new AsyncBluetoothEscPosPrint(this).execute(this.getAsyncEscPosPrinter(null));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case MainActivity.PERMISSION_BLUETOOTH:
                    this.printBluetooth();
                    break;
            }
        }
    }


    /*==============================================================================================
    ===========================================USB PART=============================================
    ==============================================================================================*/

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MainActivity.ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbManager != null && usbDevice != null) {
                            // printIt(new UsbConnection(usbManager, usbDevice));
                            new AsyncUsbEscPosPrint(context)
                                    .execute(getAsyncEscPosPrinter(new UsbConnection(usbManager, usbDevice)));
                        }
                    }
                }
            }
        }
    };

    public void printUsb() {
        UsbConnection usbConnection = UsbPrintersConnections.selectFirstConnected(this);
        UsbManager usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);

        if (usbConnection == null || usbManager == null) {
            new AlertDialog.Builder(this)
                    .setTitle("USB Connection")
                    .setMessage("No USB printer found.")
                    .show();
            return;
        }

        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(MainActivity.ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_USB_PERMISSION);
        registerReceiver(this.usbReceiver, filter);
        usbManager.requestPermission(usbConnection.getDevice(), permissionIntent);
    }

    /*==============================================================================================
    =========================================TCP PART===============================================
    ==============================================================================================*/

    public void printTcp() {
        final EditText ipAddress = (EditText) this.findViewById(R.id.edittext_tcp_ip);
        final EditText portAddress = (EditText) this.findViewById(R.id.edittext_tcp_port);

        try {
            // this.printIt(new TcpConnection(ipAddress.getText().toString(), Integer.parseInt(portAddress.getText().toString())));
            new AsyncTcpEscPosPrint(this)
                    .execute(this.getAsyncEscPosPrinter(new TcpConnection(ipAddress.getText().toString(), Integer.parseInt(portAddress.getText().toString()))));
        } catch (NumberFormatException e) {
            new AlertDialog.Builder(this)
                    .setTitle("Invalid TCP port address")
                    .setMessage("Port field must be a number.")
                    .show();
            e.printStackTrace();
        }
    }

    /*==============================================================================================
    ===================================ESC/POS PRINTER PART=========================================
    ==============================================================================================*/


    /**
     * Synchronous printing
     */
    public void printIt(DeviceConnection printerConnection) {
        try {
            EscPosPrinter printer = new EscPosPrinter(printerConnection, 203, 48f, 32);
            printer
                    .printFormattedText(
                            "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.logo, DisplayMetrics.DENSITY_MEDIUM)) + "</img>\n" +
                                    "[L]\n" +
                                    "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
                                    "[L]\n" +
                                    "[C]================================\n" +
                                    "[L]\n" +
                                    "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
                                    "[L]  + Size : S\n" +
                                    "[L]\n" +
                                    "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
                                    "[L]  + Size : 57/58\n" +
                                    "[L]\n" +
                                    "[C]--------------------------------\n" +
                                    "[R]TOTAL PRICE :[R]34.98e\n" +
                                    "[R]TAX :[R]4.23e\n" +
                                    "[L]\n" +
                                    "[C]================================\n" +
                                    "[L]\n" +
                                    "[L]<font size='tall'>Customer :</font>\n" +
                                    "[L]Raymond DUPONT\n" +
                                    "[L]5 rue des girafes\n" +
                                    "[L]31547 PERPETES\n" +
                                    "[L]Tel : +33801201456\n" +
                                    "[L]\n" +
                                    "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
                                    "[C]<qrcode size='20'>http://www.developpeur-web.dantsu.com/</qrcode>"
                    );
        } catch (EscPosConnectionException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this)
                    .setTitle("Broken connection")
                    .setMessage(e.getMessage())
                    .show();
        } catch (EscPosParserException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this)
                    .setTitle("Invalid formatted text")
                    .setMessage(e.getMessage())
                    .show();
        } catch (EscPosEncodingException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this)
                    .setTitle("Bad selected encoding")
                    .setMessage(e.getMessage())
                    .show();
        } catch (EscPosBarcodeException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this)
                    .setTitle("Invalid barcode")
                    .setMessage(e.getMessage())
                    .show();
        }
    }

    /**
     * Asynchronous printing
     */
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 32);
        return printer.setTextToPrint(
                "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.logo, DisplayMetrics.DENSITY_MEDIUM)) + "</img>\n" +
                        "[L]\n" +
                        "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
                        "[L]\n" +
                        "[C]================================\n" +
                        "[L]\n" +
                        "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
                        "[L]  + Size : S\n" +
                        "[L]\n" +
                        "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
                        "[L]  + Size : 57/58\n" +
                        "[L]\n" +
                        "[C]--------------------------------\n" +
                        "[R]TOTAL PRICE :[R]34.98e\n" +
                        "[R]TAX :[R]4.23e\n" +
                        "[L]\n" +
                        "[C]================================\n" +
                        "[L]\n" +
                        "[L]<font size='tall'>Customer :</font>\n" +
                        "[L]Raymond DUPONT\n" +
                        "[L]5 rue des girafes\n" +
                        "[L]31547 PERPETES\n" +
                        "[L]Tel : +33801201456\n" +
                        "[L]\n" +
                        "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
                        "[C]<qrcode size='20'>http://www.developpeur-web.dantsu.com/</qrcode>"
        );
    }
}
