package com.dantsu.escposprinter;

import android.graphics.Bitmap;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.EnumMap;

import com.dantsu.escposprinter.barcode.Barcode;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

public class EscPosPrinterCommands {

    public static final byte LF = 0x0A;

    public static final byte[] RESET_PRINTER = new byte[]{0x1B, 0x40};

    public static final byte[] TEXT_ALIGN_LEFT = new byte[]{0x1B, 0x61, 0x00};
    public static final byte[] TEXT_ALIGN_CENTER = new byte[]{0x1B, 0x61, 0x01};
    public static final byte[] TEXT_ALIGN_RIGHT = new byte[]{0x1B, 0x61, 0x02};

    public static final byte[] TEXT_WEIGHT_NORMAL = new byte[]{0x1B, 0x45, 0x00};
    public static final byte[] TEXT_WEIGHT_BOLD = new byte[]{0x1B, 0x45, 0x01};

    public static final byte[] LINE_SPACING_24 = {0x1b, 0x33, 0x18};
    public static final byte[] LINE_SPACING_30 = {0x1b, 0x33, 0x1e};

    public static final byte[] TEXT_FONT_A = new byte[]{0x1B, 0x4D, 0x00};
    public static final byte[] TEXT_FONT_B = new byte[]{0x1B, 0x4D, 0x01};
    public static final byte[] TEXT_FONT_C = new byte[]{0x1B, 0x4D, 0x02};
    public static final byte[] TEXT_FONT_D = new byte[]{0x1B, 0x4D, 0x03};
    public static final byte[] TEXT_FONT_E = new byte[]{0x1B, 0x4D, 0x04};

    public static final byte[] TEXT_SIZE_NORMAL = new byte[]{0x1D, 0x21, 0x00};
    public static final byte[] TEXT_SIZE_DOUBLE_HEIGHT = new byte[]{0x1D, 0x21, 0x01};
    public static final byte[] TEXT_SIZE_DOUBLE_WIDTH = new byte[]{0x1D, 0x21, 0x10};
    public static final byte[] TEXT_SIZE_BIG = new byte[]{0x1D, 0x21, 0x11};
    public static final byte[] TEXT_SIZE_BIG_2 = new byte[]{0x1D, 0x21, 0x22};
    public static final byte[] TEXT_SIZE_BIG_3 = new byte[]{0x1D, 0x21, 0x33};
    public static final byte[] TEXT_SIZE_BIG_4 = new byte[]{0x1D, 0x21, 0x44};
    public static final byte[] TEXT_SIZE_BIG_5 = new byte[]{0x1D, 0x21, 0x55};
    public static final byte[] TEXT_SIZE_BIG_6 = new byte[]{0x1D, 0x21, 0x66};

    public static final byte[] TEXT_UNDERLINE_OFF = new byte[]{0x1B, 0x2D, 0x00};
    public static final byte[] TEXT_UNDERLINE_ON = new byte[]{0x1B, 0x2D, 0x01};
    public static final byte[] TEXT_UNDERLINE_LARGE = new byte[]{0x1B, 0x2D, 0x02};

    public static final byte[] TEXT_DOUBLE_STRIKE_OFF = new byte[]{0x1B, 0x47, 0x00};
    public static final byte[] TEXT_DOUBLE_STRIKE_ON = new byte[]{0x1B, 0x47, 0x01};

    public static final byte[] TEXT_COLOR_BLACK = new byte[]{0x1B, 0x72, 0x00};
    public static final byte[] TEXT_COLOR_RED = new byte[]{0x1B, 0x72, 0x01};

    public static final byte[] TEXT_COLOR_REVERSE_OFF = new byte[]{0x1D, 0x42, 0x00};
    public static final byte[] TEXT_COLOR_REVERSE_ON = new byte[]{0x1D, 0x42, 0x01};


    public static final int BARCODE_TYPE_UPCA = 65;
    public static final int BARCODE_TYPE_UPCE = 66;
    public static final int BARCODE_TYPE_EAN13 = 67;
    public static final int BARCODE_TYPE_EAN8 = 68;
    public static final int BARCODE_TYPE_39 = 69;
    public static final int BARCODE_TYPE_ITF = 70;
    public static final int BARCODE_TYPE_128 = 73;

