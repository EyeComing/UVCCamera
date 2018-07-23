/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */

package com.eyecoming.usbcamera.usbcamerahandler;

import android.app.Activity;

import com.eyecoming.usbcamera.widget.CameraViewInterface;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.UVCCamera;

/**
 * UVCCameraHandler
 *
 * @author JesseHu
 * @date 2018/7/20
 */
public class UVCCameraHandler extends AbstractUVCCameraHandler {

    /**
     * create UVCCameraHandler, use MediaVideoEncoder, try MJPEG, default bandwidth
     *
     * @param parent     Activity
     * @param cameraView CameraViewInterface
     * @param width      预览宽度
     * @param height     预览高度
     * @return UVCCameraHandler
     */
    public static final UVCCameraHandler createHandler(
            final Activity parent, final CameraViewInterface cameraView,
            final int width, final int height) {

        return createHandler(parent, cameraView, 1, width, height, UVCCamera.FRAME_FORMAT_MJPEG, UVCCamera.DEFAULT_BANDWIDTH);
    }

    /**
     * create UVCCameraHandler, use MediaVideoEncoder, try MJPEG
     *
     * @param parent          Activity
     * @param cameraView      CameraViewInterface
     * @param width           预览宽度
     * @param height          预览高度
     * @param bandwidthFactor 带宽
     * @return UVCCameraHandler
     */
    public static final UVCCameraHandler createHandler(
            final Activity parent, final CameraViewInterface cameraView,
            final int width, final int height, final float bandwidthFactor) {

        return createHandler(parent, cameraView, 1, width, height, UVCCamera.FRAME_FORMAT_MJPEG, bandwidthFactor);
    }

    /**
     * create UVCCameraHandler, try MJPEG, default bandwidth
     *
     * @param parent      Activity
     * @param cameraView  CameraViewInterface
     * @param encoderType 编码类型0:MediaSurfaceEncoder, 1:MediaVideoEncoder, 2: MediaVideoBufferEncoder
     * @param width       预览宽度
     * @param height      预览高度
     * @return UVCCameraHandler
     */
    public static final UVCCameraHandler createHandler(
            final Activity parent, final CameraViewInterface cameraView,
            final int encoderType, final int width, final int height) {

        return createHandler(parent, cameraView, encoderType, width, height, UVCCamera.FRAME_FORMAT_MJPEG, UVCCamera.DEFAULT_BANDWIDTH);
    }

    /**
     * create UVCCameraHandler, default bandwidth
     *
     * @param parent      Activity
     * @param cameraView  CameraViewInterface
     * @param encoderType 编码类型0:MediaSurfaceEncoder, 1:MediaVideoEncoder, 2: MediaVideoBufferEncoder
     * @param width       预览宽度
     * @param height      预览高度
     * @param format      编码格式 UVCCamera.FRAME_FORMAT_YUYV(0) or UVCCamera.FRAME_FORMAT_MJPEG(1)
     * @return UVCCameraHandler
     */
    public static final UVCCameraHandler createHandler(
            final Activity parent, final CameraViewInterface cameraView,
            final int encoderType, final int width, final int height, final int format) {

        return createHandler(parent, cameraView, encoderType, width, height, format, UVCCamera.DEFAULT_BANDWIDTH);
    }

    /**
     * create UVCCameraHandler
     *
     * @param parent          Activity
     * @param cameraView      CameraViewInterface
     * @param encoderType     编码类型0:MediaSurfaceEncoder, 1:MediaVideoEncoder, 2: MediaVideoBufferEncoder
     * @param width           预览宽度
     * @param height          预览高度
     * @param format          编码格式 UVCCamera.FRAME_FORMAT_YUYV(0) or UVCCamera.FRAME_FORMAT_MJPEG(1)
     * @param bandwidthFactor 带宽
     * @return UVCCameraHandler
     */
    public static final UVCCameraHandler createHandler(
            final Activity parent, final CameraViewInterface cameraView,
            final int encoderType, final int width, final int height, final int format, final float bandwidthFactor) {

        final CameraThread thread = new CameraThread(UVCCameraHandler.class, parent, cameraView, encoderType, width, height, format, bandwidthFactor);
        thread.start();
        return (UVCCameraHandler) thread.getHandler();
    }

    protected UVCCameraHandler(final CameraThread thread) {
        super(thread);
    }

    /**
     * 获取UVCCamera，并非任何时候都能获取到
     *
     * @return UVCCamera
     */
    @Override
    public UVCCamera getUvcCamera() {
        return super.getUvcCamera();
    }

    /**
     * 开始预览
     *
     * @param surface 预览所绑定的Surface
     */
    @Override
    public void startPreview(final Object surface) {
        super.startPreview(surface);
    }

    /**
     * 设置摄像头帧回调
     *
     * @param callback    IFrameCallback帧回调
     * @param pixelFormat 编码格式
     *                    <br/>0:PIXEL_FORMAT_RAW,
     *                    <br/>1:PIXEL_FORMAT_YUV,
     *                    <br/>2:PIXEL_FORMAT_RGB565,
     *                    <br/>3:PIXEL_FORMAT_RGBX,
     *                    <br/>4:PIXEL_FORMAT_NV21,
     *                    <br/>5:PIXEL_FORMAT_YUV420SP
     */
    @Override
    public void setFrameCallback(IFrameCallback callback, int pixelFormat) {
        super.setFrameCallback(callback, pixelFormat);
    }

    /**
     * 截图
     */
    @Override
    public void captureStill() {
        super.captureStill();
    }

    /**
     * 截图
     *
     * @param path 截图保存路径
     */
    @Override
    public void captureStill(final String path) {
        super.captureStill(path);
    }
}
