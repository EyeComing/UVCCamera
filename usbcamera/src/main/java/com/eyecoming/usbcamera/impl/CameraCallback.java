package com.eyecoming.usbcamera.impl;

import com.serenegiant.usb.UVCCamera;

public interface CameraCallback {
    /**
     * Camera open
     *
     * @param uvcCamera UVCCamera
     */
    void onOpen(UVCCamera uvcCamera);

    /**
     * Camera close
     */
    void onClose();

    /**
     * Camera start preview
     */
    void onStartPreview();

    /**
     * Camera stop preview
     */
    void onStopPreview();

    /**
     * Start recording
     */
    void onStartRecording();

    /**
     * Stop recording
     */
    void onStopRecording();

    /**
     * An exception occurred during UVCCameraHandler execution
     *
     * @param e Exception
     */
    void onError(final Exception e);
}