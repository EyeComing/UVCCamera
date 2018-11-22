package com.eyecoming.usbcamera;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.util.Log;
import android.view.Surface;

import com.eyecoming.usbcamera.impl.CameraClientCallback;
import com.eyecoming.usbcamera.impl.CameraStatusCallback;
import com.eyecoming.usbcamera.service.UVCService;
import com.eyecoming.usbcamera.serviceclient.CameraClient;
import com.eyecoming.usbcamera.serviceclient.ICameraClientCallback;
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
public class UCameraServer {
    private final static String TAG = "UCameraServer";
    public static final int STATUS_UNKNOWN = -1;
    public static final int STATUS_ATTACHED = 0;
    public static final int STATUS_CONNECTED = 1;
    public static final int STATUS_DISCONNECTED = 2;
    public static final int STATUS_DETACHED = 3;
    public static final int STATUS_OPEN_READY = 4;
    public static final int STATUS_OPENING = 5;
    public static final int STATUS_OPENED = 6;
    public static final int STATUS_CLOSING = 7;
    public static final int STATUS_CLOSED = 8;
    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 720;

    private static UCameraServer mServer;
    private Context mContext;
    private USBMonitor mUSBMonitor;
    private USBMonitor.UsbControlBlock mControlBlock;
    private CameraClient mCameraClient;

    private boolean isAttached = false;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private Surface mPreviewSurface;
    private Surface mPreviewSurfaceSub;
    private List<Surface> surfaces;
    private int mFilterId;
    private int currentStatus = STATUS_DETACHED;

    private OnCameraListener mCameraListener;
    private CameraClientCallback mCameraClientCallback;
    private CameraStatusCallback mCameraStatusCallback;

    /**
     * initialization(with a custom filter for usb) 初始化,可自定义usb过滤
     *
     * @param mContext Context
     * @param filterId usb device filter id
     */
    private UCameraServer(Context mContext, int filterId) {
        this.mContext = mContext;
        this.mFilterId = filterId;
        surfaces = new ArrayList<>();
    }

    /**
     * get UCameraServer Instance
     *
     * @return UCameraServer
     */
    public static UCameraServer getInstance() {
        return mServer;
    }

    /**
     * initialization 初始化
     *
     * @param context Context
     */
    public static void init(Context context) {
        init(context, R.xml.device_filter);
    }

    /**
     * initialization 初始化
     *
     * @param context  Context
     * @param filterId usb device filter id
     */
    public static void init(Context context, int filterId) {
        mServer = new UCameraServer(context, filterId);
    }

    /**
     * init USB monitor
     */
    public USBMonitor initUSBMonitor() {
        mUSBMonitor = new USBMonitor(mContext, mOnDeviceConnectListener);
        List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(mContext, mFilterId);
        mUSBMonitor.setDeviceFilter(filters);
        return mUSBMonitor;
    }

