package com.dantsu.escposprinter.textparser;

import java.util.Hashtable;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.EscPosPrinterCommands;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;

public class PrinterTextParserColumn {

    private static String generateSpace(int nbrSpace) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < nbrSpace; i++) {
            str.append(" ");
        }
        return str.toString();
    }


    private PrinterTextParserLine textParserLine;
    private IPrinterTextParserElement[] elements = new IPrinterTextParserElement[0];

    /**
     * Create a new instance of PrinterTextParserColumn.
     *
     * @param textParserLine Parent PrinterTextParserLine instance
     * @param textColumn     Text that the column contain
     */
    public PrinterTextParserColumn(PrinterTextParserLine textParserLine, String textColumn) throws EscPosParserException, EscPosBarcodeException, EscPosEncodingException {
        this.textParserLine = textParserLine;
        PrinterTextParser textParser = this.textParserLine.getTextParser();
        String textAlign = PrinterTextParser.TAGS_ALIGN_LEFT;
        byte[]
                textUnderlineStartColumn = textParser.getLastTextUnderline(),
                textDoubleStrikeStartColumn = textParser.getLastTextDoubleStrike(),
                textColorStartColumn = textParser.getLastTextColor(),
                textReverseColorStartColumn = textParser.getLastTextReverseColor();


        // =================================================================
        // Check the column alignment
        if(textColumn.length() > 2) {
            switch (textColumn.substring(0, 3).toUpperCase()) {
                case "[" + PrinterTextParser.TAGS_ALIGN_LEFT + "]":
                case "[" + PrinterTextParser.TAGS_ALIGN_CENTER + "]":
                case "[" + PrinterTextParser.TAGS_ALIGN_RIGHT + "]":
                    textAlign = textColumn.substring(1, 2).toUpperCase();
                    textColumn = textColumn.substring(3);
                    break;
            }
        }

        String trimmedTextColumn = textColumn.trim();
        boolean isImgOrBarcodeLine = false;

        if (this.textParserLine.getNbrColumns() == 1 && trimmedTextColumn.indexOf("<") == 0) {
            // =================================================================
            // Image or Barcode Lines
            int openTagIndex = trimmedTextColumn.indexOf("<"),
                    openTagEndIndex = trimmedTextColumn.indexOf(">", openTagIndex + 1) + 1;

            if (openTagIndex < openTagEndIndex) {
                PrinterTextParserTag textParserTag = new PrinterTextParserTag(trimmedTextColumn.substring(openTagIndex, openTagEndIndex));

                switch (textParserTag.getTagName()) {
                    case PrinterTextParser.TAGS_IMAGE:
                    case PrinterTextParser.TAGS_BARCODE:
                    case PrinterTextParser.TAGS_QRCODE:
                        String closeTag = "</" + textParserTag.getTagName() + ">";
                        int closeTagPosition = trimmedTextColumn.length() - closeTag.length();

                        if (trimmedTextColumn.substring(closeTagPosition).equals(closeTag)) {
                            switch (textParserTag.getTagName()) {
                                case PrinterTextParser.TAGS_IMAGE:
                                    this.appendImage(textAlign, trimmedTextColumn.substring(openTagEndIndex, closeTagPosition));
                                    break;
                                case PrinterTextParser.TAGS_BARCODE:
                                    this.appendBarcode(textAlign, textParserTag.getAttributes(), trimmedTextColumn.substring(openTagEndIndex, closeTagPosition));
                                    break;
                                case PrinterTextParser.TAGS_QRCODE:
                                    this.appendQRCode(textAlign, textParserTag.getAttributes(), trimmedTextColumn.substring(openTagEndIndex, closeTagPosition));
                                    break;
                            }
                            isImgOrBarcodeLine = true;
                        }
                        break;
                }
            }
        }

        if (!isImgOrBarcodeLine) {
            // =================================================================
            // If the tag is for format text

            int offset = 0;
            while (true) {
                int openTagIndex = textColumn.indexOf("<", offset), closeTagIndex = -1;

                if (openTagIndex != -1) {
                    closeTagIndex = textColumn.indexOf(">", openTagIndex);
                } else {
                    openTagIndex = textColumn.length();
                }

                this.appendString(textColumn.substring(offset, openTagIndex));

                if (closeTagIndex == -1) {
                    break;
                }

                closeTagIndex++;
                PrinterTextParserTag textParserTag = new PrinterTextParserTag(textColumn.substring(openTagIndex, closeTagIndex));

                if (PrinterTextParser.isTagTextFormat(textParserTag.getTagName())) {
                    if (textParserTag.isCloseTag()) {
                        switch (textParserTag.getTagName()) {
                            case PrinterTextParser.TAGS_FORMAT_TEXT_BOLD:
                                textParser.dropTextBold();
                                break;
                            case PrinterTextParser.TAGS_FORMAT_TEXT_UNDERLINE:
                                textParser.dropLastTextUnderline();
                                textParser.dropLastTextDoubleStrike();
                                break;
                            case PrinterTextParser.TAGS_FORMAT_TEXT_FONT:
                                textParser.dropLastTextSize();
                                textParser.dropLastTextColor();
                                textParser.dropLastTextReverseColor();
                                break;
                        }
                    } else {
                        switch (textParserTag.getTagName()) {
                            case PrinterTextParser.TAGS_FORMAT_TEXT_BOLD:
                                textParser.addTextBold(EscPosPrinterCommands.TEXT_WEIGHT_BOLD);
                                break;
                            case PrinterTextParser.TAGS_FORMAT_TEXT_UNDERLINE:
                                if (textParserTag.hasAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_UNDERLINE_TYPE)) {
                                    switch (textParserTag.getAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_UNDERLINE_TYPE)) {
                                        case PrinterTextParser.ATTR_FORMAT_TEXT_UNDERLINE_TYPE_NORMAL:
                                            textParser.addTextUnderline(EscPosPrinterCommands.TEXT_UNDERLINE_LARGE);
                                            textParser.addTextDoubleStrike(textParser.getLastTextDoubleStrike());
                                            break;
                                        case PrinterTextParser.ATTR_FORMAT_TEXT_UNDERLINE_TYPE_DOUBLE:
                                            textParser.addTextUnderline(textParser.getLastTextUnderline());
                                            textParser.addTextDoubleStrike(EscPosPrinterCommands.TEXT_DOUBLE_STRIKE_ON);
                                            break;
                                    }
                                } else {
                                    textParser.addTextUnderline(EscPosPrinterCommands.TEXT_UNDERLINE_LARGE);
                                    textParser.addTextDoubleStrike(textParser.getLastTextDoubleStrike());
                                }
                                break;
                            case PrinterTextParser.TAGS_FORMAT_TEXT_FONT:
                                if (textParserTag.hasAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE)) {
                                    switch (textParserTag.getAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE)) {
                                        case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_NORMAL:
                                        default:
                                            textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_NORMAL);
                                            break;
                                        case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_TALL:
                                            textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_DOUBLE_HEIGHT);
                                            break;
                                        case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_WIDE:
                                            textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_DOUBLE_WIDTH);
                                            break;
                                        case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_BIG:
                                            textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG);
                                            break;
                                        case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_BIG_2:
                                            textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_2);
                                            break;
                                        case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_BIG_3:
                                            textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_3);
                                            break;
                                        case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_BIG_4:
                                            textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_4);
                                            break;
                                        case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_BIG_5:
                                            textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_5);
                                            break;
                                        case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_BIG_6:
                                            textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_6);
                                            break;
                                    }
                                } else {
                                    textParser.addTextSize(textParser.getLastTextSize());
                                }

                                if (textParserTag.hasAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR)) {
                                    switch (textParserTag.getAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR)) {
                                        case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR_BLACK:
                                        default:
                                            textParser.addTextColor(EscPosPrinterCommands.TEXT_COLOR_BLACK);
                                            textParser.addTextReverseColor(EscPosPrinterCommands.TEXT_COLOR_REVERSE_OFF);
                                            break;
                                        case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR_BG_BLACK:
                                            textParser.addTextColor(EscPosPrinterCommands.TEXT_COLOR_BLACK);
                                            textParser.addTextReverseColor(EscPosPrinterCommands.TEXT_COLOR_REVERSE_ON);
                                            break;
                                        case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR_RED:
                                            textParser.addTextColor(EscPosPrinterCommands.TEXT_COLOR_RED);
                                            textParser.addTextReverseColor(EscPosPrinterCommands.TEXT_COLOR_REVERSE_OFF);
                                            break;
                                        case PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR_BG_RED:
                                            textParser.addTextColor(EscPosPrinterCommands.TEXT_COLOR_RED);
                                            textParser.addTextReverseColor(EscPosPrinterCommands.TEXT_COLOR_REVERSE_ON);
                                            break;
                                    }
                                } else {
                                    textParser.addTextColor(textParser.getLastTextColor());
                                    textParser.addTextReverseColor(textParser.getLastTextReverseColor());
                                }
                                break;
                        }
                    }
                    offset = closeTagIndex;
                } else {
                    this.appendString("<");
                    offset = openTagIndex + 1;
                }
            }

            // =================================================================
            // Define the number of spaces required for the different alignments

            int nbrCharColumn = this.textParserLine.getNbrCharColumn(),
                    nbrCharForgetted = this.textParserLine.getNbrCharForgetted(),
                    nbrCharColumnExceeded = this.textParserLine.getNbrCharColumnExceeded(),
                    nbrCharTextWithoutTag = 0,
                    leftSpace = 0,
                    rightSpace = 0;

            for (IPrinterTextParserElement textParserElement : this.elements) {
                nbrCharTextWithoutTag += textParserElement.length();
            }

            switch (textAlign) {
                case PrinterTextParser.TAGS_ALIGN_LEFT:
                    rightSpace = nbrCharColumn - nbrCharTextWithoutTag;
                    break;
                case PrinterTextParser.TAGS_ALIGN_CENTER:
                    leftSpace = (int) Math.floor((((float) nbrCharColumn) - ((float) nbrCharTextWithoutTag)) / 2f);
                    rightSpace = nbrCharColumn - nbrCharTextWithoutTag - leftSpace;
                    break;
                case PrinterTextParser.TAGS_ALIGN_RIGHT:
                    leftSpace = nbrCharColumn - nbrCharTextWithoutTag;
                    break;
            }

            if (nbrCharForgetted > 0) {
                nbrCharForgetted -= 1;
                rightSpace++;
            }

            if (nbrCharColumnExceeded < 0) {
                leftSpace += nbrCharColumnExceeded;
                nbrCharColumnExceeded = 0;
                if (leftSpace < 1) {
                    rightSpace += leftSpace - 1;
                    leftSpace = 1;
                }
            }

            if (leftSpace < 0) {
                nbrCharColumnExceeded += leftSpace;
                leftSpace = 0;
            }
            if (rightSpace < 0) {
                nbrCharColumnExceeded += rightSpace;
                rightSpace = 0;
            }

            if (leftSpace > 0) {
                this.prependString(PrinterTextParserColumn.generateSpace(leftSpace), EscPosPrinterCommands.TEXT_SIZE_NORMAL, textColorStartColumn, textReverseColorStartColumn, EscPosPrinterCommands.TEXT_WEIGHT_NORMAL, textUnderlineStartColumn, textDoubleStrikeStartColumn);
            }
            if (rightSpace > 0) {
                this.appendString(PrinterTextParserColumn.generateSpace(rightSpace), EscPosPrinterCommands.TEXT_SIZE_NORMAL, textParser.getLastTextColor(), textParser.getLastTextReverseColor(), EscPosPrinterCommands.TEXT_WEIGHT_NORMAL, textParser.getLastTextUnderline(), textParser.getLastTextDoubleStrike());
            }

            // =================================================================================================
            // nbrCharForgetted and nbrCharColumnExceeded is use to define number of spaces for the next columns

            this.textParserLine
                    .setNbrCharForgetted(nbrCharForgetted)
                    .setNbrCharColumnExceeded(nbrCharColumnExceeded);
        }
    }

    private PrinterTextParserColumn prependString(String text) {
        PrinterTextParser textParser = this.textParserLine.getTextParser();
        return this.prependString(text, textParser.getLastTextSize(), textParser.getLastTextColor(), textParser.getLastTextReverseColor(), textParser.getLastTextBold(), textParser.getLastTextUnderline(), textParser.getLastTextDoubleStrike());
    }

    private PrinterTextParserColumn prependString(String text, byte[] textSize, byte[] textColor, byte[] textReverseColor, byte[] textBold, byte[] textUnderline, byte[] textDoubleStrike) {
        return this.prependElement(new PrinterTextParserString(this, text, textSize, textColor, textReverseColor, textBold, textUnderline, textDoubleStrike));
    }

    private PrinterTextParserColumn appendString(String text) {
        PrinterTextParser textParser = this.textParserLine.getTextParser();
        return this.appendString(text, textParser.getLastTextSize(), textParser.getLastTextColor(), textParser.getLastTextReverseColor(), textParser.getLastTextBold(), textParser.getLastTextUnderline(), textParser.getLastTextDoubleStrike());
    }

    private PrinterTextParserColumn appendString(String text, byte[] textSize, byte[] textColor, byte[] textReverseColor, byte[] textBold, byte[] textUnderline, byte[] textDoubleStrike) {
        EscPosPrinter printer = this.textParserLine.getTextParser().getPrinter();
        return this.appendElement(new PrinterTextParserString(this, text, textSize, textColor, textReverseColor, textBold, textUnderline, textDoubleStrike));
    }

    private PrinterTextParserColumn prependImage(String textAlign, String hexString) {
        return this.prependElement(new PrinterTextParserImg(this, textAlign, hexString));
    }

    private PrinterTextParserColumn appendImage(String textAlign, String hexString) {
        return this.appendElement(new PrinterTextParserImg(this, textAlign, hexString));
    }

    private PrinterTextParserColumn prependBarcode(String textAlign, Hashtable<String, String> barcodeAttributes, String code) throws EscPosParserException, EscPosBarcodeException {
        return this.prependElement(new PrinterTextParserBarcode(this, textAlign, barcodeAttributes, code));
    }

    private PrinterTextParserColumn appendBarcode(String textAlign, Hashtable<String, String> barcodeAttributes, String code) throws EscPosParserException, EscPosBarcodeException {
        return this.appendElement(new PrinterTextParserBarcode(this, textAlign, barcodeAttributes, code));
    }

    private PrinterTextParserColumn prependQRCode(String textAlign, Hashtable<String, String> qrCodeAttributes, String data) throws EscPosParserException, EscPosBarcodeException {
        return this.prependElement(new PrinterTextParserBarcode(this, textAlign, qrCodeAttributes, data));
    }

    private PrinterTextParserColumn appendQRCode(String textAlign, Hashtable<String, String> qrCodeAttributes, String data) throws EscPosParserException, EscPosBarcodeException {
        return this.appendElement(new PrinterTextParserQRCode(this, textAlign, qrCodeAttributes, data));
    }

    private PrinterTextParserColumn prependElement(IPrinterTextParserElement element) {
        IPrinterTextParserElement[] elementsTmp = new IPrinterTextParserElement[this.elements.length + 1];
        elementsTmp[0] = element;
        System.arraycopy(this.elements, 0, elementsTmp, 1, this.elements.length);
        this.elements = elementsTmp;
        return this;
    }

    private PrinterTextParserColumn appendElement(IPrinterTextParserElement element) {
        IPrinterTextParserElement[] elementsTmp = new IPrinterTextParserElement[this.elements.length + 1];
        System.arraycopy(this.elements, 0, elementsTmp, 0, this.elements.length);
        elementsTmp[this.elements.length] = element;
        this.elements = elementsTmp;
        return this;
    }

    public PrinterTextParserLine getLine() {
        return this.textParserLine;
    }

    public IPrinterTextParserElement[] getElements() {
        return this.elements;
    }
}
