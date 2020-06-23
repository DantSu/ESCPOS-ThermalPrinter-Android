package com.dantsu.escposprinter;


import android.graphics.Bitmap;

public abstract class EscPosPrinterSize {

    public static final float INCH_TO_MM = 25.4f;

    protected int printerDpi;
    protected float printerWidthMM;
    protected int printerNbrCharactersPerLine;
    protected int printerWidthPx;
    protected int printerCharSizeWidthPx;

    protected EscPosPrinterSize(int printerDpi, float printerWidthMM, int printerNbrCharactersPerLine) {
        this.printerDpi = printerDpi;
        this.printerWidthMM = printerWidthMM;
        this.printerNbrCharactersPerLine = printerNbrCharactersPerLine;
        int printingWidthPx = this.mmToPx(this.printerWidthMM);
        this.printerWidthPx = printingWidthPx + (printingWidthPx % 8);
        this.printerCharSizeWidthPx = printingWidthPx / this.printerNbrCharactersPerLine;
    }

    /**
     * Get the maximum number of characters that can be printed on a line.
     *
     * @return int
     */
    public int getPrinterNbrCharactersPerLine() {
        return this.printerNbrCharactersPerLine;
    }

    /**
     * Get the printing width in millimeters
     *
     * @return float
     */
    public float getPrinterWidthMM() {
        return this.printerWidthMM;
    }

    /**
     * Get the printer DPI
     *
     * @return int
     */
    public int getPrinterDpi() {
        return this.printerDpi;
    }

    /**
     * Get the printing width in dot
     *
     * @return int
     */
    public int getPrinterWidthPx() {
        return this.printerWidthPx;
    }

    /**
     * Get the number of dot that a printed character contain
     *
     * @return int
     */
    public int getPrinterCharSizeWidthPx() {
        return this.printerCharSizeWidthPx;
    }

    /**
     * Convert from millimeters to dot the mmSize variable.
     *
     * @param mmSize Distance in millimeters to be converted
     * @return int
     */
    public int mmToPx(float mmSize) {
        return Math.round(mmSize * ((float) this.printerDpi) / EscPosPrinterSize.INCH_TO_MM);
    }


    /**
     * Convert Bitmap object to ESC/POS image.
     *
     * @param bitmap Instance of Bitmap
     * @return Bytes contain the image in ESC/POS command
     */
    public byte[] bitmapToBytes(Bitmap bitmap) {
        boolean isSizeEdit = false;
        int bitmapWidth = bitmap.getWidth(),
                bitmapHeight = bitmap.getHeight(),
                maxWidth = this.printerWidthPx,
                maxHeight = 256;

        if (bitmapWidth > maxWidth) {
            bitmapHeight = Math.round(((float) bitmapHeight) * ((float) maxWidth) / ((float) bitmapWidth));
            bitmapWidth = maxWidth;
            isSizeEdit = true;
        }
        if (bitmapHeight > maxHeight) {
            bitmapWidth = Math.round(((float) bitmapWidth) * ((float) maxHeight) / ((float) bitmapHeight));
            bitmapHeight = maxHeight;
            isSizeEdit = true;
        }

        if (isSizeEdit) {
            bitmap = Bitmap.createScaledBitmap(bitmap, bitmapWidth, bitmapHeight, false);
        }

        return EscPosPrinterCommands.bitmapToBytes(bitmap);
    }
}
