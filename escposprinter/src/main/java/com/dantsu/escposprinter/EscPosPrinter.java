package com.dantsu.escposprinter;

import android.graphics.Bitmap;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.exceptions.EscPosBrokenConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParser;
import com.dantsu.escposprinter.textparser.PrinterTextParserColumn;
import com.dantsu.escposprinter.textparser.IPrinterTextParserElement;
import com.dantsu.escposprinter.textparser.PrinterTextParserLine;

public class EscPosPrinter {
    
    public static final float INCH_TO_MM = 25.4f;
    
    private int printerDpi;
    private float printingWidthMM;
    private int nbrCharactersPerLine;
    private int printingWidthPx;
    private int charSizeWidthPx;
    
    
    private EscPosPrinterCommands printer = null;

    /**
     * Create new instance of EscPosPrinter.
     *
     * @param printerConnection Instance of class which implement DeviceConnection
     * @param printerDpi DPI of the connected printer
     * @param printingWidthMM Printing width in millimeters
     * @param nbrCharactersPerLine The maximum number of characters that can be printed on a line.
     */
    public EscPosPrinter(DeviceConnection printerConnection, int printerDpi, float printingWidthMM, int nbrCharactersPerLine) {
        this(printerConnection != null ? new EscPosPrinterCommands(printerConnection) : null, printerDpi, printingWidthMM, nbrCharactersPerLine);
    }

    /**
     * Create new instance of EscPosPrinter.
     *
     * @param printerConnection Instance of class which implement DeviceConnection
     * @param printerDpi DPI of the connected printer
     * @param printingWidthMM Printing width in millimeters
     * @param nbrCharactersPerLine The maximum number of characters that can be printed on a line.
     * @param charsetEncoding Set the charset encoding.
     */
    public EscPosPrinter(DeviceConnection printerConnection, int printerDpi, float printingWidthMM, int nbrCharactersPerLine, EscPosCharsetEncoding charsetEncoding) {
        this(printerConnection != null ? new EscPosPrinterCommands(printerConnection, charsetEncoding) : null, printerDpi, printingWidthMM, nbrCharactersPerLine);
    }

    /**
     * Create new instance of EscPosPrinter.
     *
     * @param printer Instance of EscPosPrinterCommands
     * @param printerDpi DPI of the connected printer
     * @param printingWidthMM Printing width in millimeters
     * @param nbrCharactersPerLine The maximum number of characters that can be printed on a line.
     */
    public EscPosPrinter(EscPosPrinterCommands printer, int printerDpi, float printingWidthMM, int nbrCharactersPerLine) {
        if (printer != null && printer.connect()) {
            this.printer = printer;
        }
        this.printerDpi = printerDpi;
        this.printingWidthMM = printingWidthMM;
        this.nbrCharactersPerLine = nbrCharactersPerLine;
        
        int printingWidthPx = this.mmToPx(this.printingWidthMM);
        this.printingWidthPx = printingWidthPx + (printingWidthPx % 8);
        
        this.charSizeWidthPx = printingWidthPx / this.nbrCharactersPerLine;
    }
    
    /**
     * Close the connection with the printer.
     *
     * @return Fluent interface
     */
    public EscPosPrinter disconnectPrinter() {
        if (this.printer != null) {
            this.printer.disconnect();
            this.printer = null;
        }
        return this;
    }
    
    /**
     * Get the maximum number of characters that can be printed on a line.
     *
     * @return int
     */
    public int getNbrCharactersPerLine() {
        return this.nbrCharactersPerLine;
    }
    
    /**
     * Get the printing width in millimeters
     *
     * @return float
     */
    public float getPrintingWidthMM() {
        return this.printingWidthMM;
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
    public int getPrintingWidthPx() {
        return this.printingWidthPx;
    }
    
    /**
     * Get the number of dot that a printed character contain
     *
     * @return int
     */
    public int getCharSizeWidthPx() {
        return this.charSizeWidthPx;
    }
    
    /**
     * Convert from millimeters to dot the mmSize variable.
     *
     * @param mmSize Distance in millimeters to be converted
     * @return int
     */
    public int mmToPx(float mmSize) {
        return Math.round(mmSize * ((float) this.printerDpi) / EscPosPrinter.INCH_TO_MM);
    }
    
    /**
     * Print a formatted text. Read the README.md for more information about text formatting options.
     *
     * @param text Formatted text to be printed.
     * @return Fluent interface
     */
    public EscPosPrinter printFormattedText(String text) throws EscPosBrokenConnectionException, EscPosParserException, EscPosEncodingException {
        if (this.printer == null || this.nbrCharactersPerLine == 0) {
            return this;
        }
        
        PrinterTextParser textParser = new PrinterTextParser(this);
        PrinterTextParserLine[] linesParsed = textParser
            .setFormattedText(text)
            .parse();
        
        for (PrinterTextParserLine line : linesParsed) {
            PrinterTextParserColumn[] columns = line.getColumns();
            
            for (PrinterTextParserColumn column : columns) {
                IPrinterTextParserElement[] elements = column.getElements();
                for (IPrinterTextParserElement element : elements) {
                    element.print(this.printer);
                }
            }
            this.printer.newLine();
        }
        
        this.printer
            .newLine()
            .newLine()
            .newLine()
            .newLine();
        
        return this;
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
            maxWidth = this.getPrintingWidthPx(),
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
