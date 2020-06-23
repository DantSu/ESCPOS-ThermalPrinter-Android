package com.dantsu.escposprinter.barcode;

import com.dantsu.escposprinter.EscPosPrinterCommands;
import com.dantsu.escposprinter.EscPosPrinterSize;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;

public class BarcodeUPCE extends Barcode {

    public BarcodeUPCE(EscPosPrinterSize printerSize, String code, float widthMM, float heightMM, int textPosition) throws EscPosBarcodeException {
        super(printerSize, EscPosPrinterCommands.BARCODE_TYPE_UPCE, code, widthMM, heightMM, textPosition);
        this.checkCode();
    }

    public int getCodeLength() {
        return 6;
    }

    @Override
    public int getColsCount() {
        return this.getCodeLength() * 7 + 16;
    }

    private void checkCode() throws EscPosBarcodeException {
        int codeLength = this.getCodeLength();

        if (this.code.length() < codeLength) {
            throw new EscPosBarcodeException("Code is too short for the barcode type.");
        }

        try {
            this.code = this.code.substring(0, codeLength);
            for (int i = 0; i < codeLength; i++) {
                Integer.parseInt(this.code.substring(i, i + 1), 10);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new EscPosBarcodeException("Invalid barcode number");
        }
    }
}
