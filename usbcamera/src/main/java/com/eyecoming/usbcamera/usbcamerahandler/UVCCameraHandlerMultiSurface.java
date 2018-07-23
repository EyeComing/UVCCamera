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
import android.view.Surface;

import com.eyecoming.usbcamera.widget.CameraViewInterface;
import com.serenegiant.glutils.RendererHolder;
import com.serenegiant.usb.UVCCamera;

/**
 * UVCCameraHandlerMultiSurface多个预览视图
 *
 * @author JesseHu
 * @date 2018/7/20
 */
public class UVCCameraHandlerMultiSurface extends AbstractUVCCameraHandler {
    /**
     * create UVCCameraHandlerMultiSurface, use MediaVideoEncoder, try MJPEG, default bandwidth
     *
     * @param parent     Activity
     * @param cameraView CameraViewInterface
     * @param width      预览宽度
     * @param height     预览高度
     * @return UVCCameraHandlerMultiSurface
     */
    public static final UVCCameraHandlerMultiSurface createHandler(
            final Activity parent, final CameraViewInterface cameraView,
            final int width, final int height) {

        return createHandler(parent, cameraView, 1, width, height, UVCCamera.FRAME_FORMAT_MJPEG, UVCCamera.DEFAULT_BANDWIDTH);
    }

    /**
     * create UVCCameraHandlerMultiSurface, use MediaVideoEncoder, try MJPEG
     *
     * @param parent          Activity
     * @param cameraView      CameraViewInterface
     * @param width           预览宽度
     * @param height          预览高度
     * @param bandwidthFactor 带宽
     * @return UVCCameraHandlerMultiSurface
     */
    public static final UVCCameraHandlerMultiSurface createHandler(
            final Activity parent, final CameraViewInterface cameraView,
            final int width, final int height, final float bandwidthFactor) {

        return createHandler(parent, cameraView, 1, width, height, UVCCamera.FRAME_FORMAT_MJPEG, bandwidthFactor);
    }

    /**
     * create UVCCameraHandlerMultiSurface, try MJPEG, default bandwidth
     *
     * @param parent      Activity
     * @param cameraView  CameraViewInterface
     * @param encoderType 编码类型0:MediaSurfaceEncoder, 1:MediaVideoEncoder, 2: MediaVideoBufferEncoder
     * @param width       预览宽度
     * @param height      预览高度
     * @return UVCCameraHandlerMultiSurface
     */
    public static final UVCCameraHandlerMultiSurface createHandler(
            final Activity parent, final CameraViewInterface cameraView,
            final int encoderType, final int width, final int height) {

        return createHandler(parent, cameraView, encoderType, width, height, UVCCamera.FRAME_FORMAT_MJPEG, UVCCamera.DEFAULT_BANDWIDTH);
    }

    /**
     * create UVCCameraHandlerMultiSurface, default bandwidth
     *
     * @param parent      Activity
     * @param cameraView  CameraViewInterface
     * @param encoderType 编码类型
     * @param width       预览宽度
     * @param height      预览高度
     * @param format      编码格式 UVCCamera.FRAME_FORMAT_YUYV(0) or UVCCamera.FRAME_FORMAT_MJPEG(1)
     * @return UVCCameraHandlerMultiSurface
     */
    public static final UVCCameraHandlerMultiSurface createHandler(
            final Activity parent, final CameraViewInterface cameraView,
            final int encoderType, final int width, final int height, final int format) {

        return createHandler(parent, cameraView, encoderType, width, height, format, UVCCamera.DEFAULT_BANDWIDTH);
    }

    /**
     * create UVCCameraHandlerMultiSurface
     *
     * @param parent          Activity
     * @param cameraView      CameraViewInterface
     * @param encoderType     0: use MediaSurfaceEncoder, 1: use MediaVideoEncoder, 2: use MediaVideoBufferEncoder
     * @param width           预览宽度
     * @param height          预览高度
     * @param format          编码格式 UVCCamera.FRAME_FORMAT_YUYV(0) or UVCCamera.FRAME_FORMAT_MJPEG(1)
     * @param bandwidthFactor 带宽
     * @return UVCCameraHandlerMultiSurface
     */
    public static final UVCCameraHandlerMultiSurface createHandler(
            final Activity parent, final CameraViewInterface cameraView,
            final int encoderType, final int width, final int height, final int format, final float bandwidthFactor) {

        final CameraThread thread = new CameraThread(UVCCameraHandlerMultiSurface.class, parent, cameraView, encoderType, width, height, format, bandwidthFactor);
        thread.start();
        return (UVCCameraHandlerMultiSurface) thread.getHandler();
    }

    private RendererHolder mRendererHolder;

    protected UVCCameraHandlerMultiSurface(final CameraThread thread) {
        super(thread);
        mRendererHolder = new RendererHolder(thread.getWidth(), thread.getHeight(), null);
    }

    @Override
    public synchronized void release() {
        if (mRendererHolder != null) {
            mRendererHolder.release();
            mRendererHolder = null;
        }
        super.release();
    }

    @Override
    public synchronized void resize(final int width, final int height) {
        super.resize(width, height);
        if (mRendererHolder != null) {
            mRendererHolder.resize(width, height);
        }
    }

    /**
     * 开启预览
     */
    public synchronized void startPreview() {
        checkReleased();
        if (mRendererHolder != null) {
            super.startPreview(mRendererHolder.getSurface());
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * 添加预览Surface
     *
     * @param surfaceId    对应Surface的ID
     * @param surface      预览Surface
     * @param isRecordable 是否允许录制
     */
    public synchronized void addSurface(final int surfaceId, final Surface surface, final boolean isRecordable) {
        checkReleased();
        mRendererHolder.addSurface(surfaceId, surface, isRecordable);
    }

    /**
     * 移除预览的Surface
     *
     * @param surfaceId 对应Surface的ID
     */
    public synchronized void removeSurface(final int surfaceId) {
        if (mRendererHolder != null) {
            mRendererHolder.removeSurface(surfaceId);
        }
    }

    /**
     * 截图
     */
    @Override
    public void captureStill() {
        checkReleased();
        super.captureStill();
    }

    /**
     * 截图
     *
     * @param path 截图保存的路径
     */
    @Override
    public void captureStill(final String path) {
        checkReleased();
        post(new Runnable() {
            @Override
            public void run() {
                synchronized (UVCCameraHandlerMultiSurface.this) {
                    if (mRendererHolder != null) {
                        mRendererHolder.captureStill(path);
                        updateMedia(path);
                    }
                }
            }
        });
    }
}
