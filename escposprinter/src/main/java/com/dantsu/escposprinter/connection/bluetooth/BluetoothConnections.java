package com.dantsu.escposprinter.connection.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.Set;

public class BluetoothConnections {
    protected BluetoothAdapter bluetoothAdapter;
    
    /**
     * Create a new instance of BluetoothConnections
     */
    public BluetoothConnections() {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    
    /**
     * Get a list of bluetooth devices available.
     * @return Return an array of BluetoothConnection instance
     */
    public BluetoothConnection[] getList() {
        if (this.bluetoothAdapter == null) {
            return null;
        }
    
        if(!this.bluetoothAdapter.isEnabled()) {
            return null;
        }
        
        Set<BluetoothDevice> bluetoothDevicesList = this.bluetoothAdapter.getBondedDevices();
        BluetoothConnection[] bluetoothDevices = new BluetoothConnection[bluetoothDevicesList.size()];
    
        if (bluetoothDevicesList.size() > 0) {
            int i = 0;
            for (BluetoothDevice device : bluetoothDevicesList) {
                bluetoothDevices[i++] = new BluetoothConnection(device);
            }
        }
        
        return bluetoothDevices;
    }
}
