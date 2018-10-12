package com.eyecoming.usblib;

import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;

import com.eyecoming.sdk.USBManager;
import com.eyecoming.sdk.bean.AudioEnableData;
import com.eyecoming.sdk.bean.AudioVolumeData;
import com.eyecoming.usbcamera.OnCameraListener;
import com.eyecoming.usbcamera.UCamera;
import com.eyecoming.usbcamera.impl.CameraCallback;
import com.eyecoming.usbcamera.usbcamerahandler.UVCCameraHandler;
import com.eyecoming.usbcamera.widget.CameraViewInterface;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * AudioActivity
 * USB Camera和Audio一起使用demo
 * 需要使用DeepVue SDK
 *
 * @author JesseHu
 * @date 2018/9/30
 */
public class AudioActivity extends AppCompatActivity implements CameraDialog.CameraDialogParent {
    private UCamera mUCamera;
    private static final int PREVIEW_WIDTH = 1280;
    private static final int PREVIEW_HEIGHT = 720;
    private CameraViewInterface mUVCCameraView;
    private UVCCameraHandler mCameraHandler;
    private boolean isFirst = true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        initView();

        setupUVCCamera();

        USBManager.init(this);
    }

    private void initView() {
        setTitle("Audio Demo");
        Button mIntentBtn = findViewById(R.id.btn_intent);
        mUVCCameraView = findViewById(R.id.camera_view);
        mIntentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCameraHandler.isPreviewing()) {
                    mCameraHandler.stopPreview();
                    return;
                }

                startPreview();
            }
        });
    }

    /**
     * 设置Camera 预览尺寸以及Camera回调
     */
    private void setupUVCCamera() {
        mUVCCameraView.setCallback(mCameraViewCallback);
        mUVCCameraView.setAspectRatio(PREVIEW_WIDTH / (float) PREVIEW_HEIGHT);
        // 预览尺寸需要Camera支持
        mCameraHandler = UVCCameraHandler.createHandler(this, mUVCCameraView, 1, PREVIEW_WIDTH, PREVIEW_HEIGHT, 1);
        mCameraHandler.addCallback(mCameraCallback);
        mUCamera = new UCamera(this, mCameraListener);
        // 设置Frame的回调以及Frame的数据格式
        mCameraHandler.setFrameCallback(mFrameCallback, UVCCamera.PIXEL_FORMAT_NV21);
    }

    private void startPreview() {
        SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
        if (st != null) {
            if (mCameraHandler.isPreviewing()) {
                mCameraHandler.stopPreview();
            }
            // 开启预览
            mCameraHandler.startPreview(new Surface(st));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUCamera.monitorRegister();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUVCCameraView != null) {
            mUVCCameraView.onPause();
        }
        if (mCameraHandler != null) {
            mCameraHandler.release();
            mCameraHandler = null;
        }
        mUCamera.monitorUnregister();
        mUCamera.monitorDestory();
        mUVCCameraView = null;
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mUCamera.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean b) {

    }

    private OnCameraListener mCameraListener = new OnCameraListener() {
        @Override
        public void connected(UsbDevice usbDevice, USBMonitor.UsbControlBlock ctrlBlock) {
            if (mCameraHandler != null) {
                // UVCCameraHandler.open()为异步
                mCameraHandler.open(ctrlBlock);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 使用UVCCameraHandler.getUvcCamera()获取UVCCamera对象时需要确保UVCCameraHandler.open()已经执行完
                        UVCCamera uvcCamera = mCameraHandler.getUvcCamera();
                    }
                }, 1000);
                startPreview();
            }
        }

        @Override
        public void disconnect(UsbDevice usbDevice, USBMonitor.UsbControlBlock usbControlBlock) {

        }

        @Override
        public void onAttach(UsbDevice device) {
            if (USBUtil.getDeviceType(device) == USBUtil.TYPE_DEVICE_CAMERA) {
                mUCamera.getFirstUsbCameraDevice();
            }
            if (USBUtil.getDeviceType(device) == USBUtil.TYPE_DEVICE_AUDIO) {
                USBManager usbManager = USBManager.getInstance();
                AudioEnableData audioEnableData = new AudioEnableData();
                audioEnableData.setEnable(true);
                usbManager.sendInterruptData(audioEnableData);

                AudioVolumeData audioVolumeData = new AudioVolumeData();
                audioVolumeData.setVolume(7);
                usbManager.sendInterruptData(audioVolumeData);
            }
        }

        @Override
        public void onDettach(UsbDevice device) {
            // 在USB设备断开的时候close UVCCameraHandler防止再次连接的时候打开Camera失败
            if (mCameraHandler != null) {
                if (mCameraHandler.isPreviewing()) {
                    mCameraHandler.stopPreview();
                }
                if (mCameraHandler.isOpened()) {
                    mCameraHandler.close();
                }
            }
        }
    };

    /**
     * CameraView回调
     */
    private CameraViewInterface.Callback mCameraViewCallback = new CameraViewInterface.Callback() {
        @Override
        public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
            if (mCameraHandler.isPreviewing()) {
                mCameraHandler.stopPreview();
            }
            //第一次打开activity
            if (isFirst) {
                isFirst = false;
            } else {
                startPreview();
            }
        }

        @Override
        public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {

        }

        @Override
        public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
            Log.i("CameraViewInterface", "onSurfaceDestroy: ");
        }

        @Override
        public void onSurfaceUpdated(CameraViewInterface cameraViewInterface, Surface surface) {

        }
    };

    /**
     * CameraHandler回调
     */
    private CameraCallback mCameraCallback = new CameraCallback() {
        @Override
        public void onOpen(UVCCamera uvcCamera) {
            //  UVCCameraHandler.open()执行完成后回调，同时返回当前UVCCamera对象

            // 获取USB Camera支持的分辨率
            // 方式一
            String supportedSizeStr = uvcCamera.getSupportedSize();
            // type参数可以通过解析supportedSizeStr，值为6或4
            List<Size> supportedSize = UVCCamera.getSupportedSize(6, supportedSizeStr);

            // 方式二
            List<Size> supportedSizeList = uvcCamera.getSupportedSizeList();

            // 方式三 通过解析json(supportedSizeStr)数据获取
        }

        @Override
        public void onClose() {

        }

        @Override
        public void onStartPreview() {

        }

        @Override
        public void onStopPreview() {

        }

        @Override
        public void onStartRecording() {

        }

        @Override
        public void onStopRecording() {

        }

        @Override
        public void onError(Exception e) {

        }
    };

    /**
     * 每帧图像的回调
     */
    private IFrameCallback mFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(final ByteBuffer frame) {
            //处理每一帧的数据
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else {

        }
    }
}
