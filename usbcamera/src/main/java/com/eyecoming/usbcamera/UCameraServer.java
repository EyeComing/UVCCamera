package com.eyecoming.usbcamera;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.util.Log;
import android.view.Surface;

import com.eyecoming.usbcamera.service.UVCService;
import com.eyecoming.usbcamera.serviceclient.CameraClient;
import com.eyecoming.usbcamera.serviceclient.ICameraClientCallback;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.USBMonitor;

import java.util.ArrayList;
import java.util.List;

import static com.eyecoming.usbcamera.UCamera.isCameraDevice;

/**
 * UCameraServer
 *
 * @author JesseHu
 * @date 2018/10/16
 */
public class UCameraServer implements CameraDialog.CameraDialogParent {
    private final static String TAG = "UCameraServer";
    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 720;
    private Context mContext;
    private OnCameraListener mCameraListener;
    private USBMonitor mUSBMonitor;
    private boolean isAttached = false;

    private USBMonitor.UsbControlBlock mControlBlock;
    private CameraClient mCameraClient;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private Surface mPreviewSurface;
    private Surface mPreviewSurfaceSub;
    private boolean open = false;
    private List<Surface> surfaces;

    /**
     * init 初始化
     *
     * @param mContext Context
     */
    public UCameraServer(Context mContext) {
        this(mContext, R.xml.device_filter);
    }

    /**
     * init(with a custom filter for usb) 初始化,可自定义usb过滤
     *
     * @param mContext Context
     * @param filterId usb device filter id
     */
    public UCameraServer(Context mContext, int filterId) {
        this.mContext = mContext;
        surfaces = new ArrayList<>();
        mUSBMonitor = new USBMonitor(mContext, mOnDeviceConnectListener);
        List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(mContext, filterId);
        mUSBMonitor.setDeviceFilter(filters);
    }

    /**
     * init 初始化
     *
     * @param mContext        Context
     * @param mCameraListener OnCameraListener
     */
    public UCameraServer(Context mContext, OnCameraListener mCameraListener) {
        this(mContext);
        this.mCameraListener = mCameraListener;
    }

    /**
     * init(with a custom filter for usb) 初始化,可自定义usb过滤
     *
     * @param mContext        Context
     * @param filterID        usb device filter id
     * @param mCameraListener OnCameraListener
     */
    public UCameraServer(Context mContext, int filterID, OnCameraListener mCameraListener) {
        this(mContext, filterID);
        this.mCameraListener = mCameraListener;
    }

    /**
     * init 初始化
     *
     * @param mContext              Context
     * @param deviceConnectListener OnDeviceConnectListener
     */
    public UCameraServer(Context mContext, USBMonitor.OnDeviceConnectListener deviceConnectListener) {
        this(mContext, R.xml.device_filter, deviceConnectListener);
    }

    /**
     * init(with a custom filter for usb) 初始化,可自定义usb过滤
     *
     * @param mContext              Context
     * @param filterId              usb device filter id
     * @param deviceConnectListener OnDeviceConnectListener
     */
    public UCameraServer(Context mContext, int filterId, USBMonitor.OnDeviceConnectListener deviceConnectListener) {
        this.mContext = mContext;
        surfaces = new ArrayList<>();
        mUSBMonitor = new USBMonitor(mContext, deviceConnectListener);
        List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(mContext, filterId);
        mUSBMonitor.setDeviceFilter(filters);
    }

    /**
     * register usb monitor 注册USB监听器
     */
    public void monitorRegister() {
        if (mUSBMonitor != null) {
            mUSBMonitor.register();
        }
    }

    /**
     * unregister usb monitor 注销USB监听器
     */
    public void monitorUnregister() {
        if (mUSBMonitor != null) {
            mUSBMonitor.unregister();
        }
    }

    /**
     * destroy usb monitor 销毁USB监听器
     */
    public void monitorDestory() {
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
    }

    /**
     * set the connection listener for camera 设置连接监听器
     *
     * @param cameraListener OnCameraListener 连接监听
     */
    public void setOnCameraListener(OnCameraListener cameraListener) {
        this.mCameraListener = cameraListener;
    }

    /**
     * set the size of preview 设置预览尺寸
     *
     * @param width  the width of preview 预览宽度
     * @param height the height of preview 预览高度
     */
    public void setPreviewSize(int width, int height) {
        this.mPreviewWidth = width;
        this.mPreviewHeight = height;
    }

    /**
     * set the surface for preview 设置预览显示
     *
     * @param mPreviewSurface Surface
     */
    public void setPreviewDisplay(Surface mPreviewSurface) {
        this.mPreviewSurface = mPreviewSurface;
        if (mPreviewSurface != null) {
            mCameraClient.addSurface(mPreviewSurface, false);
            addSurfaceToList(mPreviewSurface);
        }
    }

