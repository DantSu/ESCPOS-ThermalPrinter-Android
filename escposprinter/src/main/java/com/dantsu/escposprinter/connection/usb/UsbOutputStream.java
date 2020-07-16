package com.dantsu.escposprinter.connection.usb;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class UsbOutputStream extends OutputStream {
    private UsbDeviceConnection usbConnection;
    private UsbInterface usbInterface;
    private UsbEndpoint usbEndpoint;

    public UsbOutputStream(UsbManager usbManager, UsbDevice usbDevice) throws IOException {

        this.usbInterface = UsbDeviceHelper.findPrinterInterface(usbDevice);
        if(this.usbInterface == null) {
            throw new IOException("Unable to find USB interface.");
        }

        this.usbEndpoint = UsbDeviceHelper.findEndpointIn(this.usbInterface);
        if(this.usbEndpoint == null) {
            throw new IOException("Unable to find USB endpoint.");
        }

        this.usbConnection = usbManager.openDevice(usbDevice);
        if(this.usbConnection == null) {
            throw new IOException("Unable to open USB connection.");
        }
    }

    @Override
    public void write(int i) throws IOException {
        this.write(new byte[]{(byte) i});
    }

    @Override
    public void write(@NonNull byte[] bytes) throws IOException {
        this.write(bytes, 0, bytes.length);
    }

    @Override
    public void write(final @NonNull byte[] bytes, final int offset, final int length) throws IOException {
        if (this.usbInterface == null || this.usbEndpoint == null || this.usbConnection == null) {
            throw new IOException("Unable to connect to USB device.");
        }

        if (!this.usbConnection.claimInterface(this.usbInterface, true)) {
            throw new IOException("Error during claim USB interface.");
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        UsbRequest usbRequest = new UsbRequest();
        try {
            usbRequest.initialize(this.usbConnection, this.usbEndpoint);
            if (!usbRequest.queue(buffer, bytes.length)) {
                throw new IOException("Error queueing USB request.");
            }
            this.usbConnection.requestWait();
        } finally {
            usbRequest.close();
        }
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() throws IOException {
        if (this.usbConnection != null) {
            this.usbConnection.close();
            this.usbInterface = null;
            this.usbEndpoint = null;
            this.usbConnection = null;
        }
    }
}
