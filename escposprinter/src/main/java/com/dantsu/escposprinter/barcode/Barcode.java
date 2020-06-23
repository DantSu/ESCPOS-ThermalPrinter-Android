package com.dantsu.escposprinter.barcode;

import com.dantsu.escposprinter.EscPosPrinterSize;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;

public abstract class Barcode {

    protected int barcodeType;
    protected String code;
    protected int colWidth;
    protected int height;
    protected int textPosition;

    Barcode(EscPosPrinterSize printerSize, int barcodeType, String code, float widthMM, float heightMM, int textPosition) throws EscPosBarcodeException {
        this.barcodeType = barcodeType;
        this.code = code;
        this.height = printerSize.mmToPx(heightMM);
        this.textPosition = textPosition;

        if(widthMM == 0f) {
            widthMM = printerSize.getPrinterWidthMM() * 0.7f;
        }

        int
                wantedPxWidth = widthMM > printerSize.getPrinterWidthMM() ? printerSize.getPrinterWidthPx() : printerSize.mmToPx(widthMM),
                colWidth = (int)Math.round((double) wantedPxWidth / (double) this.getColsCount());

        if((colWidth * this.getColsCount()) > printerSize.getPrinterWidthPx()) {
            --colWidth;
        }

        if(colWidth == 0) {
            throw new EscPosBarcodeException("Barcode is too long for the paper size.");
        }

        this.colWidth = colWidth;
    }

    public abstract int getCodeLength();

    public abstract int getColsCount();

    public int getBarcodeType() {
        return this.barcodeType;
    }

    public String getCode() {
        return this.code;
    }

    public int getHeight() {
        return this.height;
    }

    public int getTextPosition() {
        return this.textPosition;
    }

    public int getColWidth() {
        return this.colWidth;
    }

}
