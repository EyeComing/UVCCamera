package com.eyecoming.usbcamera;

import android.hardware.usb.UsbDevice;

import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;

/**
 * Created by JesseHu on 2017/3/30.
 */

public interface OnCameraListener {

    /**
     * USB相机连接成功
     *
     * @param device    UsbDevice
     * @param ctrlBlock usb控制模块
     */
    void connected(UsbDevice device, UsbControlBlock ctrlBlock);

    /**
     * USB相机断开连接
     */
    void disconnect(UsbDevice device, UsbControlBlock ctrlBlock);

    /**
     * USB 设备连接
     *
     * @param device UsbDevice
     */
    void onAttach(UsbDevice device);

    /**
     * USB 设备断开连接
     *
     * @param device UsbDevice
     */
    void onDettach(UsbDevice device);
}
