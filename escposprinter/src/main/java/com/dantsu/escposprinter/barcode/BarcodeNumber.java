package com.dantsu.escposprinter.barcode;

import com.dantsu.escposprinter.EscPosPrinterSize;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;

public abstract class BarcodeNumber extends Barcode {

    public BarcodeNumber(EscPosPrinterSize printerSize, int barcodeType, String code, float widthMM, float heightMM, int textPosition) throws EscPosBarcodeException {
        super(printerSize, barcodeType, code, widthMM, heightMM, textPosition);
        this.checkCode();
    }

    @Override
    public int getColsCount() {
        return this.getCodeLength() * 7 + 11;
    }

    private void checkCode() throws EscPosBarcodeException {
        int codeLength = this.getCodeLength() - 1;

        if (this.code.length() < codeLength) {
            throw new EscPosBarcodeException("Code is too short for the barcode type.");
        }

        try {

            String code = this.code.substring(0, codeLength);
            int totalBarcodeKey = 0;
            for (int i = 0; i < codeLength; i++) {
                int
                        pos = codeLength - 1 - i,
                        intCode = Integer.parseInt(code.substring(pos, pos + 1), 10);
                if (i % 2 == 0) {
                    intCode = 3 * intCode;
                }
                totalBarcodeKey += intCode;
            }

            String barcodeKey = String.valueOf(10 - (totalBarcodeKey % 10));
            if (barcodeKey.length() == 2) {
                barcodeKey = "0";
            }
            this.code = code + barcodeKey;

        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new EscPosBarcodeException("Invalid barcode number");
        }
    }
}