    public static final int BARCODE_TEXT_POSITION_NONE = 0;
    public static final int BARCODE_TEXT_POSITION_ABOVE = 1;
    public static final int BARCODE_TEXT_POSITION_BELOW = 2;

    public static final int QRCODE_1 = 49;
    public static final int QRCODE_2 = 50;

    private DeviceConnection printerConnection;
    private EscPosCharsetEncoding charsetEncoding;
    private boolean useEscAsteriskCommand;


    public static byte[] initGSv0Command(int bytesByLine, int bitmapHeight) {
        int
            xH = bytesByLine / 256,
            xL = bytesByLine - (xH * 256),
            yH = bitmapHeight / 256,
            yL = bitmapHeight - (yH * 256);

        byte[] imageBytes = new byte[8 + bytesByLine * bitmapHeight];
        imageBytes[0] = 0x1D;
        imageBytes[1] = 0x76;
        imageBytes[2] = 0x30;
        imageBytes[3] = 0x00;
        imageBytes[4] = (byte) xL;
        imageBytes[5] = (byte) xH;
        imageBytes[6] = (byte) yL;
        imageBytes[7] = (byte) yH;
        return imageBytes;
    }

    /**
     * Convert Bitmap instance to a byte array compatible with ESC/POS printer.
     *
     * @param bitmap Bitmap to be convert
     * @param gradient false : Black and white image, true : Grayscale image
     * @return Bytes contain the image in ESC/POS command
     */
    public static byte[] bitmapToBytes(Bitmap bitmap, boolean gradient) {
        int
            bitmapWidth = bitmap.getWidth(),
            bitmapHeight = bitmap.getHeight(),
            bytesByLine = (int) Math.ceil(((float) bitmapWidth) / 8f);

        byte[] imageBytes = EscPosPrinterCommands.initGSv0Command(bytesByLine, bitmapHeight);

        int i = 8,
            greyscaleCoefficientInit = 0,
            gradientStep = 6;

        double
            colorLevelStep = 765.0 / (15 * gradientStep + gradientStep - 1);

        for (int posY = 0; posY < bitmapHeight; posY++) {
            int greyscaleCoefficient = greyscaleCoefficientInit,
                greyscaleLine = posY % gradientStep;
            for (int j = 0; j < bitmapWidth; j += 8) {
                int b = 0;
                for (int k = 0; k < 8; k++) {
                    int posX = j + k;
                    if (posX < bitmapWidth) {
                        int color = bitmap.getPixel(posX, posY),
                            red = (color >> 16) & 255,
                            green = (color >> 8) & 255,
                            blue = color & 255;

                        if (
                            (gradient && (red + green + blue) < ((greyscaleCoefficient * gradientStep + greyscaleLine) * colorLevelStep)) ||
                                (!gradient && (red < 160 || green < 160 || blue < 160))
                        ) {
                            b |= 1 << (7 - k);
                        }

                        greyscaleCoefficient += 5;
                        if (greyscaleCoefficient > 15) {
                            greyscaleCoefficient -= 16;
                        }
                    }
                }
                imageBytes[i++] = (byte) b;
            }

            greyscaleCoefficientInit += 2;
            if (greyscaleCoefficientInit > 15) {
                greyscaleCoefficientInit = 0;
            }
        }

        return imageBytes;
    }

