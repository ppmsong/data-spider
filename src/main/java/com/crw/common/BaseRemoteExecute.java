package com.crw.common;

import com.crw.util.MD5;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.chrome.ChromeDriver;


public class BaseRemoteExecute {


//  protected final Logger log = LoggerFactory.getLogger(getClass().getName());

  protected final Logger log = LogManager.getLogger(getClass().getName());

  protected String getCommDetailID(String phone,String startTime){
    return MD5.encode(phone+startTime);
  }

  protected void putDriver(String sessionid, ChromeDriver driver) {
    CacheManageTool.cacheDriver.put(sessionid, driver);
  }

  protected ChromeDriver getDriver(String sessionid) {
    return CacheManageTool.cacheDriver.getIfPresent(sessionid);
  }

  protected void delDriver(String sessionid){
    CacheManageTool.cacheDriver.invalidate(sessionid);
  }


  protected void putPhone(String sessionid, String phone) {
    CacheManageTool.cachePhone.put(sessionid, phone);
  }

  protected String getPhone(String sessionid) {
    return CacheManageTool.cachePhone.getIfPresent(sessionid);
  }

  protected void putAlipayDriver(String sessionid, ChromeDriver driver) {
    CacheManageTool.cacheAlipayDriver.put(sessionid, driver);
  }

  protected ChromeDriver getAlipayDriver(String sessionid) {
    return CacheManageTool.cacheAlipayDriver.getIfPresent(sessionid);
  }

  protected void delAlipayDriver(String sessionid){
    CacheManageTool.cacheAlipayDriver.invalidate(sessionid);
  }

  protected void putAlipayAccount(String sessionid, String accountName){
    CacheManageTool.cacheAlipayAccount.put(sessionid,accountName);
  }

  protected String getAlipayAccount(String sessionid){
    return CacheManageTool.cacheAlipayAccount.getIfPresent(sessionid);
  }

}
