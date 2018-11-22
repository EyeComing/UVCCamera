package com.eyecoming.usbcamera.impl;

/**
 * CameraClientCallback
 *
 * @author JesseHu
 * @date 2018/11/21
 */
public interface CameraClientCallback {
    /**
     * Camera client connected
     */
    void onConnected();

    /**
     * Camera client disconnected
     */
    void onDisconnected();
}
