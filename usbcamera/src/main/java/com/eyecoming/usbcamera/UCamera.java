package com.eyecoming.usbcamera;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.support.annotation.NonNull;
import android.util.Log;

import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;

import java.util.List;

/**
 * UCamera
 *
 * @author JesseHu
 * @date 2018/7/19
 */

public class UCamera implements CameraDialog.CameraDialogParent {
    private static final int DEVICE_CLASS = 239;
    private static final int DEVICE_SUBCLASS = 2;
    private static final int PRODUCT_ID = 22656;
    private static final int VENDOR_ID = 3034;
    private final static String TAG = "UCamera";
    private USBMonitor mUSBMonitor;
    private Context mContext;
    private OnCameraListener mListener;
    private boolean mAutoConnect;
    private USBMonitor.UsbControlBlock mControlBlock;
    private boolean isAttached = false;
    private boolean notConnect = false;

    /**
     * 初始化UCamera
     *
     * @param context     Context
     * @param autoConnect 是否自动连接USB相机,true默认选择第一个进行连接，false弹窗选择camera进行连接
     */
    public UCamera(Context context, boolean autoConnect) {
        this.mContext = context;
        this.mAutoConnect = autoConnect;
        this.notConnect = false;
        mUSBMonitor = new USBMonitor(context, mOnDeviceConnectListener);
    }

    /**
     * 初始化UCamera
     *
     * @param context        Context
     * @param autoConnect    是否自动连接USB相机,true默认选择第一个进行连接，false弹窗选择camera进行连接
     * @param cameraListener OnCameraListener USBCamera回调
     */
    public UCamera(Context context, boolean autoConnect, OnCameraListener cameraListener) {
        this(context, autoConnect);
        this.mListener = cameraListener;
    }

    /**
     * 初始化UCamera
     *
     * @param context                 Context
     * @param autoConnect             是否自动连接USB相机,true默认选择第一个进行连接，false弹窗选择camera进行连接
     * @param onDeviceConnectListener USBMonitor.OnDeviceConnectListener <br/>如果设置了监听,autoConnect不再起作用，所有操作将在OnDeviceConnectListener的回调中自行处理,
     *                                <br/>同时OnCameraListener也不再进行回调,即 {@link UCamera#setOnCameraListener(OnCameraListener)} 将无效
     *                                <br/> {@link UCamera#getControlBlock()} 将返回null
     * @deprecated 使用 {@link UCamera#UCamera(Context, USBMonitor.OnDeviceConnectListener)} 替代
     */
    public UCamera(Context context, boolean autoConnect, USBMonitor.OnDeviceConnectListener onDeviceConnectListener) {
        this.mContext = context;
        this.mAutoConnect = autoConnect;
        this.notConnect = false;
        if (onDeviceConnectListener == null) {
            onDeviceConnectListener = mOnDeviceConnectListener;
        }
        mUSBMonitor = new USBMonitor(context, onDeviceConnectListener);
    }

    /**
     * 初始化UCamera
     *
     * @param context                 Context
     * @param onDeviceConnectListener USBMonitor.OnDeviceConnectListener
     *                                <br/>如果设置了该监听 {@link UCamera#setOnCameraListener(OnCameraListener)} 将无效
     *                                <br/> {@link UCamera#getControlBlock()} 将返回null
     */
    public UCamera(Context context, @NonNull USBMonitor.OnDeviceConnectListener onDeviceConnectListener) {
        this.mContext = context;
        this.notConnect = false;
        mUSBMonitor = new USBMonitor(context, onDeviceConnectListener);
    }

    /**
     * 初始化UCamera
     * <br/>
     * 不会主动连接USB Camera(不会自动连接，也不会有设备选择弹窗)
     *
     * @param context        Context
     * @param cameraListener OnCameraListener USBCamera回调
     */
    public UCamera(Context context, OnCameraListener cameraListener) {
        this(context);
        this.mListener = cameraListener;
    }

    /**
     * 初始化UCamera
     * <br/>
     * 不会主动连接USB Camera(不会自动连接，也不会有设备选择弹窗)
     *
     * @param context Context
     */
    public UCamera(Context context) {
        this.mContext = context;
        this.notConnect = true;
        mUSBMonitor = new USBMonitor(context, mOnDeviceConnectListener);
    }

    /**
     * 设置连接监听器
     *
     * @param cameraListener 连接监听
     */
    public void setOnCameraListener(OnCameraListener cameraListener) {
        this.mListener = cameraListener;
    }

    /**
     * 注册USB监听器
     */
    public void monitorRegister() {
        if (mUSBMonitor != null) {
            mUSBMonitor.register();
        }
    }

    /**
     * 注销USB监听器
     */
    public void monitorUnregister() {
        if (mUSBMonitor != null) {
            mUSBMonitor.unregister();
        }
    }

