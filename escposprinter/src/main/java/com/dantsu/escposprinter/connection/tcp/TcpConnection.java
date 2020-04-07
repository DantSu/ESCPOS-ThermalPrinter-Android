package com.dantsu.escposprinter.connection.tcp;

import com.dantsu.escposprinter.connection.DeviceConnection;

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
        return this.socket != null;
    }

    /**
     * Start socket connection with the TCP device.
     *
     * @return return true if success
     */
    public boolean connect() {
        if (this.isConnected()) {
            return true;
        }
        try {
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(InetAddress.getByName(this.address), this.port));
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
     * Close the socket connection with the TCP device.
     *
     * @return return true if success
     */
    public boolean disconnect() {
        this.data = new byte[0];
        if (this.stream != null) {
            try {
                this.stream.close();
                this.stream = null;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        if (this.socket != null) {
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
