package com.dantsu.escposprinter.connection.usb;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;

import java.io.IOException;

public class UsbConnection extends DeviceConnection {

    private UsbManager usbManager;
    private UsbDevice usbDevice;

    /**
     * Create un instance of UsbConnection.
     *
     * @param usbManager an instance of UsbManager
     * @param usbDevice  an instance of UsbDevice
     */
    public UsbConnection(UsbManager usbManager, UsbDevice usbDevice) {
        super();
        this.usbManager = usbManager;
        this.usbDevice = usbDevice;
    }

    /**
     * Get the instance UsbDevice connected.
     *
     * @return an instance of UsbDevice
     */
    public UsbDevice getDevice() {
        return this.usbDevice;
    }

    /**
     * Start socket connection with the usbDevice.
     */
    public UsbConnection connect() throws EscPosConnectionException {
        if (this.isConnected()) {
            return this;
        }

        try {
            this.stream = new UsbOutputStream(this.usbManager, this.usbDevice);
            this.data = new byte[0];
        } catch (IOException e) {
            e.printStackTrace();
            this.stream = null;
            throw new EscPosConnectionException("Unable to connect to USB device.");
        }
        return this;
    }

    /**
     * Close the socket connection with the usbDevice.
     */
    public UsbConnection disconnect() {
        this.data = new byte[0];
        if (this.isConnected()) {
            try {
                this.stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.stream = null;
        }
        return this;
    }

    /**
     * Send data to the device.
     */
    public void send() throws EscPosConnectionException {
        try {
            this.stream.write(this.data);
            this.data = new byte[0];
        } catch (IOException e) {
            e.printStackTrace();
            throw new EscPosConnectionException(e.getMessage());
        }
    }


}