    public static byte[][] convertGSv0ToEscAsterisk(byte[] bytes) {
        int
            xL = bytes[4] & 0xFF,
            xH = bytes[5] & 0xFF,
            yL = bytes[6] & 0xFF,
            yH = bytes[7] & 0xFF,
            bytesByLine = xH * 256 + xL,
            dotsByLine = bytesByLine * 8,
            nH = dotsByLine / 256,
            nL = dotsByLine % 256,
            imageHeight = yH * 256 + yL,
            imageLineHeightCount = (int) Math.ceil((double) imageHeight / 24.0),
            imageBytesSize = 6 + bytesByLine * 24;

        byte[][] returnedBytes = new byte[imageLineHeightCount + 2][];
        returnedBytes[0] = EscPosPrinterCommands.LINE_SPACING_24;
        for (int i = 0; i < imageLineHeightCount; ++i) {
            int pxBaseRow = i * 24;
            byte[] imageBytes = new byte[imageBytesSize];
            imageBytes[0] = 0x1B;
            imageBytes[1] = 0x2A;
            imageBytes[2] = 0x21;
            imageBytes[3] = (byte) nL;
            imageBytes[4] = (byte) nH;
            for (int j = 5; j < imageBytes.length; ++j) {
                int
                    imgByte = j - 5,
                    byteRow = imgByte % 3,
                    pxColumn = imgByte / 3,
                    bitColumn = 1 << (7 - pxColumn % 8),
                    pxRow = pxBaseRow + byteRow * 8;
                for (int k = 0; k < 8; ++k) {
                    int indexBytes = bytesByLine * (pxRow + k) + pxColumn / 8 + 8;

                    if (indexBytes >= bytes.length) {
                        break;
                    }

                    boolean isBlack = (bytes[indexBytes] & bitColumn) == bitColumn;
                    if (isBlack) {
                        imageBytes[j] |= 1 << 7 - k;
                    }
                }
            }
            imageBytes[imageBytes.length - 1] = EscPosPrinterCommands.LF;
            returnedBytes[i + 1] = imageBytes;
        }
        returnedBytes[returnedBytes.length - 1] = EscPosPrinterCommands.LINE_SPACING_30;
        return returnedBytes;
    }

    /**
     * Convert a string to QR Code byte array compatible with ESC/POS printer.
     *
     * @param data String data to convert in QR Code
     * @param size QR code dots size
     * @return Bytes contain the image in ESC/POS command
     */
    public static byte[] QRCodeDataToBytes(String data, int size) throws EscPosBarcodeException {

        ByteMatrix byteMatrix = null;

        try {
            EnumMap<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            QRCode code = Encoder.encode(data, ErrorCorrectionLevel.L, hints);
            byteMatrix = code.getMatrix();

        } catch (WriterException e) {
            e.printStackTrace();
            throw new EscPosBarcodeException("Unable to encode QR code");
        }

        if (byteMatrix == null) {
            return EscPosPrinterCommands.initGSv0Command(0, 0);
        }

        int
            width = byteMatrix.getWidth(),
            height = byteMatrix.getHeight(),
            coefficient = Math.round((float) size / (float) width),
            imageWidth = width * coefficient,
            imageHeight = height * coefficient,
            bytesByLine = (int) Math.ceil(((float) imageWidth) / 8f),
            i = 8;

        if (coefficient < 1) {
            return EscPosPrinterCommands.initGSv0Command(0, 0);
        }

        byte[] imageBytes = EscPosPrinterCommands.initGSv0Command(bytesByLine, imageHeight);

        for (int y = 0; y < height; y++) {
            byte[] lineBytes = new byte[bytesByLine];
            int x = -1, multipleX = coefficient;
            boolean isBlack = false;
            for (int j = 0; j < bytesByLine; j++) {
                int b = 0;
                for (int k = 0; k < 8; k++) {
                    if (multipleX == coefficient) {
                        isBlack = ++x < width && byteMatrix.get(x, y) == 1;
                        multipleX = 0;
                    }
                    if (isBlack) {
                        b |= 1 << (7 - k);
                    }
                    ++multipleX;
                }
                lineBytes[j] = (byte) b;
            }

            for (int multipleY = 0; multipleY < coefficient; ++multipleY) {
                System.arraycopy(lineBytes, 0, imageBytes, i, lineBytes.length);
                i += lineBytes.length;
            }
        }

        return imageBytes;
    }

    /**
     * Create new instance of EscPosPrinterCommands.
     *
     * @param printerConnection an instance of a class which implement DeviceConnection
     */
    public EscPosPrinterCommands(DeviceConnection printerConnection) {
        this(printerConnection, null);
    }

    /**
     * Create new instance of EscPosPrinterCommands.
     *
     * @param printerConnection an instance of a class which implement DeviceConnection
     * @param charsetEncoding   Set the charset encoding.
     */
    public EscPosPrinterCommands(DeviceConnection printerConnection, EscPosCharsetEncoding charsetEncoding) {
        this.printerConnection = printerConnection;
        this.charsetEncoding = charsetEncoding != null ? charsetEncoding : new EscPosCharsetEncoding("windows-1252", 6);
    }

