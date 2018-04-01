package com.panhongyuan.painter.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.panhongyuan.painter.R;

/**
 * Created by pan on 16-11-16.
 */

public class SplashActivity extends Activity {


    private TextView tv_versionName;//显示版本号的控件

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
            finish();
            //开启跳转动画
            overridePendingTransition(R.anim.next_in_anim, R.anim.next_out_anim);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //初始化控件
        initUI();
        //获取版本号并显示出来
        tv_versionName.setText("版本号" + getVersionName());
        //进入主界面
        new Thread(new Runnable() {
            @Override
            public void run() {
                enterHone();
            }
        }).start();

    }

    /**
     * 休眠3秒进入主界面
     */
    private void enterHone() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //发送空消息，提示handler进入主界面
                handler.sendEmptyMessage(0);
            }
        }).start();

    }

    private String getVersionName() {
        //获取包管理者
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            //获取版本名称
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 初始化控件方法
     */
    private void initUI() {
        //获取显示版本号的控件
        tv_versionName = (TextView) findViewById(R.id.tv_versionName);
    }
}
