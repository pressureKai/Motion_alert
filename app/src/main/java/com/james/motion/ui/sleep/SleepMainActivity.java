package com.james.motion.ui.sleep;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.gyf.immersionbar.ImmersionBar;
import com.james.motion.R;
import com.james.motion.ui.sleep.Fragments.AlarmFragment;
import com.james.motion.ui.sleep.Fragments.ChartFragment;
import com.james.motion.ui.sleep.Fragments.DiaryFragment;
import com.james.motion.ui.sleep.Fragments.MusicFragment;

import java.util.ArrayList;
import java.util.List;

public class SleepMainActivity extends AppCompatActivity implements BottomNavigationBar.OnTabSelectedListener{

    List<Fragment> list = new ArrayList<>();
    BottomNavigationBar bottomNavigationBar;
    ChartFragment chartFragment;
    AlarmFragment alarmFragment;
    MusicFragment musicFragment;
    DiaryFragment diaryFragment;
    FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_main);


        fm = getFragmentManager();

        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom);
        ImmersionBar.with(this).titleBar(
                bottomNavigationBar
        ).statusBarDarkFont(true, 0.7f).statusBarColor(R.color.white).init();



        /**
         *添加tab标签页
         */
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.icon_color_green, "睡眠")
                        .setInActiveColor(Color.BLACK)
                        .setInactiveIconResource(R.drawable.icon_color_white))
                .addItem(new BottomNavigationItem(R.drawable.icon_color_green, "睡眠报告")
                        .setInActiveColor(Color.BLACK)
                        .setInactiveIconResource(R.drawable.icon_color_white))
                .addItem(new BottomNavigationItem(R.drawable.icon_color_green, "催眠曲")
                        .setInActiveColor(Color.BLACK)
                        .setInactiveIconResource(R.drawable.icon_color_white))
                .initialise();

        onTabSelected(0);
        bottomNavigationBar.setTabSelectedListener(this);
    }

    public void hideFragment(FragmentTransaction transaction) {
        for (Fragment fragment : list) {
            transaction.hide(fragment);
        }
    }

    @Override
    public void onTabSelected(int position) {

        FragmentTransaction transaction = fm.beginTransaction();
        hideFragment(transaction);
        switch (position) {
            case 0:
                if (alarmFragment == null) {
                    alarmFragment = new AlarmFragment();
                    transaction.add(R.id.layFrame, alarmFragment);
                    list.add(alarmFragment);
                } else {
                    transaction.show(alarmFragment);
                }
                DiaryFragment.tag = true;
                break;
            case 1:
                if (chartFragment != null) {
                    chartFragment = null;
                }
                chartFragment = new ChartFragment();
                transaction.add(R.id.layFrame, chartFragment);
                list.add(chartFragment);
                DiaryFragment.tag = true;
                break;
            case 2:
                if (musicFragment == null) {
                    musicFragment = new MusicFragment();
                    transaction.add(R.id.layFrame, musicFragment);
                    list.add(musicFragment);
                } else {
                    transaction.show(musicFragment);
                }
                DiaryFragment.tag = true;
                break;
        }
        transaction.commit();
    }

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int resquestCode, int resultCode, Intent data1) {
        super.onActivityResult(resquestCode, resultCode, data1);
        if (resquestCode == 6 || resquestCode == 9) {
            diaryFragment = null;
            DiaryFragment.tag = false;
            onTabSelected(2);
        }
    }
}
