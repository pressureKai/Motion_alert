package com.james.motion.ui.heart.main;


import static com.james.motion.commmon.utils.CameraHelper.calculateAvgGrey;

import android.content.Intent;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import com.james.motion.R;
import com.james.motion.commmon.utils.Complex;
import com.james.motion.commmon.utils.FFT;
import com.james.motion.ui.heart.BaseActivity;
import com.james.motion.widget.CameraPreviewView;
import com.james.motion.widget.CardiogView;

public class HeartMainActivity extends BaseActivity {

    private CameraPreviewView mCameraPreviewView;
    private Camera mCamera;
//    private ImageView mImageView;
    private CardiogView mCardiogView;
    private boolean started = false;
    private TextView mTv_heartrate;

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

            if (started == true) {
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
                    if (heartrate >= 40 && heartrate <= 200)
                        mTv_heartrate.setText("心率:" + heartrate + "次/分  (•̀ᴗ•́)و ̑̑ ");
                    else
                        mTv_heartrate.setText("心率:" + " 手指要放好啊 ( ＿ ＿)ノ｜扶墙");
//                    System.out.println(buffer);
//                    System.out.println("SamplingRate:" + samplingRate);
                    count = 0;
                }
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
