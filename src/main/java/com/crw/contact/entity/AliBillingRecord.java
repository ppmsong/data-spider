package com.crw.contact.entity;

import java.util.Date;

public class AliBillingRecord {
    private String id;

    private String time;

    private Date createTime;

    private String name;

    private String amount;

    private String tradingStatus;

    private String consumptionName;

    private String cid;

    private String tradingNumber;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time == null ? null : time.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount == null ? null : amount.trim();
    }

    public String getTradingStatus() {
        return tradingStatus;
    }

    public void setTradingStatus(String tradingStatus) {
        this.tradingStatus = tradingStatus == null ? null : tradingStatus.trim();
    }

    public String getConsumptionName() {
        return consumptionName;
    }

    public void setConsumptionName(String consumptionName) {
        this.consumptionName = consumptionName == null ? null : consumptionName.trim();
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid == null ? null : cid.trim();
    }

    public String getTradingNumber() {
        return tradingNumber;
    }

    public void setTradingNumber(String tradingNumber) {
        this.tradingNumber = tradingNumber == null ? null : tradingNumber.trim();
    }
}