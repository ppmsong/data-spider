package com.crw.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class DriverUtils {

  public static boolean isElementExist(WebDriver driver, By locator) {
    try {
      driver.findElement(locator);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static boolean isElementExist(WebElement ele, By locator) {
    try {
      ele.findElement(locator);
      return true;
    } catch (Exception e) {
      return false;
    }
  }



  public static boolean isElementByClassExist(ChromeDriver driver, String using) {
    try {
      driver.findElementByClassName(using);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static boolean isElementByIdExist(ChromeDriver driver, String using) {
    try {
      driver.findElementById(using);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
