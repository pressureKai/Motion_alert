package com.james.motion.ui.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.james.motion.R;
import com.james.motion.commmon.bean.HealthRecord;
import com.james.motion.commmon.bean.PathRecord;
import com.james.motion.sport_motion.MotionUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class HealthAdapter extends BaseQuickAdapter<HealthRecord, BaseViewHolder> {

    private DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private DecimalFormat intFormat = new DecimalFormat("#");

    public HealthAdapter(int layoutResId, @Nullable List<HealthRecord> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, HealthRecord item) {
        helper.setText(R.id.tv_content, item.getProject());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String format = simpleDateFormat.format(new Date(Long.parseLong(item.getTime())));
        helper.setText(R.id.tv_time, format);
    }
}
