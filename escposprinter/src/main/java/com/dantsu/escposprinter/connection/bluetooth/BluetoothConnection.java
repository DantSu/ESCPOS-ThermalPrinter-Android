package com.dantsu.escposprinter.connection.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.dantsu.escposprinter.connection.DeviceConnection;

import java.io.IOException;

public class BluetoothConnection extends DeviceConnection {
    
    private BluetoothDevice device;
    private BluetoothSocket socket = null;
    
    /**
     * Create un instance of BluetoothConnection.
     *
     * @param device an instance of BluetoothDevice
     */
    public BluetoothConnection(BluetoothDevice device) {
        super();
        this.device = device;
    }
    
    /**
     * Get the instance BluetoothDevice connected.
     *
     * @return an instance of BluetoothDevice
     */
    public BluetoothDevice getDevice() {
        return this.device;
    }

    /**
     * Start socket connection with the bluetooth device.
     *
     * @return return true if success
     */
    public boolean connect() {
        if(this.isConnected()) {
            return true;
        }
        try {
            this.socket = this.device.createRfcommSocketToServiceRecord(this.device.getUuids()[0].getUuid());
            this.socket.connect();
            this.stream = this.socket.getOutputStream();
            this.data = new byte[0];
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            this.disconnect();
            this.socket = null;
            this.stream = null;
        }
        return false;
    }
    
    /**
     * Close the socket connection with the bluetooth device.
     *
     * @return return true if success
     */
    public boolean disconnect() {
        this.data = new byte[0];
        if(this.stream != null) {
            try {
                this.stream.close();
                this.stream = null;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        if(this.socket != null) {
            try {
                this.socket.close();
                this.socket = null;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

}
