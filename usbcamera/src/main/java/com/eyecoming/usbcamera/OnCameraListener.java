package com.eyecoming.usbcamera;

import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;

/**
 * Created by JesseHu on 2017/3/30.
 */

public interface OnCameraListener {

    /**
     * usb相机连接成功
     *
     * @param camera    UVCCanera
     * @param ctrlBlock usb控制模块
     */
    void connected(UVCCamera camera, UsbControlBlock ctrlBlock);

    /**
     * usb相机断开连接
     */
    void disconnect();
}
