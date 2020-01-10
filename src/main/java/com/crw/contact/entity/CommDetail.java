package com.crw.contact.entity;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * 通话详单
 */
public class CommDetail implements Serializable {
    private String id;

    /**
     * 本机号码
     */
    private String nm;

    /**
     * 通信方式,主叫/被叫
     */
    private String commmode;

    /**
     * 通信地点
     */
    private String commplac;

    /**
     * 通信类型,国内通话(联通)/本地主叫本地(移动)
     */
    private String commtype;

    /**
     * 通话时长
     */
    private String commtime;

    /**
     * 起始时间
     */
    private String starttime;

    /**
     * 被叫号码
     */
    private String anothernm;

    /**
     * 套餐优惠
     */
    private String mealfavorable;

    /**
     * 通信费用
     */
    private String commfee;

    /**
     * 备注
     */
    private String remark;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm == null ? null : nm.trim();
    }

    public String getCommmode() {
        return commmode;
    }

    public void setCommmode(String commmode) {
        this.commmode = commmode == null ? null : commmode.trim();
    }

    public String getCommplac() {
        return commplac;
    }

    public void setCommplac(String commplac) {
        this.commplac = commplac == null ? null : commplac.trim();
    }

    public String getCommtype() {
        return commtype;
    }

    public void setCommtype(String commtype) {
        this.commtype = commtype == null ? null : commtype.trim();
    }

    public String getCommtime() {
        return commtime;
    }

    public void setCommtime(String commtime) {
        this.commtime = commtime == null ? null : commtime.trim();
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime == null ? null : starttime.trim();
    }

    public String getAnothernm() {
        return anothernm;
    }

    public void setAnothernm(String anothernm) {
        this.anothernm = anothernm == null ? null : anothernm.trim();
    }

    public String getMealfavorable() {
        return mealfavorable;
    }

    public void setMealfavorable(String mealfavorable) {
        this.mealfavorable = mealfavorable == null ? null : mealfavorable.trim();
    }

    public String getCommfee() {
        return commfee;
    }

    public void setCommfee(String commfee) {
        this.commfee = commfee == null ? null : commfee.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}