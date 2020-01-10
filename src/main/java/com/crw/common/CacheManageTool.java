package com.crw.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.chrome.ChromeDriver;

public class CacheManageTool {

  public static Cache<String, String> cachePhone = CacheBuilder.newBuilder()
      .expireAfterAccess(30, TimeUnit.MINUTES)
      .build();

  public static Cache<String, ChromeDriver> cacheDriver = CacheBuilder.newBuilder()
      .expireAfterAccess(30, TimeUnit.MINUTES)
      .build();


  public static Cache<String, ChromeDriver> cacheAlipayDriver = CacheBuilder.newBuilder()
      .expireAfterAccess(30, TimeUnit.MINUTES)
      .build();

  public static Cache<String, String> cacheAlipayAccount = CacheBuilder.newBuilder()
      .expireAfterAccess(30, TimeUnit.MINUTES)
      .build();

}
