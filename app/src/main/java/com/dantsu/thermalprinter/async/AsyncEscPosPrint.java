package com.dantsu.thermalprinter.async;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.dantsu.escposprinter.EscPosCharsetEncoding;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;

import java.lang.ref.WeakReference;

public abstract class AsyncEscPosPrint extends AsyncTask<AsyncEscPosPrinter, Integer, AsyncEscPosPrint.PrinterStatus> {
    public final static int FINISH_SUCCESS = 1;
    public final static int FINISH_NO_PRINTER = 2;
    public final static int FINISH_PRINTER_DISCONNECTED = 3;
    public final static int FINISH_PARSER_ERROR = 4;
    public final static int FINISH_ENCODING_ERROR = 5;
    public final static int FINISH_BARCODE_ERROR = 6;

    protected final static int PROGRESS_CONNECTING = 1;
    protected final static int PROGRESS_CONNECTED = 2;
    protected final static int PROGRESS_PRINTING = 3;
    protected final static int PROGRESS_PRINTED = 4;

    protected ProgressDialog dialog;
    protected WeakReference<Context> weakContext;
    protected OnPrintFinished onPrintFinished;


    public AsyncEscPosPrint(Context context) {
        this(context, null);
    }

    public AsyncEscPosPrint(Context context, OnPrintFinished onPrintFinished) {
        this.weakContext = new WeakReference<>(context);
        this.onPrintFinished = onPrintFinished;
    }

    protected PrinterStatus doInBackground(AsyncEscPosPrinter... printersData) {
        if (printersData.length == 0) {
            return new PrinterStatus(null, AsyncEscPosPrint.FINISH_NO_PRINTER);
        }

        this.publishProgress(AsyncEscPosPrint.PROGRESS_CONNECTING);

        AsyncEscPosPrinter printerData = printersData[0];

        try {
            DeviceConnection deviceConnection = printerData.getPrinterConnection();

            if(deviceConnection == null) {
                return new PrinterStatus(null, AsyncEscPosPrint.FINISH_NO_PRINTER);
            }

            EscPosPrinter printer = new EscPosPrinter(
                    deviceConnection,
                    printerData.getPrinterDpi(),
                    printerData.getPrinterWidthMM(),
                    printerData.getPrinterNbrCharactersPerLine(),
                    new EscPosCharsetEncoding("windows-1252", 16)
            );

            // printer.useEscAsteriskCommand(true);

            this.publishProgress(AsyncEscPosPrint.PROGRESS_PRINTING);

            String[] textsToPrint = printerData.getTextsToPrint();

            for(String textToPrint : textsToPrint) {
                printer.printFormattedTextAndCut(textToPrint);
                Thread.sleep(500);
            }

            this.publishProgress(AsyncEscPosPrint.PROGRESS_PRINTED);

        } catch (EscPosConnectionException e) {
            e.printStackTrace();
            return new PrinterStatus(printerData, AsyncEscPosPrint.FINISH_PRINTER_DISCONNECTED);
        } catch (EscPosParserException e) {
            e.printStackTrace();
            return new PrinterStatus(printerData, AsyncEscPosPrint.FINISH_PARSER_ERROR);
        } catch (EscPosEncodingException e) {
            e.printStackTrace();
            return new PrinterStatus(printerData, AsyncEscPosPrint.FINISH_ENCODING_ERROR);
        } catch (EscPosBarcodeException e) {
            e.printStackTrace();
            return new PrinterStatus(printerData, AsyncEscPosPrint.FINISH_BARCODE_ERROR);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new PrinterStatus(printerData, AsyncEscPosPrint.FINISH_SUCCESS);
    }

    protected void onPreExecute() {
        if (this.dialog == null) {
            Context context = weakContext.get();

            if (context == null) {
                return;
            }

            this.dialog = new ProgressDialog(context);
            this.dialog.setTitle("Printing in progress...");
            this.dialog.setMessage("...");
            this.dialog.setProgressNumberFormat("%1d / %2d");
            this.dialog.setCancelable(false);
            this.dialog.setIndeterminate(false);
            this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.dialog.show();
        }
    }

    protected void onProgressUpdate(Integer... progress) {
        switch (progress[0]) {
            case AsyncEscPosPrint.PROGRESS_CONNECTING:
                this.dialog.setMessage("Connecting printer...");
                break;
            case AsyncEscPosPrint.PROGRESS_CONNECTED:
                this.dialog.setMessage("Printer is connected...");
                break;
            case AsyncEscPosPrint.PROGRESS_PRINTING:
                this.dialog.setMessage("Printer is printing...");
                break;
            case AsyncEscPosPrint.PROGRESS_PRINTED:
                this.dialog.setMessage("Printer has finished...");
                break;
        }
        this.dialog.setProgress(progress[0]);
        this.dialog.setMax(4);
    }

    protected void onPostExecute(PrinterStatus result) {
        this.dialog.dismiss();
        this.dialog = null;

        Context context = weakContext.get();

        if (context == null) {
            return;
        }

        switch (result.getPrinterStatus()) {
            case AsyncEscPosPrint.FINISH_SUCCESS:
                new AlertDialog.Builder(context)
                        .setTitle("Success")
                        .setMessage("Congratulation ! The texts are printed !")
                        .show();
                break;
            case AsyncEscPosPrint.FINISH_NO_PRINTER:
                new AlertDialog.Builder(context)
                        .setTitle("No printer")
                        .setMessage("The application can't find any printer connected.")
                        .show();
                break;
            case AsyncEscPosPrint.FINISH_PRINTER_DISCONNECTED:
                new AlertDialog.Builder(context)
                    .setTitle("Broken connection")
                    .setMessage("Unable to connect the printer.")
                    .show();
                break;
            case AsyncEscPosPrint.FINISH_PARSER_ERROR:
                new AlertDialog.Builder(context)
                    .setTitle("Invalid formatted text")
                    .setMessage("It seems to be an invalid syntax problem.")
                    .show();
                break;
            case AsyncEscPosPrint.FINISH_ENCODING_ERROR:
                new AlertDialog.Builder(context)
                    .setTitle("Bad selected encoding")
                    .setMessage("The selected encoding character returning an error.")
                    .show();
                break;
            case AsyncEscPosPrint.FINISH_BARCODE_ERROR:
                new AlertDialog.Builder(context)
                    .setTitle("Invalid barcode")
                    .setMessage("Data send to be converted to barcode or QR code seems to be invalid.")
                    .show();
                break;
        }
        if(this.onPrintFinished != null) {
            if (result.getPrinterStatus() == AsyncEscPosPrint.FINISH_SUCCESS) {
                this.onPrintFinished.onSuccess(result.getAsyncEscPosPrinter());
            } else {
                this.onPrintFinished.onError(result.getAsyncEscPosPrinter(), result.getPrinterStatus());
            }
        }
    }

    public static class PrinterStatus {
        private AsyncEscPosPrinter asyncEscPosPrinter;
        private int printerStatus;

        public PrinterStatus (AsyncEscPosPrinter asyncEscPosPrinter, int printerStatus) {
            this.asyncEscPosPrinter = asyncEscPosPrinter;
            this.printerStatus = printerStatus;
        }

        public AsyncEscPosPrinter getAsyncEscPosPrinter() {
            return asyncEscPosPrinter;
        }

        public int getPrinterStatus() {
            return printerStatus;
        }
    }

    public static abstract class OnPrintFinished {
        public abstract void onError(AsyncEscPosPrinter asyncEscPosPrinter, int codeException);
        public abstract void onSuccess(AsyncEscPosPrinter asyncEscPosPrinter);
    }
}
