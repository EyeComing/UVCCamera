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

package com.eyecoming.usbcamera.widget;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import com.eyecoming.usbcamera.encoder.IVideoEncoder;
import com.serenegiant.widget.IAspectRatioView;

/**
 * CameraViewInterface
 *
 * @author JesseHu
 * @date 2018/7/20
 */
public interface CameraViewInterface extends IAspectRatioView {
    interface Callback {
        /**
         * Surface创建完成
         *
         * @param view    CameraViewInterface
         * @param surface Surface
         */
        void onSurfaceCreated(CameraViewInterface view, Surface surface);

        /**
         * Surface发生改变
         *
         * @param view    CameraViewInterface
         * @param surface Surface
         * @param width   宽度
         * @param height  高度
         */
        void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height);

        /**
         * Surface销毁
         *
         * @param view    CameraViewInterface
         * @param surface Surface
         */
        void onSurfaceDestroy(CameraViewInterface view, Surface surface);
    }

    /**
     * 暂停
     */
    void onPause();

    /**
     * 继续
     */
    void onResume();

    /**
     * 设置Surface回调
     *
     * @param callback CameraViewInterface.Callback
     */
    void setCallback(Callback callback);

    /**
     * 获取SurfaceTexture
     *
     * @return SurfaceTexture
     */
    SurfaceTexture getSurfaceTexture();

    /**
     * 获取Surface
     *
     * @return Surface
     */
    Surface getSurface();

    /**
     * 是否存在Surface
     *
     * @return true:存在，false:不存在
     */
    boolean hasSurface();

    /**
     * 设置视频编码器
     *
     * @param encoder IVideoEncoder编码器
     */
    void setVideoEncoder(final IVideoEncoder encoder);

    /**
     * 获取静态图片
     *
     * @return Bitmap
     */
    Bitmap captureStillImage();
}
