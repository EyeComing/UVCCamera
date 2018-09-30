package com.eyecoming.usblib;

import android.hardware.usb.UsbDevice;

/**
 * USBCamera
 *
 * @author JesseHu
 * @date 2018/7/26
 */
public class USBUtil {
    public static final int TYPE_DEVICE_UNKNOWN = -1;
    public static final int TYPE_DEVICE_CAMERA = 1;
    public static final int TYPE_DEVICE_AUDIO = 2;
    public static final int TYPE_DEVICE_STM32 = 3;

    private static final int DEVICE_CLASS_CAMERA = 239;
    private static final int DEVICE_CLASS_AUDIO = 0;
    private static final int DEVICE_CLASS_STM32 = 255;

    private static final int DEVICE_SUBCLASS_CAMERA = 2;
    private static final int DEVICE_SUBCLASS_AUDIO = 0;
    private static final int DEVICE_SUBCLASS_STM32 = 2;

    public static int getDeviceType(UsbDevice device) {
        int deviceClass = device.getDeviceClass();
        int deviceSubclass = device.getDeviceSubclass();
        if (deviceClass == DEVICE_CLASS_CAMERA && deviceSubclass == DEVICE_SUBCLASS_CAMERA) {
            return TYPE_DEVICE_CAMERA;
        } else if (deviceClass == DEVICE_CLASS_AUDIO && deviceSubclass == DEVICE_SUBCLASS_AUDIO) {
            return TYPE_DEVICE_AUDIO;
        } else if (deviceClass == DEVICE_CLASS_STM32 && deviceSubclass == DEVICE_SUBCLASS_STM32) {
            return TYPE_DEVICE_STM32;
        } else {
            return TYPE_DEVICE_UNKNOWN;
        }
    }
}
