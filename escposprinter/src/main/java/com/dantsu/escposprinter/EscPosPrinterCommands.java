package com.dantsu.escposprinter;

import android.graphics.Bitmap;

import java.io.UnsupportedEncodingException;
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

    public static final byte[] TEXT_ALIGN_LEFT = new byte[]{0x1B, 0x61, 0x00};
    public static final byte[] TEXT_ALIGN_CENTER = new byte[]{0x1B, 0x61, 0x01};
    public static final byte[] TEXT_ALIGN_RIGHT = new byte[]{0x1B, 0x61, 0x02};

    public static final byte[] TEXT_WEIGHT_NORMAL = new byte[]{0x1B, 0x45, 0x00};
    public static final byte[] TEXT_WEIGHT_BOLD = new byte[]{0x1B, 0x45, 0x01};

    public static final byte[] TEXT_SIZE_NORMAL = new byte[]{0x1B, 0x21, 0x03};
    public static final byte[] TEXT_SIZE_MEDIUM = new byte[]{0x1B, 0x21, 0x08};
    public static final byte[] TEXT_SIZE_DOUBLE_HEIGHT = new byte[]{0x1B, 0x21, 0x10};
    public static final byte[] TEXT_SIZE_DOUBLE_WIDTH = new byte[]{0x1B, 0x21, 0x20};
    public static final byte[] TEXT_SIZE_BIG = new byte[]{0x1B, 0x21, 0x30};

    public static final byte[] TEXT_UNDERLINE_OFF = new byte[]{0x1B, 0x2D, 0x00};
    public static final byte[] TEXT_UNDERLINE_ON = new byte[]{0x1B, 0x2D, 0x01};
    public static final byte[] TEXT_UNDERLINE_LARGE = new byte[]{0x1B, 0x2D, 0x02};

    public static final byte[] TEXT_DOUBLE_STRIKE_OFF = new byte[]{0x1B, 0x47, 0x00};
    public static final byte[] TEXT_DOUBLE_STRIKE_ON = new byte[]{0x1B, 0x47, 0x01};


    public static final int BARCODE_TYPE_UPCA = 65;
    public static final int BARCODE_TYPE_UPCE = 66;
    public static final int BARCODE_TYPE_EAN13 = 67;
    public static final int BARCODE_TYPE_EAN8 = 68;
    public static final int BARCODE_TYPE_ITF = 70;
    public static final int BARCODE_TYPE_128 = 73;

    public static final int BARCODE_TEXT_POSITION_NONE = 0;
    public static final int BARCODE_TEXT_POSITION_ABOVE = 1;
    public static final int BARCODE_TEXT_POSITION_BELOW = 2;


    public static final int QRCODE_1 = 49;
    public static final int QRCODE_2 = 50;

    private DeviceConnection printerConnection;
    private EscPosCharsetEncoding charsetEncoding;


    private static byte[] initImageCommand(int bytesByLine, int bitmapHeight) {
        byte[] imageBytes = new byte[8 + bytesByLine * bitmapHeight];
        System.arraycopy(new byte[]{0x1D, 0x76, 0x30, 0x00, (byte) bytesByLine, 0x00, (byte) bitmapHeight, 0x00}, 0, imageBytes, 0, 8);
        return imageBytes;
    }

    /**
     * Convert Bitmap instance to a byte array compatible with ESC/POS printer.
     *
     * @param bitmap Bitmap to be convert
     * @return Bytes contain the image in ESC/POS command
     */
    public static byte[] bitmapToBytes(Bitmap bitmap) {
        int
                bitmapWidth = bitmap.getWidth(),
                bitmapHeight = bitmap.getHeight(),
                bytesByLine = (int) Math.ceil(((float) bitmapWidth) / 8f);

        byte[] imageBytes = EscPosPrinterCommands.initImageCommand(bytesByLine, bitmapHeight);

        int i = 8;
        for (int posY = 0; posY < bitmapHeight; posY++) {
            for (int j = 0; j < bitmapWidth; j += 8) {
                StringBuilder stringBinary = new StringBuilder();
                for (int k = 0; k < 8; k++) {
                    int posX = j + k;
                    if (posX < bitmapWidth) {
                        int color = bitmap.getPixel(posX, posY),
                                r = (color >> 16) & 0xff,
                                g = (color >> 8) & 0xff,
                                b = color & 0xff;

                        if (r > 160 && g > 160 && b > 160) {
                            stringBinary.append("0");
                        } else {
                            stringBinary.append("1");
                        }
                    } else {
                        stringBinary.append("0");
                    }
                }
                imageBytes[i++] = (byte) Integer.parseInt(stringBinary.toString(), 2);
            }
        }

        return imageBytes;
    }

    /**
     * Convert a string to QR Code byte array compatible with ESC/POS printer.
     *
     * @param data String data to convert in QR Code
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
            return EscPosPrinterCommands.initImageCommand(0, 0);
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
            return EscPosPrinterCommands.initImageCommand(0, 0);
        }

        byte[] imageBytes = EscPosPrinterCommands.initImageCommand(bytesByLine, imageHeight);

        for (int y = 0; y < height; y++) {
            byte[] lineBytes = new byte[bytesByLine];
            int j = 0, multipleX = coefficient;
            boolean isBlack = false;
            for (int x = -1; x < width; ) {
                StringBuilder stringBinary = new StringBuilder();
                for (int k = 0; k < 8; k++) {
                    if (multipleX == coefficient) {
                        isBlack = ++x < width && byteMatrix.get(x, y) == 1;
                        multipleX = 0;
                    }
                    stringBinary.append(isBlack ? "1" : "0");
                    ++multipleX;
                }
                lineBytes[j++] = (byte) Integer.parseInt(stringBinary.toString(), 2);
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
        this(printerConnection, new EscPosCharsetEncoding("ISO-8859-1", 6));
    }

    /**
     * Create new instance of EscPosPrinterCommands.
     *
     * @param printerConnection an instance of a class which implement DeviceConnection
     * @param charsetEncoding   Set the charset encoding.
     */
    public EscPosPrinterCommands(DeviceConnection printerConnection, EscPosCharsetEncoding charsetEncoding) {
        this.printerConnection = printerConnection;
        this.charsetEncoding = charsetEncoding;
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
     * @param text     Text to be printed
     * @param textSize Set the text size. Use EscPosPrinterCommands.TEXT_SIZE_... constants
     * @param textBold Set the text weight. Use EscPosPrinterCommands.TEXT_WEIGHT_... constants
     * @return Fluent interface
     */
    public EscPosPrinterCommands printText(String text, byte[] textSize, byte[] textBold) throws EscPosEncodingException {
        return this.printText(text, textSize, textBold, null);
    }

    /**
     * Print text with the connected printer.
     *
     * @param text          Text to be printed
     * @param textSize      Set the text size. Use EscPosPrinterCommands.TEXT_SIZE_... constants
     * @param textBold      Set the text weight. Use EscPosPrinterCommands.TEXT_WEIGHT_... constants
     * @param textUnderline Set the underlining of the text. Use EscPosPrinterCommands.TEXT_UNDERLINE_... constants
     * @return Fluent interface
     */
    public EscPosPrinterCommands printText(String text, byte[] textSize, byte[] textBold, byte[] textUnderline) throws EscPosEncodingException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        try {
            byte[] textBytes = text.getBytes(this.charsetEncoding.getName());
            this.printerConnection.write(this.charsetEncoding.getCommand());
            this.printerConnection.write(EscPosPrinterCommands.TEXT_SIZE_NORMAL);
            this.printerConnection.write(EscPosPrinterCommands.TEXT_WEIGHT_NORMAL);
            this.printerConnection.write(EscPosPrinterCommands.TEXT_UNDERLINE_OFF);

            if (textSize != null) {
                this.printerConnection.write(textSize);
            }
            if (textBold != null) {
                this.printerConnection.write(textBold);
            }
            if (textUnderline != null) {
                this.printerConnection.write(textUnderline);
            }

            this.printerConnection.write(textBytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new EscPosEncodingException(e.getMessage());
        }

        return this;
    }

    /**
     * Print image with the connected printer.
     *
     * @param image Bytes contain the image in ESC/POS command
     * @return Fluent interface
     */
    public EscPosPrinterCommands printImage(byte[] image) {
        if (!this.printerConnection.isConnected()) {
            return this;
        }
        this.printerConnection.write(image);
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
            this.printerConnection.write(this.charsetEncoding.getCommand());
            byte[] textBytes = text.getBytes(this.charsetEncoding.getName());

            int
                    commandLength = textBytes.length + 3,
                    pL = commandLength % 256,
                    pH = (int) Math.floor(commandLength / 256);

            /*byte[] qrCodeCommand = new byte[textBytes.length + 7];
            System.arraycopy(new byte[]{0x1B, 0x5A, 0x00, 0x00, (byte)size, (byte)pL, (byte)pH}, 0, qrCodeCommand, 0, 7);
            System.arraycopy(textBytes, 0, qrCodeCommand, 7, textBytes.length);
            this.printerConnection.write(qrCodeCommand);*/

            this.printerConnection.write(new byte[]{0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, (byte) qrCodeType, 0x00});
            this.printerConnection.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, (byte) size});
            this.printerConnection.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, (byte) 48});

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

        this.printerConnection.write(new byte[]{0x1B, 0x4A, (byte) dots});
        this.printerConnection.send();
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
        this.printerConnection.send();
        return this;
    }

}
