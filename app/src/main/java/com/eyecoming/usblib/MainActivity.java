package com.eyecoming.usblib;

import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;

import com.eyecoming.usbcamera.OnCameraListener;
import com.eyecoming.usbcamera.UCamera;
import com.eyecoming.usbcamera.usbcamerahandler.UVCCameraHandler;
import com.eyecoming.usbcamera.widget.CameraViewInterface;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.nio.ByteBuffer;

/**
 * MainActivity
 *
 * @author JesseHu
 * @date 2018/7/19
 */
public final class MainActivity extends AppCompatActivity implements CameraDialog.CameraDialogParent {

    private UCamera mUCamera;
    private static final int PREVIEW_WIDTH = 1280;
    private static final int PREVIEW_HEIGHT = 720;
    private CameraViewInterface mUVCCameraView;
    private UVCCameraHandler mCameraHandler;
    private boolean isFirst = true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button mIntentBtn = findViewById(R.id.btn_intent);
        mUVCCameraView = findViewById(R.id.camera_view);
        mUVCCameraView.setAspectRatio(PREVIEW_WIDTH / (float) PREVIEW_HEIGHT);
        mCameraHandler = UVCCameraHandler.createHandler(this, mUVCCameraView, 1, PREVIEW_WIDTH, PREVIEW_HEIGHT, 1);
        mUCamera = new UCamera(this, true, new OnCameraListener() {
            @Override
            public void connected(UVCCamera camera, USBMonitor.UsbControlBlock ctrlBlock) {
                if (mCameraHandler != null) {
                    mCameraHandler.open(ctrlBlock);
                    startPreview();
                }
            }

            @Override
            public void disconnect() {

            }

            @Override
            public void onAttach(UsbDevice device) {

            }

            @Override
            public void onDettach(UsbDevice device) {

            }
        });

        mIntentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCameraHandler.isPreviewing()) {
//                    mCameraHandler.close();
                    mCameraHandler.stopPreview();
                    return;
                }
                //重新连接camera
//                mUCamera.getFirstUsbCameraDevice();
                startPreview();
            }
        });
        mUVCCameraView.setCallback(new CameraViewInterface.Callback() {
            @Override
            public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
                if (mCameraHandler.isPreviewing()) {
                    mCameraHandler.stopPreview();
                }
                //第一次打开activity
                if (isFirst) {
                    isFirst = false;
                } else {
//                    mUCamera.getFirstUsbCameraDevice();
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
        });
    }

    private void startPreview() {
        SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
        if (st != null) {
            mCameraHandler.startPreview(new Surface(st));
            mCameraHandler.setFrameCallback(mFrameCallback, UVCCamera.PIXEL_FORMAT_NV21);
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


    private final IFrameCallback mFrameCallback = new IFrameCallback() {
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
