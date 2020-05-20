package com.dantsu.escposprinter.connection.usb;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.exceptions.BrokenConnectionException;

import java.io.IOException;

public class UsbConnection extends DeviceConnection {
    
    private UsbManager usbManager;
    private UsbDevice usbDevice;

    /**
     * Create un instance of UsbConnection.
     *
     * @param usbManager an instance of UsbManager
     * @param usbDevice an instance of UsbDevice
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
     *
     * @return return true if success
     */
    public boolean connect() {
        if(this.isConnected()) {
            return true;
        }
        try {
            this.stream = new UsbOutputStream(this.usbManager, this.usbDevice);
            this.data = new byte[0];
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            this.disconnect();
            this.stream = null;
        }
        return false;
    }
    
    /**
     * Close the socket connection with the usbDevice.
     *
     * @return return true if success
     */
    public boolean disconnect() {
        this.data = new byte[0];
        if(this.isConnected()) {
            try {
                this.stream.close();
                this.stream = null;
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Send data to the device.
     */
    public void send() throws BrokenConnectionException {
        try {
            this.stream.write(this.data);
            this.data = new byte[0];
        } catch (IOException e) {
            throw new BrokenConnectionException(e.getMessage());
        }
    }
}
