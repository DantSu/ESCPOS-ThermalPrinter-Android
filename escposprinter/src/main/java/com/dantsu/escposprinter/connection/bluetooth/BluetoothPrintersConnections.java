package com.dantsu.escposprinter.connection.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;

import com.dantsu.escposprinter.exceptions.EscPosConnectionException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BluetoothPrintersConnections extends BluetoothConnections {

    /**
     * Easy way to get the first bluetooth printer paired / connected.
     * @return a EscPosPrinterCommands instance
     */
    public static BluetoothConnection selectFirstPaired() {
        return selectFirstPaired(false);
    }

    /**
     * Easy way to get the first bluetooth printer paired / connected.
     * If forceConnect == true then ignore missing properties in device
     * @return a EscPosPrinterCommands instance
     */
    public static BluetoothConnection selectFirstPaired(boolean forceConnect) {
        BluetoothPrintersConnections printers = new BluetoothPrintersConnections();
        BluetoothConnection[] bluetoothPrinters = printers.getList();

        if (bluetoothPrinters != null && bluetoothPrinters.length > 0) {
            for (BluetoothConnection printer : bluetoothPrinters) {
                try {
                    return printer.connect(forceConnect);
                } catch (EscPosConnectionException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * Get a list of bluetooth printers.
     *
     * @return an array of EscPosPrinterCommands
     */
    public BluetoothConnection[] getList() {
        BluetoothConnection[] bluetoothDevicesList = super.getList();
        Set<Integer> allowedBluetoothPrinterDeviceClasses = new HashSet<>(Arrays.asList(1664, BluetoothClass.Device.Major.IMAGING));

        if(bluetoothDevicesList == null) {
            return null;
        }

        int i = 0;
        BluetoothConnection[] printersTmp = new BluetoothConnection[bluetoothDevicesList.length];
        for (BluetoothConnection bluetoothConnection : bluetoothDevicesList) {
            BluetoothDevice device = bluetoothConnection.getDevice();
            if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING && allowedBluetoothPrinterDeviceClasses.contains(device.getBluetoothClass().getDeviceClass())) {
                printersTmp[i++] = new BluetoothConnection(device);
            }
        }
        BluetoothConnection[] bluetoothPrinters = new BluetoothConnection[i];
        System.arraycopy(printersTmp, 0, bluetoothPrinters, 0, i);
        return bluetoothPrinters;
    }

}
