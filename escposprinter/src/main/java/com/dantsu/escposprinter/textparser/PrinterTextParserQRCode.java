package com.dantsu.escposprinter.textparser;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.EscPosPrinterCommands;

import java.util.Hashtable;

public class PrinterTextParserQRCode extends PrinterTextParserImg {

    private static byte[] initConstructor(PrinterTextParserColumn printerTextParserColumn, Hashtable<String, String> qrCodeAttributes, String data) {
        EscPosPrinter printer = printerTextParserColumn.getLine().getTextParser().getPrinter();
        data = data.trim();

        int size = printer.mmToPx(20f);
        try {
            if (qrCodeAttributes.containsKey(PrinterTextParser.ATTR_QRCODE_SIZE)) {
                size = printer.mmToPx(Float.parseFloat(qrCodeAttributes.get(PrinterTextParser.ATTR_QRCODE_SIZE)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return EscPosPrinterCommands.QRCodeDataToBytes(data, size);
    }

    public PrinterTextParserQRCode(PrinterTextParserColumn printerTextParserColumn, String textAlign, Hashtable<String, String> qrCodeAttributes, String data) {
        super(
                printerTextParserColumn,
                textAlign,
                PrinterTextParserQRCode.initConstructor(printerTextParserColumn, qrCodeAttributes, data)
        );
    }
}
