package com.crw.contact.entity;

import java.util.Date;

public class AliTotalssets {
    private String id;

    private Date createTime;

    private String balance;

    private String yuebaoAmt;

    private String yuebaoTotalProfit;

    private String huabeiLimit;

    private String huabeiCanLimit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance == null ? null : balance.trim();
    }

    public String getYuebaoAmt() {
        return yuebaoAmt;
    }

    public void setYuebaoAmt(String yuebaoAmt) {
        this.yuebaoAmt = yuebaoAmt == null ? null : yuebaoAmt.trim();
    }

    public String getYuebaoTotalProfit() {
        return yuebaoTotalProfit;
    }

    public void setYuebaoTotalProfit(String yuebaoTotalProfit) {
        this.yuebaoTotalProfit = yuebaoTotalProfit == null ? null : yuebaoTotalProfit.trim();
    }

    public String getHuabeiLimit() {
        return huabeiLimit;
    }

    public void setHuabeiLimit(String huabeiLimit) {
        this.huabeiLimit = huabeiLimit == null ? null : huabeiLimit.trim();
    }

    public String getHuabeiCanLimit() {
        return huabeiCanLimit;
    }

    public void setHuabeiCanLimit(String huabeiCanLimit) {
        this.huabeiCanLimit = huabeiCanLimit == null ? null : huabeiCanLimit.trim();
    }
}