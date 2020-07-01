package com.dantsu.escposprinter.connection.tcp;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpConnection extends DeviceConnection {
    private Socket socket = null;
    private String address;
    private int port;

    /**
     * Create un instance of TcpConnection.
     *
     * @param address IP address of the device
     * @param port    Port of the device
     */
    public TcpConnection(String address, int port) {
        super();
        this.address = address;
        this.port = port;
    }

    /**
     * Check if the TCP device is connected by socket.
     *
     * @return true if is connected
     */
    public boolean isConnected() {
        return this.socket != null && this.socket.isConnected() && super.isConnected();
    }

    /**
     * Start socket connection with the TCP device.
     */
    public TcpConnection connect() throws EscPosConnectionException {
        if (this.isConnected()) {
            return this;
        }
        try {
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(InetAddress.getByName(this.address), this.port));
            this.stream = this.socket.getOutputStream();
            this.data = new byte[0];
        } catch (IOException e) {
            e.printStackTrace();
            this.socket = null;
            this.stream = null;
            throw new EscPosConnectionException("Unable to connect to TCP device.");
        }
        return this;
    }

    /**
     * Close the socket connection with the TCP device.
     */
    public TcpConnection disconnect() {
        this.data = new byte[0];
        if (this.stream != null) {
            try {
                this.stream.close();
                this.stream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.socket != null) {
            try {
                this.socket.close();
                this.socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

}