    /**
     * 销毁USB监听器
     */
    public void monitorDestory() {
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
    }


    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(UsbDevice device) {
            Log.i(TAG, "attach");
            if (mListener != null) {
                mListener.onAttach(device);
            }

            if (isCameraDevice(device)) {
                if (!isAttached) {
                    isAttached = true;
                    if (notConnect) {
                        return;
                    }
                    if (!mAutoConnect) {
                        //选择摄像头
                        CameraDialog.showDialog((Activity) mContext);
                    } else {
                        getFirstUsbCameraDevice();
                    }
                }
            }
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            Log.i(TAG, "USBMonitor connect");

            mControlBlock = ctrlBlock;
            if (mListener != null) {
                mListener.connected(device, ctrlBlock);
            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            // XXX you should check whether the comming device equal to camera device that currently using
            Log.i(TAG, "USBMonitor disconnect");

            if (mListener != null) {
                mListener.disconnect(device, ctrlBlock);
            }
        }

        @Override
        public void onCancel(UsbDevice usbDevice) {

        }

        @Override
        public void onDettach(final UsbDevice device) {
            Log.i(TAG, "dettach");
            if (mListener != null) {
                mListener.onDettach(device);
            }
            isAttached = false;
        }

    };

    /**
     * 请求usb权限
     *
     * @param device USB Camera 对应的 UsbDevice
     * @return 设备是否支持USB设备 true:支持 false:不支持
     */
    public boolean requestUsbCameraDevice(UsbDevice device) {
        boolean result = mUSBMonitor.requestPermission(device);
        if (result) {
            // when failed. your device may not support USB.
            Log.d(TAG, "your device may not support USB");
        }
        return !result;
    }

    /**
     * 请求第一个可用USB摄像头
     *
     * @return UsbDevice
     */
    public UsbDevice getFirstUsbCameraDevice() {
        List<UsbDevice> deviceList = getUsbDevices();
        UsbDevice device = null;

        if (deviceList.size() > 0) {
            //默认取第一个摄像头
            device = deviceList.get(0);
            boolean result = mUSBMonitor.requestPermission(device);
            if (result) {
                // when failed. your device may not support USB.
                Log.d(TAG, "your device may not support USB");
            }
        }
        return device;
    }

    /**
     * 根据USB设备过滤文件请求第一个可用USB摄像头
     *
     * @param deviceFilterId 摄像头过滤文件ID(只限xml文件夹)
     * @return UsbDevice
     */
    public UsbDevice getFirstUsbCameraDevice(int deviceFilterId) {
        List<UsbDevice> deviceList = getUsbDevices(deviceFilterId);
        UsbDevice device = null;

        if (deviceList.size() > 0) {
            //默认取第一个摄像头
            device = deviceList.get(0);
            boolean result = mUSBMonitor.requestPermission(device);
            if (result) {
                // when failed. your device may not support USB.
                Log.d(TAG, "your device may not support USB");
            }
        }
        return device;
    }

    /**
     * 获取USB摄像头列表
     *
     * @return List&lt;UsbDevice&gt;USB摄像头列表
     */
    public List<UsbDevice> getUsbDevices() {
        return getUsbDevices(com.serenegiant.uvccamera.R.xml.device_filter);
    }

    /**
     * 根据USB设备过滤文件获取USB摄像头列表
     *
     * @param deviceFilterId 摄像头过滤文件ID(只限xml文件夹)
     * @return List&lt;UsbDevice&gt;USB摄像头列表
     */
    public List<UsbDevice> getUsbDevices(int deviceFilterId) {
        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(mContext, deviceFilterId);
        return mUSBMonitor.getDeviceList(filter.get(0));
    }


    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean b) {

    }

    /**
     * 获取UsbControlBlock
     *
     * @return UsbControlBlock
     */
    public USBMonitor.UsbControlBlock getControlBlock() {
        return mControlBlock;
    }

    /**
     * 判断指定设备是否已经申请了权限
     *
     * @param device UsbDevice
     * @return true:有权限 false:未申请权限
     */
    public boolean hasPermission(UsbDevice device) {
        return mUSBMonitor.hasPermission(device);
    }

    /**
     * @param device
     * @return
     */
    public static boolean isCameraDevice(UsbDevice device) {
        int deviceClass = device.getDeviceClass();
        int deviceSubclass = device.getDeviceSubclass();
        int productId = device.getProductId();
        int vendorId = device.getVendorId();
        return (PRODUCT_ID == productId && VENDOR_ID == vendorId && DEVICE_CLASS == deviceClass && deviceSubclass == DEVICE_SUBCLASS);
//        return (DEVICE_CLASS == deviceClass && deviceSubclass == DEVICE_SUBCLASS);
    }
}
