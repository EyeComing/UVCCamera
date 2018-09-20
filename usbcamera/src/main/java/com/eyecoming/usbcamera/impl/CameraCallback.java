package com.eyecoming.usbcamera.impl;

import com.serenegiant.usb.UVCCamera;

public interface CameraCallback {
    void onOpen(UVCCamera uvcCamera);

    void onClose();

    void onStartPreview();

    void onStopPreview();

    void onStartRecording();

    void onStopRecording();

    void onError(final Exception e);
}