package com.eyecoming.usblib;

import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.eyecoming.usbcamera.OnCameraListener;
import com.eyecoming.usbcamera.UCamera;
import com.eyecoming.usbcamera.usbcamerahandler.UVCCameraHandler;
import com.eyecoming.usbcamera.widget.CameraViewInterface;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private Button mIntentBtn;
    public USBMonitor.UsbControlBlock mCtrlBlock;
    private boolean isFirst = true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mIntentBtn = findViewById(R.id.btn_intent);
        mUVCCameraView = findViewById(R.id.camera_view);
        mUVCCameraView.setAspectRatio(PREVIEW_WIDTH / (float) PREVIEW_HEIGHT);
        mUCamera = new UCamera(this, true, new OnCameraListener() {
            @Override
            public void connected(UVCCamera camera, USBMonitor.UsbControlBlock ctrlBlock) {
                mCtrlBlock = ctrlBlock;
                mCameraHandler.open(ctrlBlock);
                SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
                if (st != null) {
                    mCameraHandler.startPreview(new Surface(st));
//                UVCCamera uvcCamera = mCameraHandler.getUvcCamera();
//                uvcCamera.setFrameCallback(mFrameCallback, UVCCamera.PIXEL_FORMAT_NV21);
                    mCameraHandler.setFrameCallback(mFrameCallback, UVCCamera.PIXEL_FORMAT_NV21);

                }
            }

            @Override
            public void disconnect() {

            }
        });
        mCameraHandler = UVCCameraHandler.createHandler(this, mUVCCameraView, 1, PREVIEW_WIDTH, PREVIEW_HEIGHT, 1);

        mIntentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCameraHandler.isPreviewing()) {
                    mCameraHandler.close();
                    return;
                }
                mUCamera.getFirstUsbCameraDevice();
            }
        });
        mUVCCameraView.setCallback(new CameraViewInterface.Callback() {
            @Override
            public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
                if (!mCameraHandler.isPreviewing()) {
                    //第一次打开activity
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        mUCamera.getFirstUsbCameraDevice();
                    }
                }
            }

            @Override
            public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {

            }

            @Override
            public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUCamera.monitorRegister();
        if (mUVCCameraView != null) {
            mUVCCameraView.onResume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCameraHandler.close();
        if (mUVCCameraView != null) {
            mUVCCameraView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    private ExecutorService fixedExecturoService = Executors.newFixedThreadPool(1);
    byte[] decoding = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 3 / 2];
    private final IFrameCallback mFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(final ByteBuffer frame) {
            frame.clear();
            frame.get(decoding, 0, frame.remaining());
            fixedExecturoService.execute(new Runnable() {
                @Override
                public void run() {
                    //处理每一帧的数据
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(MainActivity.this, "onFrame回调触发", Toast.LENGTH_SHORT).show();
//                        }
//                    });

                    getFile(decoding, Environment.getExternalStorageDirectory().getAbsolutePath(), "yuv.yuv");
                }
            });
        }
    };

    public static void getFile(byte[] bfile, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            //判断文件目录是否存在
            if (!dir.exists() && dir.isDirectory()) {
                dir.mkdirs();
            }
//            file = new File(filePath + File.separator + fileName);
            file = new File(filePath, fileName);
//            if (file.exists()) {
//                file.delete();
//            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
