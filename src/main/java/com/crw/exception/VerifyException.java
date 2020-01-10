package com.crw.exception;

public class VerifyException extends RuntimeException {

  private String url;

  public VerifyException(){
  }

  public VerifyException(String url){
    this.url=url;
  }


  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
