package com.dantsu.escposprinter.textparser;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.EscPosPrinterCommands;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.ParserException;

import java.util.Hashtable;

public class PrinterTextParserQRCode extends PrinterTextParserImg {

    private static byte[] initConstructor(PrinterTextParserColumn printerTextParserColumn,
                                          Hashtable<String, String> qrCodeAttributes, String data) throws ParserException, EscPosEncodingException {
        EscPosPrinter printer = printerTextParserColumn.getLine().getTextParser().getPrinter();
        data = data.trim();

        int size = printer.mmToPx(20f);

        if (qrCodeAttributes.containsKey(PrinterTextParser.ATTR_QRCODE_SIZE)) {
            String qrCodeAttribute = qrCodeAttributes.get(PrinterTextParser.ATTR_QRCODE_SIZE);
            if (qrCodeAttribute == null)
                throw new ParserException("Invalid QR code attribute");
            size = printer.mmToPx(Float.parseFloat(qrCodeAttribute));
        }

        return EscPosPrinterCommands.QRCodeDataToBytes(data, size);
    }

    public PrinterTextParserQRCode(PrinterTextParserColumn printerTextParserColumn, String textAlign,
                                   Hashtable<String, String> qrCodeAttributes, String data) throws ParserException, EscPosEncodingException {
        super(
                printerTextParserColumn,
                textAlign,
                PrinterTextParserQRCode.initConstructor(printerTextParserColumn, qrCodeAttributes, data)
        );
    }
}
