package com.eyecoming.usblib;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * MainActivity
 * demo 入口
 *
 * @author JesseHu
 * @date 2018/10/11
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_camera).setOnClickListener(this);
        findViewById(R.id.btn_audio).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.btn_camera:
                intent.setClass(this, CameraActivity.class);
                break;
            case R.id.btn_audio:
                intent.setClass(this, AudioActivity.class);
                break;
            default:
                break;
        }
        startActivity(intent);
    }
}
