[![](https://jitpack.io/v/DantSu/ESCPOS-ThermalPrinter-Android.svg)](https://jitpack.io/#DantSu/ESCPOS-ThermalPrinter-Android/1.0.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# Android library for Thermal Printer ESC/POS Command

Useful library to help Android developers to print with (Bluetooth, TCP, USB) thermal printers ESC/POS.


## Table of contents

- [Android version](#android-version)
- [Tested printers](#tested-printers)
- [Installation](#installation)
- [Bluetooth](#bluetooth)
  - [Bluetooth permission](#bluetooth-permission)
  - [Bluetooth code example](#bluetooth-code-example)
- [TCP](#tcp)
  - [TCP permission](#tcp-permission)
  - [TCP code example](#tcp-code-example)
- [USB](#usb)
  - [USB permission](#usb-permission)
  - [USB code example](#usb-code-example)
- [Formatted text : syntax guide](#formatted-text--syntax-guide)
- [Class list](#class-list)
  - [BluetoothPrintersConnections](#user-content-class--comdantsuescposprinterconnectionbluetoothbluetoothprintersconnections)
  - [UsbPrintersConnections](#user-content-class--comdantsuescposprinterconnectionusbusbprintersconnections)
  - [EscPosPrinter](#user-content-class--comdantsuescposprinterescposprinter)
  - [PrinterTextParserImg](#user-content-class--comdantsuescposprintertextparserprintertextparserimg)
- [Contributing](#contributing)


## Android version

Developed for SDK version 16 (Android 4.1 Jelly Bean) and above.


## Tested printers

1. [HOIN HOP H58 Thermal Printer ESC/POS](https://www.gearbest.com/printers/pp_662658.html).
2. [XPRINTER XP-P300](https://xprinter.vn/xprinter-xp-p300-may-in-hoa-don-di-dong-bluetooth/).
3. [MUNBYN IMP001](https://www.munbyn.com/collections/portable-receipt-printer/products/58mm-bluetooth-thermal-printer-imp001).


## Installation

**Step 1.** Add the [JitPack](https://jitpack.io/#DantSu/ESCPOS-ThermalPrinter-Android/1.1.2) repository to your build file. Add it in your root `/build.gradle` at the end of repositories:

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

**Step 2.** Add the dependency in `/app/build.gradle` :

```
dependencies {
    ...
    implementation 'com.github.DantSu:ESCPOS-ThermalPrinter-Android:1.0.0'
}
```

## Bluetooth

### Bluetooth permission

Be sure to have `<uses-permission android:name="android.permission.BLUETOOTH" />` in your `AndroidMenifest.xml`.

Also, you have to check the bluetooth permission in your app like this :

```java
if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, MainActivity.PERMISSION_BLUETOOTH);
} else {
    // Your code HERE
}
```

### Bluetooth code example

The code below is an example to write in your activity :

```java
EscPosPrinter printer = new EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 48f, 32);
printer
    .printFormattedText(
        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.logo, DisplayMetrics.DENSITY_MEDIUM))+"</img>\n" +
        "[L]\n" +
        "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
        "[L]\n" +
        "[C]================================\n" +
        "[L]\n" +
        "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
        "[L]  + Size : S\n" +
        "[L]\n" +
        "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
        "[L]  + Size : 57/58\n" +
        "[L]\n" +
        "[C]--------------------------------\n" +
        "[L][R]TOTAL PRICE :[R]34.98e\n" +
        "[L][R]TAX :[R]4.23e\n" +
        "[L]\n" +
        "[C]================================\n" +
        "[L]\n" +
        "[L]<font size='tall'>Customer :</font>\n" +
        "[L]Raymond DUPONT\n" +
        "[L]5 rue des girafes\n" +
        "[L]31547 PERPETES\n" +
        "[L]Tel : +33801201456\n" +
        "[L]\n" +
        "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
        "[C]<qrcode size='20'>http://www.developpeur-web.dantsu.com/</qrcode>"
    );
```

Below a picture of the receipt printed with the code above :

![Example of a printed receipt](http://www.developpeur-web.dantsu.com/files/librairie/receipt-thermal-printer.png?1)

## TCP

### TCP permission

Be sure to have `<uses-permission android:name="android.permission.INTERNET"/>` in your `AndroidMenifest.xml`.

### TCP code example

The code below is an example to write in your activity :

```java
final String hexImage = PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.logo, DisplayMetrics.DENSITY_MEDIUM));
new Thread(new Runnable() {
    public void run() {
        try {
            EscPosPrinter printer = new EscPosPrinter(new TcpConnection("192.168.1.3", 9300), 203, 48f, 32);
            printer
                .printFormattedText(
                    "[C]<img>" + +"</img>\n" +
                    "[L]\n" +
                    "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
                    "[L]\n" +
                    "[C]================================\n" +
                    "[L]\n" +
                    "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
                    "[L]  + Size : S\n" +
                    "[L]\n" +
                    "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
                    "[L]  + Size : 57/58\n" +
                    "[L]\n" +
                    "[C]--------------------------------\n" +
                    "[L][R]TOTAL PRICE :[R]34.98e\n" +
                    "[L][R]TAX :[R]4.23e\n" +
                    "[L]\n" +
                    "[C]================================\n" +
                    "[L]\n" +
                    "[L]<font size='tall'>Customer :</font>\n" +
                    "[L]Raymond DUPONT\n" +
                    "[L]5 rue des girafes\n" +
                    "[L]31547 PERPETES\n" +
                    "[L]Tel : +33801201456\n" +
                    "[L]\n" +
                    "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
                    "[C]<qrcode size='20'>http://www.developpeur-web.dantsu.com/</qrcode>"
                );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}).start();
```

## USB

### USB permission

You have to check the USB permission in your app like this :

```java
private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (MainActivity.ACTION_USB_PERMISSION.equals(action)) {
            synchronized (this) {
                UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (usbManager != null && usbDevice != null) {
                        // YOUR PRINT CODE HERE
                    }
                }
            }
        }
    }
};

public void printUsb() {
    UsbConnection usbConnection = UsbPrintersConnections.selectFirstConnected(this);
    UsbManager usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
    if (usbConnection != null && usbManager != null) {
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(MainActivity.ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_USB_PERMISSION);
        registerReceiver(this.usbReceiver, filter);
        usbManager.requestPermission(usbConnection.getDevice(), permissionIntent);
    }
}
```

### USB code example

The code below is an example to write in your activity :

```java
EscPosPrinter printer = new EscPosPrinter(new UsbConnection(usbManager, usbDevice), 203, 48f, 32);
printer
    .printFormattedText(
        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.logo, DisplayMetrics.DENSITY_MEDIUM))+"</img>\n" +
        "[L]\n" +
        "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
        "[L]\n" +
        "[C]================================\n" +
        "[L]\n" +
        "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
        "[L]  + Size : S\n" +
        "[L]\n" +
        "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
        "[L]  + Size : 57/58\n" +
        "[L]\n" +
        "[C]--------------------------------\n" +
        "[L][R]TOTAL PRICE :[R]34.98e\n" +
        "[L][R]TAX :[R]4.23e\n" +
        "[L]\n" +
        "[C]================================\n" +
        "[L]\n" +
        "[L]<font size='tall'>Customer :</font>\n" +
        "[L]Raymond DUPONT\n" +
        "[L]5 rue des girafes\n" +
        "[L]31547 PERPETES\n" +
        "[L]Tel : +33801201456\n" +
        "[L]\n" +
        "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
        "[C]<qrcode size='20'>http://www.developpeur-web.dantsu.com/</qrcode>"
    );
```


## Formatted text : syntax guide

### New line

Use `\n` to create a new line of text.

### Text alignment and column separation

Add an alignment tag on a same line of text implicitly create a new column.

Column alignment tags :

- `[L]` : left side alignment
- `[C]` : center alignment
- `[R]` : right side alignment

Example :

- `[L]Some text` : One column aligned to left
- `[C]Some text` : One column aligned to center
- `[R]Some text` : One column aligned to right
- `[L]Some text[L]Some other text` : Two columns aligned to left. `Some other text` starts in the center of the paper.
- `[L]Some text[R]Some other text` : Two columns, first aligned to left, second aligned to right. `Some other text` is printed at the right of paper.
- `[L]Some[R]text[R]here` : Three columns.
- `[L][R]text[R]here` : Three columns. The first is empty but it takes a third of the available space.

### Font

#### Size

`<font></font>` tag allows you to change the font size. Default size is `medium`.

- `<font size='small'>Some text</font>` : Small size
- `<font size='medium'>Some text</font>` : Medium size
- `<font size='wide'>Some text</font>` : Double width of medium size
- `<font size='tall'>Some text</font>` : Double height of medium size
- `<font size='big'>Some text</font>` : Double width and height of medium size

#### Bold

`<b></b>` tag allows you to change the font weight.

- `<b>Some text</b>`

#### Underline

`<u></u>` tag allows you to underline the text.

- `<u>Some text</u>`

### Image

`<img></img>` tag allows you to print image. Inside the tag you need to write a hexadecimal string of an image.

Use `PrinterTextParserImg.bitmapToHexadecimalString` to convert `Drawable`, `BitmapDrawable` or `Bitmap` to hexadecimal string.

- `<img>`hexadecimal string of an image`</img>`

**/!\\ WARNING /!\\** : This tag has several constraints :

- A line that contains `<img></img>` can have only one alignment tag and it must be at the beginning of the line.
- `<img>` must be directly preceded by nothing or an alignment tag (`[L][C][R]`).
- `</img>` must be directly followed by a new line `\n`.
- You can't write text on a line that contains `<img></img>`.

### Barcode

`<barcode></barcode>` tag allows you to print a barcode. Inside the tag you need to write the code number to print.

- `<barcode>451278452159</barcode>` : **(12 numbers)**  
Prints a EAN13 barcode with a height of 10 millimeters.
- `<barcode height='15'>451278452159</barcode>` : **(12 numbers)**  
Prints a EAN13 barcode with a height of 15 millimeters.
- `<barcode type='ean13'>451278452159</barcode>` : **(12 numbers)**  
Prints a EAN13 barcode with a height of 10 millimeters.
- `<barcode type='ean8'>4512784</barcode>` : **(7 numbers)**  
Prints a EAN8 barcode with a height of 10 millimeters.
- `<barcode type='upca' height='20'>4512784521</barcode>` : **(11 numbers)**  
Prints a UPC-A barcode with a height of 20 millimeters.
- `<barcode type='upce' height='25'>051278</barcode>` : **(6 numbers)**  
Prints a UPC-E barcode with a height of 25 millimeters.

**/!\\ WARNING /!\\** : This tag has several constraints :

- A line that contains `<barcode></barcode>` can have only one alignment tag and it must be at the beginning of the line.
- `<barcode>` must be directly preceded by nothing or an alignment tag (`[L][C][R]`).
- `</barcode>` must be directly followed by a new line `\n`.
- You can't write text on a line that contains `<barcode></barcode>`.

### QR Code

`<qrcode></qrcode>` tag allows you to print a QR code. Inside the tag you need to write the QR code data.

- `<qrcode>http://www.developpeur-web.dantsu.com/</qrcode>` :
Prints a QR code with a width and height of 20 millimeters.
- `<qrcode size='25'>123456789</qrcode>` :
Prints a QR code with a width and height of 25 millimeters.

**/!\\ WARNING /!\\** : This tag has several constraints :

- A line that contains `<qrcode></qrcode>` can have only one alignment tag and it must be at the beginning of the line.
- `<qrcode>` must be directly preceded by nothing or an alignment tag (`[L][C][R]`).
- `</qrcode>` must be directly followed by a new line `\n`.
- You can't write text on a line that contains `<qrcode></qrcode>`.

## Configuration

Library allow you change character table and encoding using `EscPosPrinterConfig` class. If not set, default value will be used.

```
//set before printing to change character table and charset encoding
EscPosPrinterConfig.CHARACTER_TABLE = 0x06; //Western Europe 
EscPosPrinterConfig.CHARSET_ENCODING = "ISO-8859-1";
```

## Class list

### Class : `com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections`

#### **Static** Method : `selectFirstPaired()`
Easy way to get the first bluetooth printer paired / connected.
- **return** `BluetoothConnection`

#### Method : `getList()`
Get a list of bluetooth printers.
- **return** `BluetoothConnection[]`

### Class : `com.dantsu.escposprinter.connection.usb.UsbPrintersConnections`

#### **Static** Method : `selectFirstConnected()`
Easy way to get the first USB printer connected.
- **return** `UsbConnection`

#### Method : `getList()`
Get a list of USB printers.
- **return** `UsbConnection[]`

### Class : `com.dantsu.escposprinter.EscPosPrinter`

#### Constructor : `EscPosPrinter(DeviceConnection printer, int printerDpi, float printingWidthMM, int nbrCharactersPerLine)`
- **param** `DeviceConnection printer` : Instance of a connected printer
- **param** `int printerDpi` : DPI of the connected printer
- **param** `float printingWidthMM` : Printing width in millimeters
- **param** `int nbrCharactersPerLine` : The maximum number of medium sized characters that can be printed on a line.

#### Method : `disconnectPrinter()`
Close the connection with the printer.
- **return** `Printer` : Fluent interface

#### Method : `getNbrCharactersPerLine()`
Get the maximum number of characters that can be printed on a line.
- **return** `int`

#### Method : `getPrintingWidthMM()`
Get the printing width in millimeters
- **return** `float`

#### Method : `getPrinterDpi()`
Get the printer DPI
- **return** `int`

#### Method : `getPrintingWidthPx()`
Get the printing width in dot
- **return** `int`

#### Method : `getCharSizeWidthPx()`
Get the number of dot that a printed character contain
- **return** `int`

#### Method : `mmToPx(float mmSize)`
Convert the mmSize variable from millimeters to dot.
- **param** `float mmSize` : Distance in millimeters to be converted
- **return** `int` : Dot size of mmSize.

#### Method : `printFormattedText(String text)`
Print a formatted text. Read the ["Formatted Text : Syntax guide" section](#formatted-text--syntax-guide) for more information about text formatting options.
- **param** `String text` : Formatted text to be printed.
- **return** `Printer` : Fluent interface

#### Method : `bitmapToBytes(Bitmap bitmap)`
Convert Bitmap object to ESC/POS image.
- **param** `Bitmap bitmap` : Instance of Bitmap
- **return** `byte[]` : Bytes contain the image in ESC/POS command

### Class : `com.dantsu.escposprinter.textparser.PrinterTextParserImg`

#### **Static** Method : `bitmapToHexadecimalString(Printer printer, Drawable drawable)`
Convert Drawable instance to a hexadecimal string of the image data.
- **param** `Printer printer` : A Printer instance that will print the image.
- **param** `Drawable drawable` : Drawable instance to be converted.
- **return** `String` : A hexadecimal string of the image data. Empty string if Drawable cannot be cast to BitmapDrawable.

#### **Static** Method : `bitmapToHexadecimalString(Printer printer, BitmapDrawable bitmapDrawable)`
Convert BitmapDrawable instance to a hexadecimal string of the image data.
- **param** `Printer printer` : A Printer instance that will print the image.
- **param** `BitmapDrawable bitmapDrawable` : BitmapDrawable instance to be converted.
- **return** `String` : A hexadecimal string of the image data.

#### **Static** Method : `bitmapToHexadecimalString(Printer printer, Bitmap bitmap)`
Convert Bitmap instance to a hexadecimal string of the image data.
- **param** `Printer printer` : A Printer instance that will print the image.
- **param** `Bitmap bitmap` : Bitmap instance to be converted.
- **return** `String` : A hexadecimal string of the image data.

#### **Static** Method : `bytesToHexadecimalString(byte[] bytes)`
Convert byte array to a hexadecimal string of the image data.
- **param** `byte[] bytes` : Bytes contain the image in ESC/POS command.
- **return** `String` : A hexadecimal string of the image data.

#### **Static** Method : `hexadecimalStringToBytes(String hexString)`
Convert hexadecimal string of the image data to bytes ESC/POS command.
- **param** `String hexString` : Hexadecimal string of the image data.
- **return** `byte[]` : Bytes contain the image in ESC/POS command.


## Contributing

Please fork this repository and contribute back using pull requests.

Any contributions, large or small, major features, bug fixes, are welcomed and appreciated but will be thoroughly reviewed