    /**
     * Start socket connection and open stream with the device.
     */
    public EscPosPrinterCommands connect() throws EscPosConnectionException {
        this.printerConnection.connect();
        return this;
    }

    /**
     * Close the socket connection and stream with the device.
     */
    public void disconnect() {
        this.printerConnection.disconnect();
    }

    /**
     * Reset printers parameters.
     */
    public EscPosPrinterCommands reset() {
        if (!this.printerConnection.isConnected()) {
            return this;
        }
        this.printerConnection.write(EscPosPrinterCommands.RESET_PRINTER);
        return this;
    }

    /**
     * Set the alignment of text and barcodes.
     * Don't works with image.
     *
     * @param align Set the alignment of text and barcodes. Use EscPosPrinterCommands.TEXT_ALIGN_... constants
     * @return Fluent interface
     */
    public EscPosPrinterCommands setAlign(byte[] align) {
        if (!this.printerConnection.isConnected()) {
            return this;
        }
        this.printerConnection.write(align);
        return this;
    }

    /**
     * Print text with the connected printer.
     *
     * @param text Text to be printed
     * @return Fluent interface
     */
    public EscPosPrinterCommands printText(String text) throws EscPosEncodingException {
        return this.printText(text, null);
    }

    /**
     * Print text with the connected printer.
     *
     * @param text     Text to be printed
     * @param textSize Set the text size. Use EscPosPrinterCommands.TEXT_SIZE_... constants
     * @return Fluent interface
     */
    public EscPosPrinterCommands printText(String text, byte[] textSize) throws EscPosEncodingException {
        return this.printText(text, textSize, null);
    }

    /**
     * Print text with the connected printer.
     *
     * @param text      Text to be printed
     * @param textSize  Set the text size. Use EscPosPrinterCommands.TEXT_SIZE_... constants
     * @param textColor Set the text color. Use EscPosPrinterCommands.TEXT_COLOR_... constants
     * @return Fluent interface
     */
    public EscPosPrinterCommands printText(String text, byte[] textSize, byte[] textColor) throws EscPosEncodingException {
        return this.printText(text, textSize, textColor, null);
    }

    /**
     * Print text with the connected printer.
     *
     * @param text             Text to be printed
     * @param textSize         Set the text size. Use EscPosPrinterCommands.TEXT_SIZE_... constants
     * @param textColor        Set the text color. Use EscPosPrinterCommands.TEXT_COLOR_... constants
     * @param textReverseColor Set the background and text color. Use EscPosPrinterCommands.TEXT_COLOR_REVERSE_... constants
     * @return Fluent interface
     */
    public EscPosPrinterCommands printText(String text, byte[] textSize, byte[] textColor, byte[] textReverseColor) throws EscPosEncodingException {
        return this.printText(text, textSize, textColor, textReverseColor, null);
    }

    /**
     * Print text with the connected printer.
     *
     * @param text             Text to be printed
     * @param textSize         Set the text size. Use EscPosPrinterCommands.TEXT_SIZE_... constants
     * @param textColor        Set the text color. Use EscPosPrinterCommands.TEXT_COLOR_... constants
     * @param textReverseColor Set the background and text color. Use EscPosPrinterCommands.TEXT_COLOR_REVERSE_... constants
     * @param textBold         Set the text weight. Use EscPosPrinterCommands.TEXT_WEIGHT_... constants
     * @return Fluent interface
     */
    public EscPosPrinterCommands printText(String text, byte[] textSize, byte[] textColor, byte[] textReverseColor, byte[] textBold) throws EscPosEncodingException {
        return this.printText(text, textSize, textColor, textReverseColor, textBold, null);
    }

