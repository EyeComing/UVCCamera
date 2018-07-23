package com.eyecoming.usbcamera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

/**
 * USBAttachedActivity
 *
 * @author JesseHu
 * @date 2018/7/23
 */
public class USBAttachedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Window window = getWindow();
        WindowManager.LayoutParams wl = window.getAttributes();
        //这句就是设置窗口里控件的透明度的．0.0全透明．1.0不透明．
        wl.alpha = 0.0f;
        window.setAttributes(wl);
        super.onCreate(savedInstanceState);
        finish();
    }

    @Override
    protected void onPause() {
        overridePendingTransition(0, 0);
        super.onPause();
    }
}
