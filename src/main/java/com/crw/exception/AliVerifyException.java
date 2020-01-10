package com.crw.exception;

public class AliVerifyException extends RuntimeException {


  private AliVerify aliVerify;

  public AliVerifyException() {
  }

  public AliVerifyException(Integer urlCode, String msg) {
    aliVerify = new AliVerify(urlCode, msg);
  }

  public AliVerifyException(Integer urlCode, String msg, String qrCode) {
    aliVerify = new AliVerify(urlCode, msg, qrCode);
  }


  public AliVerify getAliVerify() {
    return aliVerify;
  }

  public void setAliVerify(AliVerify aliVerify) {
    this.aliVerify = aliVerify;
  }
}

class AliVerify {

  private Integer urlCode;

  private String qrCode;

  private String msg;

  public AliVerify(Integer urlCode, String msg, String qrCode) {
    this.urlCode = urlCode;
    this.msg = msg;
    this.qrCode = qrCode;
  }

  public AliVerify(Integer urlCode, String msg) {
    this.urlCode = urlCode;
    this.msg = msg;
  }

  public Integer getUrlCode() {
    return urlCode;
  }

  public void setUrlCode(Integer urlCode) {
    this.urlCode = urlCode;
  }

  public String getQrCode() {
    return qrCode;
  }

  public void setQrCode(String qrCode) {
    this.qrCode = qrCode;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }
}