    /**
     * Print text with the connected printer.
     *
     * @param text             Text to be printed
     * @param textSize         Set the text size. Use EscPosPrinterCommands.TEXT_SIZE_... constants
     * @param textColor        Set the text color. Use EscPosPrinterCommands.TEXT_COLOR_... constants
     * @param textReverseColor Set the background and text color. Use EscPosPrinterCommands.TEXT_COLOR_REVERSE_... constants
     * @param textBold         Set the text weight. Use EscPosPrinterCommands.TEXT_WEIGHT_... constants
     * @param textUnderline    Set the underlining of the text. Use EscPosPrinterCommands.TEXT_UNDERLINE_... constants
     * @return Fluent interface
     */
    public EscPosPrinterCommands printText(String text, byte[] textSize, byte[] textColor, byte[] textReverseColor, byte[] textBold, byte[] textUnderline) throws EscPosEncodingException {
        return this.printText(text, textSize, textColor, textReverseColor, textBold, textUnderline, null);
    }


    private byte[] currentTextSize = new byte[0];
    private byte[] currentTextColor = new byte[0];
    private byte[] currentTextReverseColor = new byte[0];
    private byte[] currentTextBold = new byte[0];
    private byte[] currentTextUnderline = new byte[0];
    private byte[] currentTextDoubleStrike = new byte[0];

    /**
     * Print text with the connected printer.
     *
     * @param text             Text to be printed
     * @param textSize         Set the text size. Use EscPosPrinterCommands.TEXT_SIZE_... constants
     * @param textColor        Set the text color. Use EscPosPrinterCommands.TEXT_COLOR_... constants
     * @param textReverseColor Set the background and text color. Use EscPosPrinterCommands.TEXT_COLOR_REVERSE_... constants
     * @param textBold         Set the text weight. Use EscPosPrinterCommands.TEXT_WEIGHT_... constants
     * @param textUnderline    Set the underlining of the text. Use EscPosPrinterCommands.TEXT_UNDERLINE_... constants
     * @param textDoubleStrike Set the double strike of the text. Use EscPosPrinterCommands.TEXT_DOUBLE_STRIKE_... constants
     * @return Fluent interface
     */
    public EscPosPrinterCommands printText(String text, byte[] textSize, byte[] textColor, byte[] textReverseColor, byte[] textBold, byte[] textUnderline, byte[] textDoubleStrike) throws EscPosEncodingException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        if (textSize == null) {
            textSize = EscPosPrinterCommands.TEXT_SIZE_NORMAL;
        }
        if (textColor == null) {
            textColor = EscPosPrinterCommands.TEXT_COLOR_BLACK;
        }
        if (textReverseColor == null) {
            textReverseColor = EscPosPrinterCommands.TEXT_COLOR_REVERSE_OFF;
        }
        if (textBold == null) {
            textBold = EscPosPrinterCommands.TEXT_WEIGHT_NORMAL;
        }
        if (textUnderline == null) {
            textUnderline = EscPosPrinterCommands.TEXT_UNDERLINE_OFF;
        }
        if (textDoubleStrike == null) {
            textDoubleStrike = EscPosPrinterCommands.TEXT_DOUBLE_STRIKE_OFF;
        }

        try {
            byte[] textBytes = text.getBytes(this.charsetEncoding.getName());
            this.printerConnection.write(this.charsetEncoding.getCommand());
            //this.printerConnection.write(EscPosPrinterCommands.TEXT_FONT_A);


            if (!Arrays.equals(this.currentTextSize, textSize)) {
                this.printerConnection.write(textSize);
                this.currentTextSize = textSize;
            }

            if (!Arrays.equals(this.currentTextDoubleStrike, textDoubleStrike)) {
                this.printerConnection.write(textDoubleStrike);
                this.currentTextDoubleStrike = textDoubleStrike;
            }

            if (!Arrays.equals(this.currentTextUnderline, textUnderline)) {
                this.printerConnection.write(textUnderline);
                this.currentTextUnderline = textUnderline;
            }

            if (!Arrays.equals(this.currentTextBold, textBold)) {
                this.printerConnection.write(textBold);
                this.currentTextBold = textBold;
            }

            if (!Arrays.equals(this.currentTextColor, textColor)) {
                this.printerConnection.write(textColor);
                this.currentTextColor = textColor;
            }

            if (!Arrays.equals(this.currentTextReverseColor, textReverseColor)) {
                this.printerConnection.write(textReverseColor);
                this.currentTextReverseColor = textReverseColor;
            }

            this.printerConnection.write(textBytes);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new EscPosEncodingException(e.getMessage());
        }

