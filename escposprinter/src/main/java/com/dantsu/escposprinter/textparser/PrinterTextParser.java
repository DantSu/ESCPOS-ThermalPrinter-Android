package com.dantsu.escposprinter.textparser;

import java.util.Arrays;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.EscPosPrinterCommands;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;

public class PrinterTextParser {
    
    public static final String TAGS_ALIGN_LEFT = "L";
    public static final String TAGS_ALIGN_CENTER = "C";
    public static final String TAGS_ALIGN_RIGHT = "R";
    public static final String[] TAGS_ALIGN = {PrinterTextParser.TAGS_ALIGN_LEFT, PrinterTextParser.TAGS_ALIGN_CENTER, PrinterTextParser.TAGS_ALIGN_RIGHT};
    
    public static final String TAGS_IMAGE = "img";
    public static final String TAGS_BARCODE = "barcode";
    public static final String TAGS_QRCODE = "qrcode";

    public static final String ATTR_BARCODE_WIDTH = "width";
    public static final String ATTR_BARCODE_HEIGHT = "height";
    public static final String ATTR_BARCODE_TYPE = "type";
    public static final String ATTR_BARCODE_TYPE_EAN8 = "ean8";
    public static final String ATTR_BARCODE_TYPE_EAN13 = "ean13";
    public static final String ATTR_BARCODE_TYPE_UPCA = "upca";
    public static final String ATTR_BARCODE_TYPE_UPCE = "upce";
    public static final String ATTR_BARCODE_TYPE_128 = "128";
    public static final String ATTR_BARCODE_TEXT_POSITION = "text";
    public static final String ATTR_BARCODE_TEXT_POSITION_NONE = "none";
    public static final String ATTR_BARCODE_TEXT_POSITION_ABOVE = "above";
    public static final String ATTR_BARCODE_TEXT_POSITION_BELOW = "below";

    public static final String TAGS_FORMAT_TEXT_FONT = "font";
    public static final String TAGS_FORMAT_TEXT_BOLD = "b";
    public static final String TAGS_FORMAT_TEXT_UNDERLINE = "u";
    public static final String[] TAGS_FORMAT_TEXT = {PrinterTextParser.TAGS_FORMAT_TEXT_FONT, PrinterTextParser.TAGS_FORMAT_TEXT_BOLD, PrinterTextParser.TAGS_FORMAT_TEXT_UNDERLINE};

    public static final String ATTR_FORMAT_TEXT_UNDERLINE_TYPE = "type";
    public static final String ATTR_FORMAT_TEXT_UNDERLINE_TYPE_NORMAL = "normal";
    public static final String ATTR_FORMAT_TEXT_UNDERLINE_TYPE_DOUBLE = "double";

    public static final String ATTR_FORMAT_TEXT_FONT_SIZE = "size";
    public static final String ATTR_FORMAT_TEXT_FONT_SIZE_BIG = "big";
    public static final String ATTR_FORMAT_TEXT_FONT_SIZE_TALL = "tall";
    public static final String ATTR_FORMAT_TEXT_FONT_SIZE_WIDE = "wide";
    public static final String ATTR_FORMAT_TEXT_FONT_SIZE_NORMAL = "normal";

    public static final String ATTR_FORMAT_TEXT_FONT_COLOR = "color";
    public static final String ATTR_FORMAT_TEXT_FONT_COLOR_BLACK = "black";
    public static final String ATTR_FORMAT_TEXT_FONT_COLOR_BG_BLACK = "bg-black";
    public static final String ATTR_FORMAT_TEXT_FONT_COLOR_RED = "red";
    public static final String ATTR_FORMAT_TEXT_FONT_COLOR_BG_RED = "bg-red";

    public static final String ATTR_QRCODE_SIZE = "size";
    
    private static String regexAlignTags;
    public static String getRegexAlignTags() {
        if(PrinterTextParser.regexAlignTags == null) {
            StringBuilder regexAlignTags = new StringBuilder();
            for (int i = 0; i < PrinterTextParser.TAGS_ALIGN.length; i++) {
                regexAlignTags.append("|\\[").append(PrinterTextParser.TAGS_ALIGN[i]).append("\\]");
            }
            PrinterTextParser.regexAlignTags = regexAlignTags.toString().substring(1);
        }
        return PrinterTextParser.regexAlignTags;
    }
    
    public static boolean isTagTextFormat(String tagName) {
        if (tagName.substring(0, 1).equals("/")) {
            tagName = tagName.substring(1);
        }
        
        for (String tag : PrinterTextParser.TAGS_FORMAT_TEXT) {
            if (tag.equals(tagName)) {
                return true;
            }
        }
        return false;
    }
    
    public static byte[][] arrayByteDropLast(byte[][] arr) {
        if (arr.length == 0) {
            return arr;
        }
        
        byte[][] newArr = new byte[arr.length - 1][];
        System.arraycopy(arr, 0, newArr, 0, newArr.length);
        
        return newArr;
    }
    
