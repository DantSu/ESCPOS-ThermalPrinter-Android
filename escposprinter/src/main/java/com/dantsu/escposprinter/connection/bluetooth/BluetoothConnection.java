package com.dantsu.escposprinter.connection.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;

import java.io.IOException;
import java.util.UUID;

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
     * Check if OutputStream is open.
     *
     * @return true if is connected
     */
    @Override
    public boolean isConnected() {
        return this.socket != null && this.socket.isConnected() && super.isConnected();
    }

    /**
     * Start socket connection with the bluetooth device.
     */
    public BluetoothConnection connect() throws EscPosConnectionException {
        return connect(false);
    }

    /**
     * Start socket connection with the bluetooth device.
     * If forceConnect == true then ignore the absence of UUIDs
     */
    public BluetoothConnection connect(boolean forceConnect) throws EscPosConnectionException {
        if(this.isConnected()) {
            return this;
        }

        if(this.device == null) {
            throw new EscPosConnectionException("Bluetooth device is not connected.");
        }

        ParcelUuid[] uuids = this.device.getUuids();
        UUID uuid = (uuids == null || uuids.length == 0) ? UUID.randomUUID() : uuids[0].getUuid();

        if(uuid == null && !forceConnect) {
            throw new EscPosConnectionException("Unable to find bluetooth device UUID.");
        }

        try {
            this.socket = this.device.createRfcommSocketToServiceRecord(uuid);
            this.socket.connect();
            this.stream = this.socket.getOutputStream();
            this.data = new byte[0];
        } catch (IOException e) {
            e.printStackTrace();
            this.socket = null;
            this.stream = null;
            throw new EscPosConnectionException("Unable to connect to bluetooth device.");
        }
        return this;
    }

    /**
     * Close the socket connection with the bluetooth device.
     */
    public BluetoothConnection disconnect() {
        this.data = new byte[0];
        if(this.stream != null) {
            try {
                this.stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.stream = null;
        }
        if(this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.socket = null;
        }
        return this;
    }

}
