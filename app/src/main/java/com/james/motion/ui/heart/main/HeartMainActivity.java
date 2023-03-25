package com.james.motion.ui.heart.main;


import static com.james.motion.commmon.utils.CameraHelper.calculateAvgGrey;

import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.james.motion.R;
import com.james.motion.commmon.bean.NetRecoderBean;
import com.james.motion.commmon.utils.Complex;
import com.james.motion.commmon.utils.FFT;
import com.james.motion.commmon.utils.MySp;
import com.james.motion.ui.heart.BaseActivity;
import com.james.motion.widget.CameraPreviewView;
import com.james.motion.widget.CardiogView;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HeartMainActivity extends BaseActivity {

    private CameraPreviewView mCameraPreviewView;
    private CardiogView mCardiogView;
    private boolean started = false;
    private TextView mTv_heartrate;


    private int heartRate = 0;
    @Override
    public int getStatusBarType() {
        return BaseActivity.TRANSPARENT_STATUS_BAR;
    }

    @Override
    public void initViews() {
        mCameraPreviewView = (CameraPreviewView) findViewById(R.id.camerapreviewview);
        mCameraPreviewView.setPreviewCallback(new MyPreviewCallback());
//        mImageView = (ImageView) findViewById(R.id.iv_imageview);
        mCardiogView = (CardiogView) findViewById(R.id.cardiogview);

        mCameraPreviewView.openCameraFlashMode();
        mTv_heartrate = (TextView) findViewById(R.id.tv_heartrate);
    }

    @Override
    protected void doAfterInitView() {
    }

    public void startMeasure(View view) {
//        mCameraPreviewView.openCameraFlashMode();
        started = !started;
    }



    @Override
    public int getContentViewID() {
        return R.layout.activity_heart_main;
    }

    class MyPreviewCallback implements Camera.PreviewCallback {

        private int[] mRgb;
        private Matrix matrix;
        private int count = 0;
        Complex[] avggreyArray;
        private long mStartTime;
        private long mEndTime;
        private int frameCount;

        public MyPreviewCallback() {
            matrix = new Matrix();
            matrix.postRotate(90);
            frameCount = 128;
            avggreyArray = new Complex[frameCount];
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

            int width = camera.getParameters().getPreviewSize().width;
            int height = camera.getParameters().getPreviewSize().height;

            if (mRgb == null) {
                mRgb = new int[width * height];
            }

            float avggrey = calculateAvgGrey(data, width, height);
//            Bitmap bm = Bitmap.createBitmap(mRgb, width, height, Bitmap.Config.ARGB_8888);
//            mImageView.setImageBitmap(bm);xiamian

            if (started) {
                mCardiogView.putPoint(avggrey);
                //新加，把取到的灰度值散点放到数组中，用于后续的FFT处理
                avggreyArray[count] = new Complex((double)avggrey, 0.0);

                if (count == 0){
                    mStartTime = SystemClock.uptimeMillis();
                }

                count++;

                if (count == frameCount) {
                    mEndTime = SystemClock.uptimeMillis();
                    double samplingRate = 1000.0/((mEndTime-mStartTime)/(double)frameCount);
                    Complex[] rateArray = FFT.fft(avggreyArray);
                    int maxNum = 1;
                    double max = rateArray[1].abs();
                    for (int i = 2; i < rateArray.length/2; i++) {
                        double absnum = rateArray[i].abs();
                        System.out.println();
                        if (absnum > max){
                            max = absnum;
                            maxNum = i;
                        }
                    }
                    int heartrate = (int)(60*(double)(samplingRate*maxNum/frameCount));
                    if (heartrate >= 40 && heartrate <= 200){
                        heartRate = heartrate;
                    }

                    if (heartrate >= 40 && heartrate <= 200)
                        mTv_heartrate.setText("心率:" + heartrate + "次/分");
                    else
                        mTv_heartrate.setText("心率:" + " 请放好手指");
                    count = 0;
                }
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDestroy() {
        if(heartRate != 0){
            sendRequestWithOkHttp();
        }
        openFlashLight();
        closeFlashLight();
        super.onDestroy();
    }

    public void openFlashLight() {
        if (mCameraPreviewView.getmCamera() == null) {
            return;
        }
        Camera.Parameters parameter = mCameraPreviewView.getmCamera().getParameters();
        parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCameraPreviewView.getmCamera().setParameters(parameter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        openFlashLight();
        closeFlashLight();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        openFlashLight();
        closeFlashLight();
    }

    /**
     * 关闭闪光灯
     */
    public void closeFlashLight() {
        if (mCameraPreviewView.getmCamera() == null) {
            return;
        }
        Camera.Parameters parameter = mCameraPreviewView.getmCamera().getParameters();
        parameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCameraPreviewView.getmCamera().setParameters(parameter);
    }

    public void sendRequestWithOkHttp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String loginname = SPUtils.getInstance().getString(MySp.PHONE);
                    OkHttpClient client = new OkHttpClient();

                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("phone", loginname);
                        obj.put("project",loginname + "完成了心率监测，心率为"+heartRate+ "分/每秒");
                        obj.put("time",System.currentTimeMillis()+"");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    MediaType type = MediaType.parse("application/json;charset=utf-8");
                    RequestBody RequestBody2 = RequestBody.create(type, obj.toString());

                    Request request = new Request.Builder()
                            // 指定访问的服务器地址
                            .url("http://192.168.2.122:8081/recoder/insert")
                            .post(RequestBody2)
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
                               // ToastUtils.showShort("健康记录保存成功");
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
}
