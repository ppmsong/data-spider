package com.crw.crawler;

import com.alibaba.fastjson.JSON;
import com.crw.common.BaseRemoteExecute;
import com.crw.common.Constants;
import com.crw.common.Result;
import com.crw.config.SystemConfig;
import com.crw.contact.dao.BillInfoDao;
import com.crw.contact.dao.CustomerInfoDao;
import com.crw.contact.dao.PackageInfoDao;
import com.crw.contact.entity.BillInfo;
import com.crw.contact.entity.CustomerInfo;
import com.crw.contact.entity.PackageInfo;
import com.crw.util.DriverUtils;
import com.crw.util.JodatimeUtils;
import com.crw.util.MD5;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChinaTelecomExecute extends BaseRemoteExecute {

  @Autowired
  private CustomerInfoDao customerInfoDao;

  @Autowired
  private PackageInfoDao packageInfoDao;

  @Autowired
  private BillInfoDao billInfoDao;

  private final static String URL_LOGIN_PAGE = "https://login.189.cn/web/login";

  private final static String URL_USER_INFO_PAGE = "http://www.189.cn/dqmh/my189/initMy189home.do";

  public Result getLoginCode(String sessionId, String phone, String pwd) {
    ChromeDriver driver = getDriver(sessionId);
    try {
      if (driver != null) {
        if (URL_LOGIN_PAGE.equals(driver.getCurrentUrl())) {

        }
      } else {
        ChromeOptions option = new ChromeOptions();
        option.setHeadless(true);
        option.addArguments("--no-sandbox"); // 禁止沙箱模式
        if (StringUtils.isNotEmpty(SystemConfig.proxyIpAndPort)) {
          Proxy proxy = new Proxy();
          proxy.setHttpProxy(SystemConfig.proxyIpAndPort);
//              .setSslProxy(SystemConfig.proxyIpAndPort);
          option.setCapability(CapabilityType.PROXY, proxy);
        }
        driver = new ChromeDriver(option);
      }
      driver.navigate().to(URL_LOGIN_PAGE);
      WebDriverWait wait = new WebDriverWait(driver, 10);

      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.className("QRCodeBoxSuperscrip"))).click();
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("txtAccount"))).clear();
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("txtAccount"))).sendKeys(phone);
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("txtShowPwd"))).click();
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("txtPassword"))).clear();
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("txtPassword"))).sendKeys(pwd);
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("imgCaptcha")));
      String codePath = getQRCode(driver, driver.findElementById("imgCaptcha"));

      putDriver(sessionId, driver);
      putPhone(sessionId, phone);
      return new Result(Constants.SUCCESS, "图形验证码访问路径：" + SystemConfig.localAddress + codePath);
    } catch (Exception e) {
      driver.quit();
      delDriver(sessionId);
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, "没获取到登陆验证码");
    }
  }


  public Result login(String sessionId, String code) {
    ChromeDriver driver = getDriver(sessionId);
    WebDriverWait wait = new WebDriverWait(driver, 10);
    try {
      if (driver == null || !URL_LOGIN_PAGE.equals(driver.getCurrentUrl())) {
        return new Result(Constants.SYSTEMERROR, "验证码失效请重新获取验证码");
      }
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("txtCaptcha"))).clear();
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("txtCaptcha"))).sendKeys(code);
      driver.findElementById("loginbtn").click();
      Thread.sleep(1000);
      if (URL_LOGIN_PAGE.equals(driver.getCurrentUrl())) {
        return new Result(Constants.SYSTEMERROR, "验证码或密码错误");
      }

      return new Result(Constants.SUCCESS, Constants.getMessage(Constants.SUCCESS));
    } catch (Exception e) {
      driver.quit();
      delDriver(sessionId);
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, "登陆失败");
    }
  }


  public Result crawlerData(String sessionId, Integer urlCode) {
    ChromeDriver driver = getDriver(sessionId);
    try {

    } catch (Exception e) {
      driver.quit();
      delDriver(sessionId);
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, "登陆失败");
    }

    return null;
  }

  // 个人信息采集
  public Result custinfoQry(String sessionId) {
    String nm = getPhone(sessionId);//获取手机号
    try {
      log.warn("开始采集个人信息：" + nm);
      ChromeDriver driver = getDriver(sessionId);
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
      driver.get("http://www.189.cn/dqmh/my189/initMy189home.do?fastcode=00710597");
      Thread.sleep(1000);
      driver.switchTo().frame(driver.findElement(By.id("bodyIframe")));
      String iframe = driver.getPageSource();
      Document docIframe = Jsoup.parse(iframe);
      String name = docIframe.selectFirst("#custNameH").text();
      Elements elesTd = docIframe.select(".table-form-39.mgt-15 tbody tr").get(1).select("td");
      String package_name = elesTd.get(1).text();
      String available_balance = elesTd.get(2).selectFirst("#realtimeFee").text();
      CustomerInfo customerInfo = new CustomerInfo();
      customerInfo.setId(MD5.encode(nm));
      customerInfo.setMobile(nm);
      customerInfo.setName(name);
      customerInfo.setPackageName(package_name);
      customerInfo.setAvailableBalance(available_balance);
      customerInfo.setCreateTime(new Date());
      customerInfoDao.insert(customerInfo);
      return new Result(Constants.SUCCESS, JSON.toJSON(customerInfo));
    } catch (Exception e) {
      e.printStackTrace();
      log.warn("采集个人信息失败 {}, {}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }

  // 套餐信息
  public Result packageQry(String sessionId) {
    String nm = getPhone(sessionId);//获取手机号
    try {
      log.warn("开始采集套餐信息：" + nm);
      ChromeDriver driver = getDriver(sessionId);
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
      driver.get("http://www.189.cn/dqmh/my189/initMy189home.do?fastcode=00710600");
      Thread.sleep(1000);
      driver.switchTo().frame(driver.findElement(By.id("bodyIframe")));
      String iframe = driver.getPageSource();
      Document docIframe = Jsoup.parse(iframe);

      Elements elesTd = docIframe.select("#cumulationInfoTable tbody tr").get(1).select("td");
      String item = elesTd.get(0).text();
      String total = elesTd.get(1).text();
      String used = elesTd.get(3).text();
      String unit = elesTd.get(4).text();

      PackageInfo packageInfo = new PackageInfo();
      packageInfo.setId(MD5.encode(nm + item));
      packageInfo.setCid(MD5.encode(nm));
      packageInfo.setItem(item);
      packageInfo.setTotal(total);
      packageInfo.setUsed(used);
      packageInfo.setUnit(unit);
      packageInfo.setCreateTime(new Date());
      try {
        packageInfoDao.insert(packageInfo);
      } catch (Exception ex) {
      }
      return new Result(Constants.SUCCESS, JSON.toJSON(packageInfo));
    } catch (Exception e) {
      e.printStackTrace();
      log.warn("采集套餐信息数据发生异常 {},{}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }

  // 账单信息
  public Result billinfoQry(String sessionId) {
    String nm = getPhone(sessionId);//获取手机号
    try {
      log.warn("开始采集账单信息：" + nm);
      ChromeDriver driver = getDriver(sessionId);
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
      WebDriverWait wait = new WebDriverWait(driver, 10);
      driver.get("http://www.189.cn/dqmh/my189/initMy189home.do?fastcode=00710601");
      Thread.sleep(1000);
      driver.switchTo().frame(driver.findElement(By.id("bodyIframe")));
      Select sel = new Select(driver.findElement(By.id("billingCycle")));
      List<WebElement> options = sel.getOptions();
      List<BillInfo> list = new ArrayList<>();
      for (WebElement option : options) {
        System.out.println(option.getText() + ", " + option.getAttribute("value"));

        sel.selectByVisibleText(option.getText());
        wait.until(ExpectedConditions
            .presenceOfElementLocated(By.xpath("//span[text()='查询']"))).click();
        Thread.sleep(1000 * 5);
        String iframe = driver.getPageSource();
        Document docIframe = Jsoup.parse(iframe);
        // 月份
        String bill_month = DateTimeFormat.forPattern(JodatimeUtils.FORMAT_YYYYMM_02)
            .parseDateTime(option.getAttribute("value")).toString(JodatimeUtils.FORMAT_YYYYMM_01);
        String base_fee = "";
        String web_fee = "";
        String total_fee = "";
        Elements elesTr = docIframe.select("#showBillResult tbody tr");
        if (elesTr.size() == 1) {
          if (elesTr.get(0).text().contains("对不起，未查到您的账单数据")) {
            continue;
          }
        }
        for (int i = 0; i < elesTr.size(); i++) {
          if (i > 0) {
            Elements elesTd = elesTr.get(i).select("td");
            if (elesTd.size() > 0) {
              if (elesTd.get(0).text().contains("合计")) {
                total_fee = elesTd.get(1).text();
                continue;
              }
              if (elesTd.get(2).text().contains("月基本费")) {
                base_fee = elesTd.get(3).text();
                continue;
              }
              if (elesTd.get(2).text().contains("上网及数据通信费")) {
                web_fee = elesTd.get(3).text();
                continue;
              }
            }
          }
        }
        BillInfo billInfo = new BillInfo();
        billInfo.setId(MD5.encode(nm + bill_month));
        billInfo.setCid(MD5.encode(nm));
        billInfo.setBaseFee(base_fee);
        billInfo.setWebFee(web_fee);
        billInfo.setTotalFee(total_fee);
        billInfo.setCreateTime(new Date());
        try {
          billInfoDao.insert(billInfo);
        } catch (Exception ex) {
        }
        list.add(billInfo);
      }
      return new Result(Constants.SUCCESS, JSON.toJSON(list));
    } catch (Exception e) {
      e.printStackTrace();
      log.warn("采集账单信息失败 {}, {}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }

  // 详单信息
  public Result callinfoQry(String sessionId) {
    String nm = getPhone(sessionId);//获取手机号
    try {
      log.warn("开始采集详单信息：" + nm);
      ChromeDriver driver = getDriver(sessionId);
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
      WebDriverWait wait = new WebDriverWait(driver, 10);
      driver.get("http://www.189.cn/dqmh/my189/initMy189home.do?fastcode=00710602");
      Thread.sleep(1000);
      driver.switchTo().frame(driver.findElement(By.id("bodyIframe")));

      if (DriverUtils.isElementExist(driver, By.xpath("//span[contains(text(),'验证身份继续访问')]"))) {
        return getVCardidName(sessionId);
      }

      String iframe = driver.getPageSource();
      Document docIframe = Jsoup.parse(iframe);
      docIframe.selectFirst(".main-c3 table tbody ");

      return new Result(Constants.SUCCESS, JSON.toJSON(""));
    } catch (Exception e) {
      e.printStackTrace();
      log.warn("采集详单信息失败 {}, {}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }

  /**
   * 弹出身份验证
   */
  public Result getVCardidName(String sessionId) {
    try {
      ChromeDriver driver = getDriver(sessionId);
      // 获取验证码图片
      String imgUrl = getQRCode(driver,
          driver.findElement(By.id("vImgCode2")));
      return new Result(Constants.SUCCESS,
          "需要身份验证,证件号码和姓名; 图片验证码URL: " + SystemConfig.localAddress + imgUrl);
    } catch (Exception e) {
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }

  // 发送身份验证信息
  public Result sendVCardidName(String sessionid, String cardid, String name, String code) {
    String nm = getPhone(sessionid);
    try {
      ChromeDriver driver = getDriver(sessionid);
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
      WebDriverWait wait = new WebDriverWait(driver, 10);
      String html = driver.getPageSource();
      Document doc = Jsoup.parse(html);
      Elements elesTr = doc.select(".main-c3 table tbody tr");
      if (elesTr.size() > 0) {
        if (elesTr.get(1).selectFirst("th").text().contains("证件号码")) {
          wait.until(ExpectedConditions
              .presenceOfElementLocated(By.id("certCode"))).clear();
          wait.until(ExpectedConditions
              .presenceOfElementLocated(By.id("certCode"))).sendKeys(cardid);

          wait.until(ExpectedConditions
              .presenceOfElementLocated(By.id("cust_name"))).clear();
          wait.until(ExpectedConditions
              .presenceOfElementLocated(By.id("cust_name"))).sendKeys(name);

          wait.until(ExpectedConditions
              .presenceOfElementLocated(By.id("vCode2"))).clear();
          wait.until(ExpectedConditions
              .presenceOfElementLocated(By.id("vCode2"))).sendKeys(code);

          wait.until(ExpectedConditions
              .presenceOfElementLocated(By.xpath("//span[text()='验证身份继续访问']"))).click();
          Thread.sleep(1000 * 5);

          html = driver.getPageSource();
          doc = Jsoup.parse(html);
          if (doc.selectFirst("#vImgCode") != null) {
            String imgUrl = getQRCode(driver, driver.findElement(By.id("vImgCode")));
            return new Result(Constants.SUCCESS,
                "需要再次验证，请用本机发送CXXD至10001获取查询详单的验证码（发送免费）, 图片验证码为：" + imgUrl);
          }
        }
      }
      return new Result(Constants.SUCCESS, Constants.getMessage(Constants.SUCCESS));
    } catch (Exception e) {
      e.printStackTrace();
      log.warn("发送身份验证信息 {}, {}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }

  // 发送短信验证信息
  public Result sendVSmsCode(String sessionid, String imgCode, String smsCode) {
    String nm = getPhone(sessionid);
    try {
      ChromeDriver driver = getDriver(sessionid);
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
      WebDriverWait wait = new WebDriverWait(driver, 10);
      String html = driver.getPageSource();
      Document doc = Jsoup.parse(html);
      if (doc.select("#vCode") != null) {
        wait.until(ExpectedConditions
            .presenceOfElementLocated(By.id("vCode"))).clear();
        wait.until(ExpectedConditions
            .presenceOfElementLocated(By.id("vCode"))).sendKeys(imgCode);

        wait.until(ExpectedConditions
            .presenceOfElementLocated(By.id("sRandomCode"))).clear();
        wait.until(ExpectedConditions
            .presenceOfElementLocated(By.id("sRandomCode"))).sendKeys(smsCode);

        wait.until(ExpectedConditions
            .presenceOfElementLocated(By.xpath("//span[text()='确定']"))).click();
      }
      return new Result(Constants.SUCCESS, Constants.getMessage(Constants.SUCCESS));
    } catch (Exception e) {
      e.printStackTrace();
      log.warn("发送短信验证信息 {}, {}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }

  private String getQRCode(ChromeDriver driver, WebElement ele) {
    File file = ele.getScreenshotAs(OutputType.FILE);
    try {
      String fileName =
          MD5.encodeLowerCase(driver.getSessionId().toString()) + ".png";
      String folderPath = SystemConfig.imagesPathMobile;
      File folder = new File(folderPath);
      if (!folder.exists()) {
        folder.mkdirs();
      }
      String absolute = folderPath + fileName;
      log.info("absolute={}", absolute);
      File screenshotLocation = new File(absolute);
      FileUtils.copyFile(file, screenshotLocation);

      return absolute.split("static")[1];
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }


  private String getQRCode2(ChromeDriver driver, WebElement ele) throws Exception {
    File file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
    BufferedImage fullImg = ImageIO.read(file);

    Point point = ele.getLocation();
    // Get width and height of the element
    int eleWidth = ele.getSize().getWidth();
    int eleHeight = ele.getSize().getHeight();

    BufferedImage eleScreenshot = fullImg.getSubimage(point.getX(), point.getY(),
        eleWidth, eleHeight);
    ImageIO.write(eleScreenshot, "png", file);
    String fileName = MD5.encodeLowerCase(driver.getSessionId().toString()) + ".png";

    String folderPath = SystemConfig.imagesPathMobile;
    File folder = new File(folderPath);
    if (!folder.exists()) {
      folder.mkdirs();
    }

    String absolute = folderPath + fileName;
    log.info("absolute={}", absolute);
    File screenshotLocation = new File(absolute);
    FileUtils.copyFile(file, screenshotLocation);
    return SystemConfig.localAddress + absolute.split("static")[1];
  }


}