    public static byte[][] arrayBytePush(byte[][] arr, byte[] add) {
        byte[][] newArr = new byte[arr.length + 1][];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        newArr[arr.length] = add;
        return newArr;
    }
    
    
    
    
    private EscPosPrinter printer;
    private byte[][] textSize = {EscPosPrinterCommands.TEXT_SIZE_NORMAL};
    private byte[][] textColor = {EscPosPrinterCommands.TEXT_COLOR_BLACK};
    private byte[][] textReverseColor = {EscPosPrinterCommands.TEXT_COLOR_REVERSE_OFF};
    private byte[][] textBold = {EscPosPrinterCommands.TEXT_WEIGHT_NORMAL};
    private byte[][] textUnderline = {EscPosPrinterCommands.TEXT_UNDERLINE_OFF};
    private byte[][] textDoubleStrike = {EscPosPrinterCommands.TEXT_DOUBLE_STRIKE_OFF};
    private String text = "";
    
    public PrinterTextParser(EscPosPrinter printer) {
        this.printer = printer;
    }
    
    public EscPosPrinter getPrinter() {
        return printer;
    }
    
    public PrinterTextParser setFormattedText(String text) {
        this.text = text;
        return this;
    }

    public byte[] getLastTextSize() {
        return this.textSize[this.textSize.length - 1];
    }

    public PrinterTextParser addTextSize(byte[] newTextSize) {
        this.textSize = PrinterTextParser.arrayBytePush(this.textSize, newTextSize);
        return this;
    }

    public PrinterTextParser dropLastTextSize() {
        if (this.textSize.length > 1) {
            this.textSize = PrinterTextParser.arrayByteDropLast(this.textSize);
        }
        return this;
    }

    public byte[] getLastTextColor() {
        return this.textColor[this.textColor.length - 1];
    }

    public PrinterTextParser addTextColor(byte[] newTextColor) {
        this.textColor = PrinterTextParser.arrayBytePush(this.textColor, newTextColor);
        return this;
    }

    public PrinterTextParser dropLastTextColor() {
        if (this.textColor.length > 1) {
            this.textColor = PrinterTextParser.arrayByteDropLast(this.textColor);
        }
        return this;
    }

    public byte[] getLastTextReverseColor() {
        return this.textReverseColor[this.textReverseColor.length - 1];
    }

    public PrinterTextParser addTextReverseColor(byte[] newTextReverseColor) {
        this.textReverseColor = PrinterTextParser.arrayBytePush(this.textReverseColor, newTextReverseColor);
        return this;
    }

    public PrinterTextParser dropLastTextReverseColor() {
        if (this.textReverseColor.length > 1) {
            this.textReverseColor = PrinterTextParser.arrayByteDropLast(this.textReverseColor);
        }
        return this;
    }
    
    public byte[] getLastTextBold() {
        return this.textBold[this.textBold.length - 1];
    }
    
    public PrinterTextParser addTextBold(byte[] newTextBold) {
        this.textBold = PrinterTextParser.arrayBytePush(this.textBold, newTextBold);
        return this;
    }

    public PrinterTextParser dropTextBold() {
        if (this.textBold.length > 1) {
            this.textBold = PrinterTextParser.arrayByteDropLast(this.textBold);
        }
        return this;
    }

    public byte[] getLastTextUnderline() {
        return this.textUnderline[this.textUnderline.length - 1];
    }

    public PrinterTextParser addTextUnderline(byte[] newTextUnderline) {
        this.textUnderline = PrinterTextParser.arrayBytePush(this.textUnderline, newTextUnderline);
        return this;
    }

    public PrinterTextParser dropLastTextUnderline() {
        if (this.textUnderline.length > 1) {
            this.textUnderline = PrinterTextParser.arrayByteDropLast(this.textUnderline);
        }
        return this;
    }

    public byte[] getLastTextDoubleStrike() {
        return this.textDoubleStrike[this.textDoubleStrike.length - 1];
    }

    public PrinterTextParser addTextDoubleStrike(byte[] newTextDoubleStrike) {
        this.textDoubleStrike = PrinterTextParser.arrayBytePush(this.textDoubleStrike, newTextDoubleStrike);
        return this;
    }

    public PrinterTextParser dropLastTextDoubleStrike() {
        if (this.textDoubleStrike.length > 1) {
            this.textDoubleStrike = PrinterTextParser.arrayByteDropLast(this.textDoubleStrike);
        }
        return this;
    }
    
    public PrinterTextParserLine[] parse() throws EscPosParserException, EscPosBarcodeException, EscPosEncodingException {
        String[] stringLines = this.text.split("\n|\r\n");
        PrinterTextParserLine[] lines = new PrinterTextParserLine[stringLines.length];
        int i = 0;
        for (String line : stringLines) {
            lines[i++] = new PrinterTextParserLine(this, line);
        }
        return lines;
    }
}