    /**
     * set the sub surface for preview 设置副预览
     *
     * @param mPreviewSurfaceSub Surface
     */
    public void setSubPreviewDisPlay(Surface mPreviewSurfaceSub) {
        this.mPreviewSurfaceSub = mPreviewSurfaceSub;
        if (mPreviewSurfaceSub != null) {
            mCameraClient.addSurface(mPreviewSurfaceSub, false);
            addSurfaceToList(mPreviewSurfaceSub);
        }
    }

    /**
     * set the surface for preview 设置预览
     *
     * @param mPreviewSurface Surface显示
     * @param width           the width of preview预览宽度
     * @param height          the height of preview预览高度
     */
    public void setPreView(Surface mPreviewSurface, int width, int height) {
        this.mPreviewSurface = mPreviewSurface;
        this.mPreviewWidth = width;
        this.mPreviewHeight = height;
        if (mPreviewSurface != null) {
            mCameraClient.addSurface(mPreviewSurface, false);
            addSurfaceToList(mPreviewSurface);
        }
    }

    /**
     * 设置预览
     *
     * @param mPreviewSurface    Surface显示
     * @param mPreviewSurfaceSub sub surface副预览
     * @param width              the width of preview预览宽度
     * @param height             the height of preview预览高度
     */
    public void setPreView(Surface mPreviewSurface, Surface mPreviewSurfaceSub, int width, int height) {
        this.mPreviewSurface = mPreviewSurface;
        this.mPreviewSurfaceSub = mPreviewSurfaceSub;
        this.mPreviewWidth = width;
        this.mPreviewHeight = height;
    }

    /**
     * set the usb filter of sdk 设置默认摄像头过滤器
     */
    public void setDeviceFilter() {
        final List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(mContext, R.xml.device_filter);
        mUSBMonitor.setDeviceFilter(filters);
    }

    /**
     * set the custom usb filter 设置自定义摄像头过滤器
     *
     * @param deviceFilterId usb device filter id in XML folder
     *                       <br/>摄像头过滤文件ID(只限xml文件夹)
     */
    public void setDeviceFilter(int deviceFilterId) {
        final List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(mContext, deviceFilterId);
        mUSBMonitor.setDeviceFilter(filters);
    }

    /**
     * request the first available usb camera
     * <br/>获取第一个摄像头设备
     *
     * @return UsbDevice
     */
    private UsbDevice getFirstUsbCameraDevice() {
        List<UsbDevice> deviceList = mUSBMonitor.getDeviceList();
        UsbDevice device = null;
        if (deviceList.size() > 0) {
            //取第一个摄像头
            device = deviceList.get(0);
        }

        return device;
    }

    /**
     * open camera 开启摄像头
     */
    public void openCamera() {
        if (mCameraClient == null) {
            mCameraClient = new CameraClient(mContext, mCameraCallBack);
        }
        mCameraClient.select(getFirstUsbCameraDevice());

        if (mPreviewWidth == 0 || mPreviewHeight == 0) {
            mPreviewWidth = DEFAULT_WIDTH;
            mPreviewHeight = DEFAULT_HEIGHT;
        }
        mCameraClient.resize(mPreviewWidth, mPreviewHeight);
        mCameraClient.connect();
        open = true;
    }

    /**
     * disconnect camera 断开Camera连接
     */
    public void disconnect() {
        if (mCameraClient != null) {
            mCameraClient.disconnect();
            open = false;
        }
    }

    /**
     * release camera 释放Camera
     */
    public void release() {
        if (mCameraClient != null) {
            mCameraClient.release();
            mCameraClient = null;
            open = false;
        }
    }

    /**
     * stop camera server 停止CameraServer
     */
    public void stopServer() {
        if (mCameraClient != null) {
            mCameraClient.disconnect();
            mCameraClient.release();
            mCameraClient = null;
            open = false;
        }
    }

    /**
     * add surface to camera 添加预览Surface
     */
    public void addSurface() {
        if (mCameraClient != null && mPreviewSurface != null) {
            mCameraClient.addSurface(mPreviewSurface, false);
            addSurfaceToList(mPreviewSurface);
        }
    }

    /**
     * add sub surface to camera 添加副预览Surface
     */
    public void addSurfaceSub() {
        if (mCameraClient != null && mPreviewSurfaceSub != null) {
            mCameraClient.addSurface(mPreviewSurfaceSub, false);
            addSurfaceToList(mPreviewSurfaceSub);
        }
    }

    /**
     * add surface to camera 添加预览Surface
     *
     * @param previewSurface Surface
     */
    public void addSurface(Surface previewSurface) {
        if (mCameraClient != null && previewSurface != null) {
            mCameraClient.addSurface(previewSurface, false);
            addSurfaceToList(previewSurface);
        }
    }

