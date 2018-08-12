package com.xds.p.bean;

/**
 * Created by yt on 2018/8/12.
 */

public class XdsBean {

    private int _id;
    private String num;
    private int time;

    private String yangSe;
    private String shengXiao;

    public XdsBean(int _id, String num, int time, String yangSe, String shengXiao) {
        this._id = _id;
        this.num = num;
        this.time = time;
        this.yangSe = yangSe;
        this.shengXiao = shengXiao;
    }

    public int get_id() {
        return _id;
    }

    public String getNum() {
        return num;
    }

    public int getTime() {
        return time;
    }


    public String getYangSe() {
        return yangSe;
    }

    public String getShengXiao() {
        return shengXiao;
    }

    @Override
    public String toString() {
        return "num:" + num + " yangSe:" + yangSe + " shengXiao:" + shengXiao;
    }
}
