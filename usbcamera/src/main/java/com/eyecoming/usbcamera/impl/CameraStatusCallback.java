package com.eyecoming.usbcamera.impl;

/**
 * CameraStatusCallback
 *
 * @author JesseHu
 * @date 2018/11/21
 */
public interface CameraStatusCallback {
    /**
     * camera status callback Camera状态回调
     *
     * @param status camera status Camera状态
     */
    void cameraStatus(int status);
}