    /**
     * init USB monitor
     *
     * @param deviceConnectListener OnDeviceConnectListener
     */
    public USBMonitor initUSBMonitor(USBMonitor.OnDeviceConnectListener deviceConnectListener) {
        mUSBMonitor = new USBMonitor(mContext, deviceConnectListener);
        List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(mContext, mFilterId);
        mUSBMonitor.setDeviceFilter(filters);
        return mUSBMonitor;
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
     * set the camera client callback 设置CameraClient回调
     *
     * @param cameraClientCallback CameraClientCallback
     */
    public void setCameraClientCallback(CameraClientCallback cameraClientCallback) {
        this.mCameraClientCallback = cameraClientCallback;
    }

    /**
     * set the camera status callback 设置camera状态回调
     *
     * @param cameraStatusCallback CameraStatusCallback
     */
    public void setCameraStatusCallback(CameraStatusCallback cameraStatusCallback) {
        this.mCameraStatusCallback = cameraStatusCallback;
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
        if (mPreviewSurface != null && !isSurfaceAdded(mPreviewSurface)) {
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
        if (mPreviewSurfaceSub != null && !isSurfaceAdded(mPreviewSurfaceSub)) {
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
        if (mCameraClient != null && mPreviewSurface != null && !isSurfaceAdded(mPreviewSurface)) {
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
        openCamera(getFirstUsbCameraDevice());
    }

    /**
     * open camera 开启摄像头
     *
     * @param device UsbDevice
     */
    public void openCamera(UsbDevice device) {
        if (mCameraClient == null) {
            mCameraClient = new CameraClient(mContext, mCameraCallBack);
        }
        mCameraClient.select(device);
        currentStatus = STATUS_OPEN_READY;
        if (mCameraStatusCallback != null) {
            mCameraStatusCallback.cameraStatus(STATUS_OPEN_READY);
        }

        if (mPreviewWidth == 0 || mPreviewHeight == 0) {
            mPreviewWidth = DEFAULT_WIDTH;
            mPreviewHeight = DEFAULT_HEIGHT;
        }
        mCameraClient.resize(mPreviewWidth, mPreviewHeight);
        mCameraClient.connect();
        currentStatus = STATUS_OPENING;
        if (mCameraStatusCallback != null) {
            mCameraStatusCallback.cameraStatus(STATUS_OPENING);
        }
    }

    /**
     * disconnect camera 断开Camera连接
     */
    public void disconnect() {
        if (mCameraClient != null) {
            mCameraClient.disconnect();
            if (currentStatus != STATUS_CLOSED) {
                currentStatus = STATUS_CLOSING;
                if (mCameraStatusCallback != null) {
                    mCameraStatusCallback.cameraStatus(STATUS_CLOSING);
                }
            }
        }
    }

    /**
     * release camera 释放Camera
     */
    private void release() {
        if (mCameraClient != null) {
            mCameraClient.disconnect();
            mCameraClient.release();
            mCameraClient = null;
            if (currentStatus != STATUS_CLOSED) {
                currentStatus = STATUS_CLOSING;
                if (mCameraStatusCallback != null) {
                    mCameraStatusCallback.cameraStatus(STATUS_CLOSING);
                }
            }
        }
    }

    /**
     * stop camera server 停止CameraServer
     */
    public void stopServer() {
        removeSurface();
        removeSurfaceSub();
        release();
    }

    /**
     * add surface to camera 添加预览Surface
     */
    public void addSurface() {
        if (mCameraClient != null && mPreviewSurface != null && !isSurfaceAdded(mPreviewSurface)) {
            mCameraClient.addSurface(mPreviewSurface, false);
            addSurfaceToList(mPreviewSurface);
        }
    }

    /**
     * add sub surface to camera 添加副预览Surface
     */
    public void addSurfaceSub() {
        if (mCameraClient != null && mPreviewSurfaceSub != null && !isSurfaceAdded(mPreviewSurfaceSub)) {
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
        if (mCameraClient != null && previewSurface != null && !isSurfaceAdded(previewSurface)) {
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
        if (mCameraClient.isRecording()) {
            mCameraClient.stopRecording();
        }
    }

    /**
     * not support yet
     *
     * @param callback    IFrameCallback
     * @param pixelFormat
     */
    public void setFrameCallback(IFrameCallback callback, int pixelFormat) {
        throw new RuntimeException("frame callback is not supported yet");
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
                    currentStatus = STATUS_ATTACHED;
                    if (mCameraStatusCallback != null) {
                        mCameraStatusCallback.cameraStatus(STATUS_ATTACHED);
                    }
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
                currentStatus = STATUS_CONNECTED;
                if (mCameraStatusCallback != null) {
                    mCameraStatusCallback.cameraStatus(STATUS_CONNECTED);
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
            currentStatus = STATUS_DISCONNECTED;
            if (mCameraStatusCallback != null) {
                mCameraStatusCallback.cameraStatus(STATUS_DISCONNECTED);
            }
        }

        @Override
        public void onCancel(UsbDevice usbDevice) {

        }

        @Override
        public void onDettach(final UsbDevice device) {
            Log.i(TAG, "detach");
            if (mCameraListener != null) {
                mCameraListener.onDetach(device);
            }
            isAttached = false;
            currentStatus = STATUS_DETACHED;
            if (mCameraStatusCallback != null) {
                mCameraStatusCallback.cameraStatus(STATUS_DETACHED);
            }
        }

    };

    /**
     * Camera client callback
     */
    private final ICameraClientCallback mCameraCallBack = new ICameraClientCallback() {
        @Override
        public void onConnect() {
            Log.v(TAG, "onConnect:");

            addSurface();
            addSurfaceSub();

            // start UVCService
            Intent mService = new Intent(mContext, UVCService.class);
            mContext.startService(mService);

            if (mCameraClientCallback != null) {
                mCameraClientCallback.onConnected();
            }

            currentStatus = STATUS_OPENED;
            if (mCameraStatusCallback != null) {
                mCameraStatusCallback.cameraStatus(STATUS_OPENED);
            }
        }

        @Override
        public void onDisconnect() {
            Log.v(TAG, "onDisconnect:");
            if (mCameraClientCallback != null) {
                mCameraClientCallback.onDisconnected();
            }
            currentStatus = STATUS_CLOSED;
            if (mCameraStatusCallback != null) {
                mCameraStatusCallback.cameraStatus(STATUS_CLOSED);
            }
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

    /**
     * check camera opened 摄像头是否开启
     *
     * @return true/false
     */
    public boolean isOpen() {
        return currentStatus == STATUS_OPENED;
    }

    /**
     * get the status of camera 获取当前Camera状态
     *
     * @return camera status Camera状态
     */
    public int getCameraStatus() {
        return currentStatus;
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
