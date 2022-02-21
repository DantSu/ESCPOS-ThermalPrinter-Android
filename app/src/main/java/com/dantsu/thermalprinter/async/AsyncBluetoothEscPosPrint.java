package com.dantsu.thermalprinter.async;

import android.content.Context;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;

public class AsyncBluetoothEscPosPrint extends AsyncEscPosPrint {
    public AsyncBluetoothEscPosPrint(Context context) {
        super(context);
    }

    public AsyncBluetoothEscPosPrint(Context context, OnPrintFinished onPrintFinished) {
        super(context, onPrintFinished);
    }

    protected PrinterStatus doInBackground(AsyncEscPosPrinter... printersData) {
        if (printersData.length == 0) {
            return new PrinterStatus(null, AsyncEscPosPrint.FINISH_NO_PRINTER);
        }

        AsyncEscPosPrinter printerData = printersData[0];
        DeviceConnection deviceConnection = printerData.getPrinterConnection();

        this.publishProgress(AsyncEscPosPrint.PROGRESS_CONNECTING);

        if (deviceConnection == null) {
            printersData[0] = new AsyncEscPosPrinter(
                    BluetoothPrintersConnections.selectFirstPaired(),
                    printerData.getPrinterDpi(),
                    printerData.getPrinterWidthMM(),
                    printerData.getPrinterNbrCharactersPerLine()
            );
            printersData[0].setTextsToPrint(printerData.getTextsToPrint());
        } else {
            try {
                deviceConnection.connect();
            } catch (EscPosConnectionException e) {
                e.printStackTrace();
            }
        }

        return super.doInBackground(printersData);
    }
}
