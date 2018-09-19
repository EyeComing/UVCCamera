package com.eyecoming.usbcamera.agora;

import android.opengl.EGLContext;
import android.opengl.Matrix;

import java.util.Arrays;


public class ImgTextureFrame {
    public static final int NO_TEXTURE = -1;
    public static final float[] DEFAULT_MATRIX = new float[16];
    public int mTextureId = NO_TEXTURE;
    public final float[] mTexMatrix;
    public int mWidth;
    public int mHeight;
    public long pts;
    public long dts;
    public EGLContext eglContext;

    public ImgTextureFrame(int width, int height, int textureId, float[] matrix, EGLContext eglContext, long ts) {
        this.mWidth = width;
        this.mHeight = height;
        this.mTextureId = textureId;
        this.eglContext = eglContext;
        this.pts = ts;
        this.dts = ts;

        if (matrix != null && matrix.length == 16) {
            this.mTexMatrix = matrix;
        } else {
            this.mTexMatrix = DEFAULT_MATRIX;
            Matrix.setIdentityM(this.mTexMatrix, 0);
        }
    }

    @Override
    public String toString() {
        return "ImgTextureFrame{" +
                "mWidth=" + mWidth +
                "mHeight=" + mHeight +
                ", mTextureId=" + mTextureId +
                ", mTexMatrix=" + Arrays.toString(mTexMatrix) +
                '}';
    }
}
