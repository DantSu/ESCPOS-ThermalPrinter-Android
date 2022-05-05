package com.dantsu.escposprinter.connection.usb;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;

import androidx.annotation.Nullable;

public class UsbDeviceHelper {
    /**
     * Find the correct USB interface for printing
     *
     * @param usbDevice USB device
     * @return correct USB interface for printing, null if not found
     */
    @Nullable
    static public UsbInterface findPrinterInterface(UsbDevice usbDevice) {
        if (usbDevice == null) {
            return null;
        }
        int interfacesCount = usbDevice.getInterfaceCount();
        for (int i = 0; i < interfacesCount; i++) {
            UsbInterface usbInterface = usbDevice.getInterface(i);
            if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                return usbInterface;
            }
        }
        return null;
    }

    /**
     * Find the USB endpoint for device input
     *
     * @param usbInterface USB interface
     * @return Input endpoint or null if not found
     */
    @Nullable
    static public UsbEndpoint findEndpointIn(UsbInterface usbInterface) {
        if (usbInterface != null) {
            int endpointsCount = usbInterface.getEndpointCount();
            for (int i = 0; i < endpointsCount; i++) {
                UsbEndpoint endpoint = usbInterface.getEndpoint(i);
                if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                    return endpoint;
                }
            }
        }
        return null;
    }
}
