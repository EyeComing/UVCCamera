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

    public UCameraServer(Context mContext) {
        this.mContext = mContext;
        surfaces = new ArrayList<>();
        mUSBMonitor = new USBMonitor(mContext, mOnDeviceConnectListener);
        mCameraClient = new CameraClient(mContext, mCameraCallBack);
    }

    public UCameraServer(Context mContext, OnCameraListener mCameraListener) {
        this(mContext);
        this.mCameraListener = mCameraListener;
    }

    public UCameraServer(Context mContext, USBMonitor.OnDeviceConnectListener deviceConnectListener) {
        this.mContext = mContext;
        surfaces = new ArrayList<>();
        mUSBMonitor = new USBMonitor(mContext, deviceConnectListener);
        mCameraClient = new CameraClient(mContext, mCameraCallBack);
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

    /**
     * 设置连接监听器
     *
     * @param cameraListener 连接监听
     */
    public void setOnCameraListener(OnCameraListener cameraListener) {
        this.mCameraListener = cameraListener;
    }

    /**
     * 设置预览尺寸
     *
     * @param width  预览宽度
     * @param height 预览高度
     */
    public void setPreviewSize(int width, int height) {
        this.mPreviewWidth = width;
        this.mPreviewHeight = height;
    }

    /**
     * 设置预览显示
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
     * 设置副预览
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
     * 设置预览
     *
     * @param mPreviewSurface Surface显示
     * @param width           预览宽度
     * @param height          预览高度
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
     * @param mPreviewSurfaceSub 副预览
     * @param width              预览宽度
     * @param height             预览高度
     */
    public void setPreView(Surface mPreviewSurface, Surface mPreviewSurfaceSub, int width, int height) {
        this.mPreviewSurface = mPreviewSurface;
        this.mPreviewSurfaceSub = mPreviewSurfaceSub;
        this.mPreviewWidth = width;
        this.mPreviewHeight = height;
        if (mPreviewSurface != null) {
            mCameraClient.addSurface(mPreviewSurface, false);
            addSurfaceToList(mPreviewSurface);
        }
        if (mPreviewSurfaceSub != null) {
            mCameraClient.addSurface(mPreviewSurfaceSub, false);
            addSurfaceToList(mPreviewSurfaceSub);
        }
    }

    /**
     * 设置默认摄像头过滤器
     */
    public void setDeviceFilter() {
        final List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(mContext, R.xml.device_filter);
        mUSBMonitor.setDeviceFilter(filters);
    }

    /**
     * 设置自定义摄像头过滤器
     *
     * @param deviceFilterId 摄像头过滤文件ID(只限xml文件夹)
     */
    public void setDeviceFilter(int deviceFilterId) {
        final List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(mContext, deviceFilterId);
        mUSBMonitor.setDeviceFilter(filters);
    }

    /**
     * 获取第一个摄像头设备
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
     * 开启摄像头
     */
    public void openCamera() {
        if (mCameraClient == null) {
            mCameraClient = new CameraClient(mContext, mCameraCallBack);
        }
        mCameraClient.select(getFirstUsbCameraDevice());
        mCameraClient.resize(mPreviewWidth == 0 ? DEFAULT_WIDTH : mPreviewWidth, mPreviewHeight == 0 ? DEFAULT_HEIGHT : mPreviewHeight);
        mCameraClient.connect();
        open = true;
    }

    /**
     * 断开Camera连接
     */
    public void disconnect() {
        if (mCameraClient != null) {
            mCameraClient.disconnect();
            open = false;
        }
    }

    /**
     * 释放Camera
     */
    public void release() {
        if (mCameraClient != null) {
            mCameraClient.release();
            mCameraClient = null;
            open = false;
        }
    }

    /**
     * 停止CameraServer
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
     * 添加预览Surface
     */
    public void addSurface() {
        if (mPreviewSurface != null) {
            mCameraClient.addSurface(mPreviewSurface, false);
            addSurfaceToList(mPreviewSurface);
        }
    }

    /**
     * 添加预览Surface
     *
     * @param previewSurface Surface
     */
    public void addSurface(Surface previewSurface) {
        if (previewSurface != null) {
            mCameraClient.addSurface(previewSurface, false);
            addSurfaceToList(previewSurface);
        }
    }

    /**
     * 移除预览Surface
     */
    public void removeSurface() {
        if (mPreviewSurface != null) {
            mCameraClient.removeSurface(mPreviewSurface);
            removeSurfaceFromeList(mPreviewSurface);
        }
    }

    /**
     * 移除副预览
     */
    public void removeSurfaceSub() {
        if (mPreviewSurfaceSub != null) {
            mCameraClient.removeSurface(mPreviewSurfaceSub);
            removeSurfaceFromeList(mPreviewSurfaceSub);
        }
    }

    /**
     * 移除指定预览Surface
     *
     * @param previewSurface Surface
     */
    public void removeSurface(Surface previewSurface) {
        if (previewSurface != null) {
            mCameraClient.removeSurface(previewSurface);
            removeSurfaceFromeList(previewSurface);
        }
    }

    /**
     * 开始录制
     */
    public void startRecording() {
        if (mCameraClient != null) {
            mCameraClient.startRecording();
        }
    }

    /**
     * 停止录制
     */
    public void stopRecording() {
        if (mCameraClient != null && mCameraClient.isRecording()) {
            mCameraClient.stopRecording();
        }
    }

    /**
     * 是否正在录制
     *
     * @return true:录制中 false:未录制或者已经停止录制
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
//            if (mPreviewSurface != null) {
//                mCameraClient.addSurface(mPreviewSurface, false);
//                addSurfaceToList(mPreviewSurface);
//                if (mPreviewSurfaceSub != null) {
//                    mCameraClient.addSurface(mPreviewSurfaceSub, false);
//                    addSurfaceToList(mPreviewSurfaceSub);
//                }
//            }

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


    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean b) {

    }

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

    public boolean isSurfaceAdded(Surface surface) {
        return surfaces.contains(surface);
    }
}