        return this;
    }

    public EscPosPrinterCommands printAllCharsetsEncodingCharacters() {
        for (int charsetId = 0; charsetId < 256; ++charsetId) {
            this.printCharsetEncodingCharacters(charsetId);
        }
        return this;
    }

    public EscPosPrinterCommands printCharsetsEncodingCharacters(int[] charsetsId) {
        for (int charsetId : charsetsId) {
            this.printCharsetEncodingCharacters(charsetId);
        }
        return this;
    }

    public EscPosPrinterCommands printCharsetEncodingCharacters(int charsetId) {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        try {
            this.printerConnection.write(new byte[]{0x1B, 0x74, (byte) charsetId});
            this.printerConnection.write(EscPosPrinterCommands.TEXT_SIZE_NORMAL);
            this.printerConnection.write(EscPosPrinterCommands.TEXT_COLOR_BLACK);
            this.printerConnection.write(EscPosPrinterCommands.TEXT_COLOR_REVERSE_OFF);
            this.printerConnection.write(EscPosPrinterCommands.TEXT_WEIGHT_NORMAL);
            this.printerConnection.write(EscPosPrinterCommands.TEXT_UNDERLINE_OFF);
            this.printerConnection.write(EscPosPrinterCommands.TEXT_DOUBLE_STRIKE_OFF);
            this.printerConnection.write((":::: Charset n°" + charsetId + " : ").getBytes());
            this.printerConnection.write(new byte[]{
                (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F,
                (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1A, (byte) 0x1B, (byte) 0x1C, (byte) 0x1D, (byte) 0x1E, (byte) 0x1F,
                (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2A, (byte) 0x2B, (byte) 0x2C, (byte) 0x2D, (byte) 0x2E, (byte) 0x2F,
                (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39, (byte) 0x3A, (byte) 0x3B, (byte) 0x3C, (byte) 0x3D, (byte) 0x3E, (byte) 0x3F,
                (byte) 0x40, (byte) 0x41, (byte) 0x42, (byte) 0x43, (byte) 0x44, (byte) 0x45, (byte) 0x46, (byte) 0x47, (byte) 0x48, (byte) 0x49, (byte) 0x4A, (byte) 0x4B, (byte) 0x4C, (byte) 0x4D, (byte) 0x4E, (byte) 0x4F,
                (byte) 0x50, (byte) 0x51, (byte) 0x52, (byte) 0x53, (byte) 0x54, (byte) 0x55, (byte) 0x56, (byte) 0x57, (byte) 0x58, (byte) 0x59, (byte) 0x5A, (byte) 0x5B, (byte) 0x5C, (byte) 0x5D, (byte) 0x5E, (byte) 0x5F,
                (byte) 0x60, (byte) 0x61, (byte) 0x62, (byte) 0x63, (byte) 0x64, (byte) 0x65, (byte) 0x66, (byte) 0x67, (byte) 0x68, (byte) 0x69, (byte) 0x6A, (byte) 0x6B, (byte) 0x6C, (byte) 0x6D, (byte) 0x6E, (byte) 0x6F,
                (byte) 0x70, (byte) 0x71, (byte) 0x72, (byte) 0x73, (byte) 0x74, (byte) 0x75, (byte) 0x76, (byte) 0x77, (byte) 0x78, (byte) 0x79, (byte) 0x7A, (byte) 0x7B, (byte) 0x7C, (byte) 0x7D, (byte) 0x7E, (byte) 0x7F,
                (byte) 0x80, (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85, (byte) 0x86, (byte) 0x87, (byte) 0x88, (byte) 0x89, (byte) 0x8A, (byte) 0x8B, (byte) 0x8C, (byte) 0x8D, (byte) 0x8E, (byte) 0x8F,
                (byte) 0x90, (byte) 0x91, (byte) 0x92, (byte) 0x93, (byte) 0x94, (byte) 0x95, (byte) 0x96, (byte) 0x97, (byte) 0x98, (byte) 0x99, (byte) 0x9A, (byte) 0x9B, (byte) 0x9C, (byte) 0x9D, (byte) 0x9E, (byte) 0x9F,
                (byte) 0xA0, (byte) 0xA1, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4, (byte) 0xA5, (byte) 0xA6, (byte) 0xA7, (byte) 0xA8, (byte) 0xA9, (byte) 0xAA, (byte) 0xAB, (byte) 0xAC, (byte) 0xAD, (byte) 0xAE, (byte) 0xAF,
                (byte) 0xB0, (byte) 0xB1, (byte) 0xB2, (byte) 0xB3, (byte) 0xB4, (byte) 0xB5, (byte) 0xB6, (byte) 0xB7, (byte) 0xB8, (byte) 0xB9, (byte) 0xBA, (byte) 0xBB, (byte) 0xBC, (byte) 0xBD, (byte) 0xBE, (byte) 0xBF,
                (byte) 0xC0, (byte) 0xC1, (byte) 0xC2, (byte) 0xC3, (byte) 0xC4, (byte) 0xC5, (byte) 0xC6, (byte) 0xC7, (byte) 0xC8, (byte) 0xC9, (byte) 0xCA, (byte) 0xCB, (byte) 0xCC, (byte) 0xCD, (byte) 0xCE, (byte) 0xCF,
                (byte) 0xD0, (byte) 0xD1, (byte) 0xD2, (byte) 0xD3, (byte) 0xD4, (byte) 0xD5, (byte) 0xD6, (byte) 0xD7, (byte) 0xD8, (byte) 0xD9, (byte) 0xDA, (byte) 0xDB, (byte) 0xDC, (byte) 0xDD, (byte) 0xDE, (byte) 0xDF,
                (byte) 0xE0, (byte) 0xE1, (byte) 0xE2, (byte) 0xE3, (byte) 0xE4, (byte) 0xE5, (byte) 0xE6, (byte) 0xE7, (byte) 0xE8, (byte) 0xE9, (byte) 0xEA, (byte) 0xEB, (byte) 0xEC, (byte) 0xED, (byte) 0xEE, (byte) 0xEF,
                (byte) 0xF0, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3, (byte) 0xF4, (byte) 0xF5, (byte) 0xF6, (byte) 0xF7, (byte) 0xF8, (byte) 0xF9, (byte) 0xFA, (byte) 0xFB, (byte) 0xFC, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF
            });
            this.printerConnection.write(new byte[]{EscPosPrinterCommands.LF, EscPosPrinterCommands.LF, EscPosPrinterCommands.LF, EscPosPrinterCommands.LF});
            this.printerConnection.send();
        } catch (EscPosConnectionException e) {
            e.printStackTrace();
        }
        return this;
    }


    /**
     * Active "ESC *" command for image print.
     *
     * @param enable true to use "ESC *", false to use "GS v 0"
     * @return Fluent interface
     */
    public EscPosPrinterCommands useEscAsteriskCommand(boolean enable) {
        this.useEscAsteriskCommand = enable;
        return this;
    }

    /**
     * Print image with the connected printer.
     *
     * @param image Bytes contain the image in ESC/POS command
     * @return Fluent interface
     */
    public EscPosPrinterCommands printImage(byte[] image) throws EscPosConnectionException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        byte[][] bytesToPrint = this.useEscAsteriskCommand ? EscPosPrinterCommands.convertGSv0ToEscAsterisk(image) : new byte[][]{image};

        for (byte[] bytes : bytesToPrint) {
            this.printerConnection.write(bytes);
            this.printerConnection.send();
        }

        return this;
    }

    /**
     * Print a barcode with the connected printer.
     *
     * @param barcode Instance of Class that implement Barcode
     * @return Fluent interface
     */
    public EscPosPrinterCommands printBarcode(Barcode barcode) {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        String code = barcode.getCode();
        int barcodeLength = barcode.getCodeLength();
        byte[] barcodeCommand = new byte[barcodeLength + 4];
        System.arraycopy(new byte[]{0x1D, 0x6B, (byte) barcode.getBarcodeType(), (byte) barcodeLength}, 0, barcodeCommand, 0, 4);

        for (int i = 0; i < barcodeLength; i++) {
            barcodeCommand[i + 4] = (byte) code.charAt(i);
        }

        this.printerConnection.write(new byte[]{0x1D, 0x48, (byte) barcode.getTextPosition()});
        this.printerConnection.write(new byte[]{0x1D, 0x77, (byte) barcode.getColWidth()});
        this.printerConnection.write(new byte[]{0x1D, 0x68, (byte) barcode.getHeight()});
        this.printerConnection.write(barcodeCommand);
        return this;
    }


    /**
     * Print a QR code with the connected printer.
     *
     * @param qrCodeType Set the barcode type. Use EscPosPrinterCommands.QRCODE_... constants
     * @param text       String that contains QR code data
     * @param size       dot size of QR code pixel
     * @return Fluent interface
     */
    public EscPosPrinterCommands printQRCode(int qrCodeType, String text, int size) throws EscPosEncodingException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        if (size < 1) {
            size = 1;
        } else if (size > 16) {
            size = 16;
        }


        try {
            byte[] textBytes = text.getBytes("UTF-8");

            int
                commandLength = textBytes.length + 3,
                pL = commandLength % 256,
                pH = commandLength / 256;

            /*byte[] qrCodeCommand = new byte[textBytes.length + 7];
            System.arraycopy(new byte[]{0x1B, 0x5A, 0x00, 0x00, (byte)size, (byte)pL, (byte)pH}, 0, qrCodeCommand, 0, 7);
            System.arraycopy(textBytes, 0, qrCodeCommand, 7, textBytes.length);
            this.printerConnection.write(qrCodeCommand);*/

            this.printerConnection.write(new byte[]{0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, (byte) qrCodeType, 0x00});
            this.printerConnection.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, (byte) size});
            this.printerConnection.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x30});

            byte[] qrCodeCommand = new byte[textBytes.length + 8];
            System.arraycopy(new byte[]{0x1D, 0x28, 0x6B, (byte) pL, (byte) pH, 0x31, 0x50, 0x30}, 0, qrCodeCommand, 0, 8);
            System.arraycopy(textBytes, 0, qrCodeCommand, 8, textBytes.length);
            this.printerConnection.write(qrCodeCommand);
            this.printerConnection.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30});
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new EscPosEncodingException(e.getMessage());
        }
        return this;
    }

    /**
     * Forces the transition to a new line with the connected printer.
     *
     * @return Fluent interface
     */
    public EscPosPrinterCommands newLine() throws EscPosConnectionException {
        return this.newLine(null);
    }

    /**
     * Forces the transition to a new line and set the alignment of text and barcodes with the connected printer.
     *
     * @param align Set the alignment of text and barcodes. Use EscPosPrinterCommands.TEXT_ALIGN_... constants
     * @return Fluent interface
     */
    public EscPosPrinterCommands newLine(byte[] align) throws EscPosConnectionException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        this.printerConnection.write(new byte[]{EscPosPrinterCommands.LF});
        this.printerConnection.send();

        if (align != null) {
            this.printerConnection.write(align);
        }
        return this;
    }

    /**
     * Feed the paper
     *
     * @param dots Number of dots to feed (0 <= dots <= 255)
     * @return Fluent interface
     */
    public EscPosPrinterCommands feedPaper(int dots) throws EscPosConnectionException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        if (dots > 0) {
            this.printerConnection.write(new byte[]{0x1B, 0x4A, (byte) dots});
            this.printerConnection.send(dots);
        }

        return this;
    }

    /**
     * Cut the paper
     *
     * @return Fluent interface
     */
    public EscPosPrinterCommands cutPaper() throws EscPosConnectionException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        this.printerConnection.write(new byte[]{0x1D, 0x56, 0x01});
        this.printerConnection.send(100);
        return this;
    }

    /**
     * Open the cash box
     *
     * @return Fluent interface
     */
    public EscPosPrinterCommands openCashBox() throws EscPosConnectionException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        this.printerConnection.write(new byte[]{0x1B, 0x70, 0x00, 0x3C, (byte) 0xFF});
        this.printerConnection.send(100);
        return this;
    }

    /**
     * @return Charset encoding
     */
    public EscPosCharsetEncoding getCharsetEncoding() {
        return this.charsetEncoding;
    }
}
