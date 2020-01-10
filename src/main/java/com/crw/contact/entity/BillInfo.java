package com.crw.contact.entity;

import com.alibaba.fastjson.JSON;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 账单信息
 */
public class BillInfo implements Serializable {
    private String id;

    private String cid;

    private String baseFee;

    private String billMonth;

    private String billStartDate;

    private String billEndDate;

    private String voiceFee;

    private String webFee;

    private String smsFee;

    private String extraFee;

    private String extraServiceFee;

    private String discount;

    private String totalFee;

    private String point;

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

    public String getBaseFee() {
        return baseFee;
    }

    public void setBaseFee(String baseFee) {
        this.baseFee = baseFee == null ? null : baseFee.trim();
    }

    public String getBillMonth() {
        return billMonth;
    }

    public void setBillMonth(String billMonth) {
        this.billMonth = billMonth == null ? null : billMonth.trim();
    }

    public String getBillStartDate() {
        return billStartDate;
    }

    public void setBillStartDate(String billStartDate) {
        this.billStartDate = billStartDate == null ? null : billStartDate.trim();
    }

    public String getBillEndDate() {
        return billEndDate;
    }

    public void setBillEndDate(String billEndDate) {
        this.billEndDate = billEndDate == null ? null : billEndDate.trim();
    }

    public String getVoiceFee() {
        return voiceFee;
    }

    public void setVoiceFee(String voiceFee) {
        this.voiceFee = voiceFee == null ? null : voiceFee.trim();
    }

    public String getWebFee() {
        return webFee;
    }

    public void setWebFee(String webFee) {
        this.webFee = webFee == null ? null : webFee.trim();
    }

    public String getSmsFee() {
        return smsFee;
    }

    public void setSmsFee(String smsFee) {
        this.smsFee = smsFee == null ? null : smsFee.trim();
    }

    public String getExtraFee() {
        return extraFee;
    }

    public void setExtraFee(String extraFee) {
        this.extraFee = extraFee == null ? null : extraFee.trim();
    }

    public String getExtraServiceFee() {
        return extraServiceFee;
    }

    public void setExtraServiceFee(String extraServiceFee) {
        this.extraServiceFee = extraServiceFee == null ? null : extraServiceFee.trim();
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount == null ? null : discount.trim();
    }

    public String getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(String totalFee) {
        this.totalFee = totalFee == null ? null : totalFee.trim();
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point == null ? null : point.trim();
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