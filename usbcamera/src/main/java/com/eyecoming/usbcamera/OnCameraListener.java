package com.eyecoming.usbcamera;

import android.hardware.usb.UsbDevice;

import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;

/**
 * Created by JesseHu on 2017/3/30.
 */

public interface OnCameraListener {

    /**
     * USB camera connected USB相机连接成功
     *
     * @param device    UsbDevice
     * @param ctrlBlock UsbControlBlock
     */
    void connected(UsbDevice device, UsbControlBlock ctrlBlock);

    /**
     * USB camera disconnected USB相机断开连接
     *
     * @param device    UsbDevice
     * @param ctrlBlock UsbControlBlock
     */
    void disconnect(UsbDevice device, UsbControlBlock ctrlBlock);

    /**
     * USB device attach USB设备连接
     *
     * @param device UsbDevice
     */
    void onAttach(UsbDevice device);

    /**
     * USB device detach USB设备断开连接
     *
     * @param device UsbDevice
     */
    void onDettach(UsbDevice device);
}
