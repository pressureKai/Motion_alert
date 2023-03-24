package com.james.motion.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;


import com.james.motion.R;
import com.james.motion.ui.heart.BaseActivity;
import com.james.motion.ui.heart.main.HeartMainActivity;
import com.james.motion.ui.sleep.SleepMainActivity;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_CODE_FOR_PERMISSION = 0;
    private static final int PERMISSION_DENY = 1;
    private static final int GO_MAIN = 2;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PERMISSION_DENY:
                    finish();
                    break;
                case GO_MAIN:
                    Intent intent = new Intent(MainActivity.this, HeartMainActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };


    @Override
    protected void initViews() {

    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_main;
    }

    @Override
    protected void doAfterInitView() {
        findViewById(R.id.sport_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });


        findViewById(R.id.heart_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 23) {
                    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    if (checkandrequestPermissions(permissions, REQUEST_CODE_FOR_PERMISSION)){
                        mHandler.sendEmptyMessageDelayed(GO_MAIN, 2000);
                    }
                } else {
                    mHandler.sendEmptyMessageDelayed(GO_MAIN, 2000);
                }
            }
        });


        findViewById(R.id.sleep_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SleepMainActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public int getStatusBarType() {
        return BaseActivity.TRANSPARENT_STATUS_BAR;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_FOR_PERMISSION:
                if (getUngrantedPermissions(permissions).length == 0)
                    mHandler.sendEmptyMessageDelayed(GO_MAIN, 2000);
                else
                    mHandler.sendEmptyMessageDelayed(PERMISSION_DENY, 2000);
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
