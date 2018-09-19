package com.eyecoming.usbcamera;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.support.annotation.NonNull;
import android.util.Log;

import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * UCamera
 *
 * @author JesseHu
 * @date 2018/7/19
 */

public class UCamera implements CameraDialog.CameraDialogParent {
    private USBMonitor mUSBMonitor;
    private Context mContext;
    private OnCameraListener mListener;
    private boolean mAutoConnect;
    private USBMonitor.UsbControlBlock mControlBlock;
    private final static String TAG = "UCamera";
    private boolean isAttached = false;

    /**
     * 初始化UCamera
     *
     * @param context     Context
     * @param autoConnect 是否自动连接USB相机,true默认选择第一个进行连接，false弹窗选择camera进行连接
     */
    public UCamera(Context context, boolean autoConnect) {
        this.mContext = context;
        this.mAutoConnect = autoConnect;
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
        mUSBMonitor = new USBMonitor(context, onDeviceConnectListener);
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
            if (!isAttached) {
                isAttached = true;
                if (!mAutoConnect) {
                    //选择摄像头
                    CameraDialog.showDialog((Activity) mContext);
                } else {
                    getFirstUsbCameraDevice();
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
     * 请求第一个可用USB摄像头
     *
     * @return UsbDevice
     */
    public UsbDevice getFirstUsbCameraDevice() {
        List<UsbDevice> deviceList = getUsbDevices();
        UsbDevice device = null;

        if (deviceList.size() > 0) {
            //默认去第一个摄像头
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
     * 获取USB摄像头列表
     *
     * @param deviceFilterId 摄像头过滤文件
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
}
