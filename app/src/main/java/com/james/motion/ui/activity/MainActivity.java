package com.james.motion.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.james.motion.MyApplication;
import com.james.motion.R;
import com.james.motion.commmon.bean.HealthRecord;
import com.james.motion.commmon.bean.NetBean;
import com.james.motion.commmon.bean.NetRecoderBean;
import com.james.motion.commmon.utils.Conn;
import com.james.motion.commmon.utils.MySp;
import com.james.motion.db.DataManager;
import com.james.motion.db.RealmHelper;
import com.james.motion.ui.adapter.HealthAdapter;
import com.james.motion.ui.heart.BaseActivity;
import com.james.motion.ui.heart.main.HeartMainActivity;
import com.james.motion.ui.sleep.SleepMainActivity;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_CODE_FOR_PERMISSION = 0;
    private static final int PERMISSION_DENY = 1;
    private static final int GO_MAIN = 2;


    private RecyclerView list;
    private ImageView tvEmpty;
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
                    startActivityForResult(intent,99);
                    break;
            }
        }
    };


    @Override
    protected void initViews() {

        dataManager = new DataManager(new RealmHelper());
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_main;
    }
    private DataManager dataManager = null;
    private void logOut() {
        SPUtils.getInstance().put(MySp.ISLOGIN, false);

        dataManager.deleteSportRecord();

        MyApplication.exitActivity();
        ToastUtils.showShort("退出登陆成功!");

        Intent it = new Intent(this, LoginActivity.class);
        startActivity(it);

    }

    @Override
    protected void doAfterInitView() {
        tvEmpty = findViewById(R.id.iv_empty);
        list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));


        list.setAdapter(new HealthAdapter(R.layout.item_health,new ArrayList<>()));

        findViewById(R.id.sport_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SportsActivity.class);
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


        String loginname = SPUtils.getInstance().getString(MySp.PHONE);
        ((TextView) findViewById(R.id.userName)).setText(loginname);

        findViewById(R.id.iv_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });

        sendRequestWithOkHttp();
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


    public void sendRequestWithOkHttp() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String loginname = SPUtils.getInstance().getString(MySp.PHONE);
                            OkHttpClient client = new OkHttpClient();
                            RequestBody requestBody = new FormBody.Builder()
                                    .add("phone",loginname)
                                    .build();
                            Request request = new Request.Builder()
                                    // 指定访问的服务器地址
                                    .url("http://192.168.2.122:8081/recoder/list")
                                    .post(requestBody)
                                    .build();
                            Response response = client.newCall(request).execute();
                            String responseData = response.body().string();
                            System.out.println(responseData);


                            Gson gson = new Gson();
                            NetRecoderBean netBean = gson.fromJson(responseData, NetRecoderBean.class);
                            if (netBean.getCode().equals("0")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {

                                            List<HealthRecord> data = netBean.getData();
                                            if(data.size() > 0){
                                                tvEmpty.setVisibility(View.GONE);
                                            }else {
                                                tvEmpty.setVisibility(View.VISIBLE);
                                            }

                                            ((HealthAdapter)list.getAdapter()).setNewData(data);

                                            //ToastUtils.showShort("查询成功");
                                        }catch (Exception e){

                                        }

                                    }
                                });

                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastUtils.showShort(netBean.getMsg());
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        },1000);

    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            sendRequestWithOkHttp();
        }catch (Exception e){

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
