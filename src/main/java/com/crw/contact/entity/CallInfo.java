package com.crw.contact.entity;

import com.alibaba.fastjson.JSON;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 通话语音信息
 */
public class CallInfo implements Serializable {
    private String id;

    private String cid;

    private String fee;

    private String location;

    private String locationType;

    private String peerNumber;

    private String time;

    private String duration;

    private String dialType;

    private String remark;

    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid == null ? null : cid.trim();
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee == null ? null : fee.trim();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location == null ? null : location.trim();
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType == null ? null : locationType.trim();
    }

    public String getPeerNumber() {
        return peerNumber;
    }

    public void setPeerNumber(String peerNumber) {
        this.peerNumber = peerNumber == null ? null : peerNumber.trim();
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time == null ? null : time.trim();
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration == null ? null : duration.trim();
    }

    public String getDialType() {
        return dialType;
    }

    public void setDialType(String dialType) {
        this.dialType = dialType == null ? null : dialType.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}