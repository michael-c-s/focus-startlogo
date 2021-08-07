package com.assessbyphone.zambia.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.assessbyphone.zambia.R;

import static com.assessbyphone.zambia.CallbackUtils.UiUtil.getIsLogin;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (getIsLogin(SplashActivity.this).equals("1")) {
                        startActivity(new Intent(SplashActivity.this, GetMasterTemplateActivity.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this, NewLoginActivity.class));
                    }
                    finish();
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }
            }
        };
        timerThread.start();
    }
}