    /**
     * remove the preview surface 移除预览Surface
     */
    public void removeSurface() {
        if (mCameraClient != null && mPreviewSurface != null) {
            mCameraClient.removeSurface(mPreviewSurface);
            removeSurfaceFromeList(mPreviewSurface);
        }
    }

    /**
     * remove the sub preview surface 移除副预览
     */
    public void removeSurfaceSub() {
        if (mCameraClient != null && mPreviewSurfaceSub != null) {
            mCameraClient.removeSurface(mPreviewSurfaceSub);
            removeSurfaceFromeList(mPreviewSurfaceSub);
        }
    }

    /**
     * remove the surface which you want 移除指定预览Surface
     *
     * @param previewSurface Surface
     */
    public void removeSurface(Surface previewSurface) {
        if (mCameraClient != null && previewSurface != null) {
            mCameraClient.removeSurface(previewSurface);
            removeSurfaceFromeList(previewSurface);
        }
    }

    /**
     * start recording 开始录制
     */
    public void startRecording() {
        if (mCameraClient != null) {
            mCameraClient.startRecording();
        }
    }

    /**
     * stop recording 停止录制
     */
    public void stopRecording() {
        if (mCameraClient != null && mCameraClient.isRecording()) {
            mCameraClient.stopRecording();
        }
    }

    /**
     * not support yet
     *
     * @param callback
     * @param pixelFormat
     */
    public void setFrameCallback(IFrameCallback callback, int pixelFormat) {

    }

    /**
     * the camera is recording or not 是否正在录制
     *
     * @return true:recording录制中 false:not recording未录制或者已经停止录制
     */
    public boolean isRecoeding() {
        return mCameraClient.isRecording();
    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(UsbDevice device) {
            Log.i(TAG, "attach");
            if (mCameraListener != null) {
                mCameraListener.onAttach(device);
            }

            if (isCameraDevice(device)) {
                if (!isAttached) {
                    isAttached = true;
                }
            }
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            Log.i(TAG, "USBMonitor connect");

            if (isCameraDevice(device)) {
                mControlBlock = ctrlBlock;
                if (mControlBlock != null && mControlBlock.equals(ctrlBlock)) {
                    return;
                }
                if (mCameraListener != null) {
                    mCameraListener.connected(device, ctrlBlock);
                }
            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            // XXX you should check whether the comming device equal to camera device that currently using
            Log.i(TAG, "USBMonitor disconnect");

            if (mCameraListener != null) {
                mCameraListener.disconnect(device, ctrlBlock);
            }
        }

        @Override
        public void onCancel(UsbDevice usbDevice) {

        }

        @Override
        public void onDettach(final UsbDevice device) {
            Log.i(TAG, "dettach");
            if (mCameraListener != null) {
                mCameraListener.onDettach(device);
            }
            isAttached = false;
        }

    };

    private final ICameraClientCallback mCameraCallBack = new ICameraClientCallback() {
        @Override
        public void onConnect() {
            Log.v(TAG, "onConnect:");

            if (mPreviewSurface != null) {
                mCameraClient.addSurface(mPreviewSurface, false);
                addSurfaceToList(mPreviewSurface);
            }
            if (mPreviewSurfaceSub != null) {
                mCameraClient.addSurface(mPreviewSurfaceSub, false);
                addSurfaceToList(mPreviewSurfaceSub);
            }

            // start UVCService
            final Intent intent = new Intent(mContext, UVCService.class);
            mContext.startService(intent);
        }

        @Override
        public void onDisconnect() {
            Log.v(TAG, "onDisconnect:");
        }

    };

    /**
     * get UsbControlBlock 获取UsbControlBlock
     *
     * @return UsbControlBlock
     */
    public USBMonitor.UsbControlBlock getControlBlock() {
        return mControlBlock;
    }

    /**
     * check whether the usb device get the permission
     * 判断指定设备是否已经申请了权限
     *
     * @param device UsbDevice
     * @return true:get有权限 false:not get未申请权限
     */
    public boolean hasPermission(UsbDevice device) {
        return mUSBMonitor.hasPermission(device);
    }


    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean b) {

    }

    /**
     * check camera opened 摄像头是否开启
     *
     * @return true/false
     */
    public boolean isOpen() {
        return open;
    }


    private void addSurfaceToList(Surface surface) {
        if (!surfaces.contains(surface)) {
            surfaces.add(surface);
        }
    }

    private void removeSurfaceFromeList(Surface surface) {
        surfaces.remove(surface);
    }

    /**
     * check the surface is added 判断当前surface是否被添加
     *
     * @param surface Surface
     * @return true/false
     */
    public boolean isSurfaceAdded(Surface surface) {
        return surfaces.contains(surface);
    }
}
