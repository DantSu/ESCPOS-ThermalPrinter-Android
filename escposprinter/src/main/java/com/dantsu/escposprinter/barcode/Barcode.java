package com.dantsu.escposprinter.barcode;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;

public abstract class Barcode {

    protected EscPosPrinter printer;
    protected int barcodeType;
    protected String code;
    protected int colWidth;
    protected int height;
    protected int textPosition;

    Barcode(EscPosPrinter printer, int barcodeType, String code, float widthMM, float heightMM, int textPosition) throws EscPosBarcodeException {
        this.printer = printer;
        this.barcodeType = barcodeType;
        this.code = code;
        this.height = printer.mmToPx(heightMM);
        this.textPosition = textPosition;

        if(widthMM == 0f) {
            widthMM = printer.getPrintingWidthMM() * 0.7f;
        }

        int wantedPxWidth = widthMM > printer.getPrintingWidthMM() ? printer.getPrintingWidthPx() : printer.mmToPx(widthMM);

        int colWidth = (int)Math.round((double) wantedPxWidth / (double) this.getColsCount());

        if((colWidth * this.getColsCount()) > printer.getPrintingWidthPx()) {
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
