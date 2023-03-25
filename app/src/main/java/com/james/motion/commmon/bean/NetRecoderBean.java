package com.james.motion.commmon.bean;

import java.util.List;

public class NetRecoderBean {


    /**
     * code : 0
     * msg : 查询成功！
     * data : [{"uid":1,"phone":"15816522608","project":"完成心率监测","time":"15816522608"}]
     */

    private String code;
    private String msg;
    private List<HealthRecord> data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<HealthRecord> getData() {
        return data;
    }

    public void setData(List<HealthRecord> data) {
        this.data = data;
    }

}
