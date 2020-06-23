package com.dantsu.escposprinter.barcode;

import com.dantsu.escposprinter.EscPosPrinterCommands;
import com.dantsu.escposprinter.EscPosPrinterSize;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;

public class BarcodeUPCA extends BarcodeNumber {

    public BarcodeUPCA(EscPosPrinterSize printerSize, String code, float widthMM, float heightMM, int textPosition) throws EscPosBarcodeException {
        super(printerSize, EscPosPrinterCommands.BARCODE_TYPE_UPCA, code, widthMM, heightMM, textPosition);
    }

    @Override
    public int getCodeLength() {
        return 12;
    }
}
