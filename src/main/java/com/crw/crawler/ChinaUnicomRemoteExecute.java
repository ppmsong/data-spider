package com.crw.crawler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.crw.common.BaseRemoteExecute;
import com.crw.common.Constants;
import com.crw.common.Result;
import com.crw.config.SystemConfig;
import com.crw.contact.dao.BillInfoDao;
import com.crw.contact.dao.CallInfoDao;
import com.crw.contact.dao.CustomerInfoDao;
import com.crw.contact.dao.NetInfoDao;
import com.crw.contact.dao.PackageInfoDao;
import com.crw.contact.dao.RechargeInfoDao;
import com.crw.contact.dao.SmsInfoDao;
import com.crw.contact.entity.BillInfo;
import com.crw.contact.entity.CallInfo;
import com.crw.contact.entity.CommDetail;
import com.crw.contact.entity.CustomerInfo;
import com.crw.contact.entity.NetInfo;
import com.crw.contact.entity.PackageInfo;
import com.crw.contact.entity.RechargeInfo;
import com.crw.contact.entity.SmsInfo;
import com.crw.exception.AliVerifyException;
import com.crw.exception.VerifyException;
import com.crw.util.DriverUtils;
import com.crw.util.JodatimeUtils;
import com.crw.util.MD5;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ChinaUnicomRemoteExecute extends BaseRemoteExecute {

  private static final String URL_LOGIN_PAGE = "https://uac.10010.com/portal/mallLogin.jsp";

  private static final String URL_CUSTOMER_INFO_PAGE_ = "https://iservice.10010.com/e4/query/basic/personal_xx_iframe.html";
  private static final String URL_CALL_DETAIL_PAGE = "https://iservice.10010.com/e4/query/bill/call_dan-iframe.html";
  private static final String URL_CALL_DETAIL2_PAGE = "https://iservice.10010.com/e4/query/call_detail_record.html?menuId=000100030001&beginDate=BEGINDATE&endDate=ENDDATE";
  private static final String URL_SMS_PAGE = "https://iservice.10010.com/e4/query/calls/call_sms-iframe.html";
  private static final String URL_NET_PAGE = "https://iservice.10010.com/e4/query/basic/call_flow_iframe1.html";


  @Autowired
  private CustomerInfoDao customerInfoDao;

  @Autowired
  private RechargeInfoDao rechargeInfoDao;

  @Autowired
  private PackageInfoDao packageInfoDao;

  @Autowired
  private SmsInfoDao smsInfoDao;

  @Autowired
  private CallInfoDao callInfoDao;

  @Autowired
  private NetInfoDao netInfoDao;

  @Autowired
  private BillInfoDao billInfoDao;


  public Result sendCode2(String sessionid, String phone) {
    ChromeDriver driver = getDriver(sessionid);
    try {
      ChromeOptions option = new ChromeOptions();
      if (driver != null) {
        // 用户之前打开过浏览器
        if (URL_LOGIN_PAGE == driver.getCurrentUrl()) {
          // 如果是登陆页面则点击发送验证码
          driver.findElement(By.id("randomCode")).click();
          return new Result(Constants.SUCCESS, phone);
        }
      } else {
        driver = new ChromeDriver(option);
      }
      driver.get(URL_LOGIN_PAGE);
      driver.switchTo().frame(driver.findElement(By.className("login_iframe")));
      driver.findElement(By.id("randomPwdTips")).click();
      driver.findElement(By.id("userName")).sendKeys(phone);
      driver.findElement(By.id("randomCode")).click();
      putDriver(sessionid, driver);
      putPhone(sessionid, phone);
    } catch (Exception e) {
      driver.quit();
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
    return new Result(Constants.SUCCESS, phone);
  }

  public Result sendCode(String sessionid, String phone) {
    ChromeDriver driver = getDriver(sessionid);
    try {

      ChromeOptions option = new ChromeOptions();
      option.setHeadless(true);
      option.addArguments("--no-sandbox"); // 禁止沙箱模式
      if (StringUtils.isNotEmpty(SystemConfig.proxyIpAndPort)) {
        Proxy proxy = new Proxy();
        proxy.setHttpProxy(SystemConfig.proxyIpAndPort);
        option.setCapability(CapabilityType.PROXY, proxy);
      }
      if (driver != null) {
        // 用户之前打开过浏览器
        if (URL_LOGIN_PAGE == driver.getCurrentUrl()) {
          // 如果是登陆页面则点击发送验证码
          driver.findElement(By.id("randomCode")).click();
          return new Result(Constants.SUCCESS, phone);
        }
      } else {
        driver = new ChromeDriver(option);
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
      }
      driver.get(URL_LOGIN_PAGE);
      WebDriverWait wait = new WebDriverWait(driver, 30);
      driver.switchTo().frame(driver.findElement(By.className("login_iframe")));
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("userName"))).clear();
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("userName"))).sendKeys(phone);
      Thread.sleep(1000);
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("randomCKCode"))).click();
      Thread.sleep(1000);
      putDriver(sessionid, driver);
      putPhone(sessionid, phone);

      if (DriverUtils.isElementExist(driver, By.id("error"))) {
        String error = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("error"))).getText();
        if (StringUtils.isEmpty(error)) {
          error = phone + ": 短息发送成功";
          return new Result(Constants.SUCCESS, error);
        }
        return new Result(Constants.SYSTEMERROR, error);
      }
    } catch (Exception e) {
      driver.quit();
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
    return new Result(Constants.SUCCESS, phone + ": 短息发送成功");
  }


  public Result login(String sessionid, String code) {
    ChromeDriver driver = getDriver(sessionid);
    if (driver == null) {
      return new Result(Constants.SYSTEMERROR, "验证码过期请重新发送验证码");
    }
    StringBuilder cookieSb = new StringBuilder();
    try {
      driver.findElement(By.id("userPwd")).sendKeys(code);
      driver.findElement(By.id("login1")).click();
      Thread.sleep(1000 * 5);
      driver.navigate().to(URL_CALL_DETAIL_PAGE);
      WebElement ele = driver.findElementById("input_num");
      if (ele == null) {
        // 如果没有弹出身份验证则继续采集数据
        crawlerCallDetailContent(getPhone(sessionid), driver);
      }
      Set<Cookie> set = driver.manage().getCookies();
      for (Cookie c : set) {
        cookieSb.append(c.getName() + "=" + c.getValue()).append("; ");
      }
      log.info("Cookie: {}", cookieSb.toString());
    } catch (Exception e) {
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
    return new Result(Constants.SUCCESS, "登陆成功");
  }

  private void crawlerCustomerInfo(ChromeDriver driver, CustomerInfo customerInfo)
      throws Exception {
    log.info("{}采集{}信息开始--->", "联通", "基本");
    driver.navigate().to(URL_CUSTOMER_INFO_PAGE_);
    Thread.sleep(1000 * 10);
    driver.switchTo().frame(2);
    String html = driver.getPageSource();
    Document doc = Jsoup.parse(html);
    Elements trEles = doc.select(".ly_gr_l table tr");
    customerInfo.setMobile(trEles.get(0).selectFirst("td").text());
    customerInfo.setId(MD5.encode(customerInfo.getMobile()));
    customerInfo.setLevel(trEles.get(1).selectFirst("td").text());

    Elements dlEles = doc.select("#userInfocontext .data_basic_c.ly_gr_zl dl");
    customerInfo.setName(dlEles.get(0).selectFirst("dd").text());

    customerInfo.setIdcard(dlEles.get(2).selectFirst("dd").text());

    dlEles = doc.select(".ly_gr_l2 dl");
    customerInfo.setPackageName(dlEles.get(0).selectFirst("dd").text());

    customerInfo.setOpenTime(doc.selectFirst(".data_basic_c2_r dl dd").text());

    driver.navigate().to("https://iservice.10010.com/e4/skip.html?menuCode=000100010002");
    Thread.sleep(1000 * 4);
    html = driver.getPageSource();
    String balance = Jsoup.parse(html).selectFirst("#h_2").text();
    customerInfo.setAvailableBalance(balance);
    customerInfo.setIsCert("1");
    customerInfo.setState("1");
    customerInfo.setCreateTime(new Date());
    customerInfoDao.insert(customerInfo);
    log.info("{}采集{}信息结束<---", "联通", "基本");
  }

  private void crawlerRechargeInfo(ChromeDriver driver, String phone) throws Exception {
    log.info("{}采集{}信息开始--->", "联通", "缴费");
    WebDriverWait wait = new WebDriverWait(driver, 30);
    driver.navigate().to("https://iservice.10010.com/e4/query/calls/paid_record-iframe.html");
    Thread.sleep(1000 * 4);
    // driver.switchTo().frame(2);
    String nowMonth = JodatimeUtils.now(JodatimeUtils.FORMAT_YYYYMM_02);
    for (int i = 0; i < 6; i++) {
      driver.switchTo().defaultContent();
      for (int j = 0; j < 10; j++) {
        if (driver.findElement(By.id("center_loadingBg")).getAttribute("style").contains("block")) {
          Thread.sleep(1000 * 4);
        } else {
          break;
        }
      }
      driver.switchTo().frame(2);
      if (i == 0) {

      } else {
        String queryMonth = DateTimeFormat.forPattern(JodatimeUtils.FORMAT_YYYYMM_02)
            .parseDateTime(nowMonth)
            .minusMonths(i).toString(JodatimeUtils.FORMAT_YYYYMM_02);

        if (!DriverUtils.isElementExist(driver, By.xpath("//li[@value=\"" + queryMonth + "\"]"))) {
          continue;
        }
        try {
          driver.switchTo().defaultContent();
          driver.executeScript("window.scrollTo(0,0)");
          Thread.sleep(1000);
          driver.switchTo().frame(2);
          wait.until(ExpectedConditions
              .presenceOfElementLocated(By.xpath("//li[@value=\"" + queryMonth + "\"]"))).click();
        } catch (Exception e) {
          driver.navigate().to("https://iservice.10010.com/e4/query/calls/paid_record-iframe.html");
          Thread.sleep(1000 * 4);
          driver.switchTo().frame(2);
          Thread.sleep(1000);
          continue;
        }
        Thread.sleep(1000 * 4);
      }
      String html = driver.getPageSource();
      Document doc = Jsoup.parse(html);
      if (doc.selectFirst("#paymentRecordContent") == null) {
        log.warn("采集缴费信息网络异常");
        return;
      }
      if (doc.selectFirst("#paymentRecordContent").attr("style").contains("none")) {
        continue;
      }
      Elements trEles = doc.select("#paymentRecordContent table tr");
      for (int j = 0; j < trEles.size(); j++) {
        if (j > 0) {
          RechargeInfo rechargeInfo = new RechargeInfo();
          rechargeInfo.setCid(MD5.encode(phone));
          rechargeInfo.setRechargeTime(trEles.get(j).select("th").get(0).text());
          rechargeInfo.setType(trEles.get(j).select("th").get(1).text());
          rechargeInfo.setAmount(trEles.get(j).select("th").get(3).text());
          rechargeInfo.setCreateTime(new Date());
          rechargeInfo.setId(MD5.encode(phone + rechargeInfo.getRechargeTime()));
          try {
            rechargeInfoDao.insert(rechargeInfo);
          } catch (Exception e) {
            continue;
          }
        }
      }
    }
    log.info("{}采集{}信息结束<---", "联通", "缴费");
  }

  private String crawlerSmsInfo(ChromeDriver driver, String phone, String code, String url)
      throws Exception {
    log.info("{}采集{}信息开始--->", "联通", "短信");
    WebDriverWait wait = new WebDriverWait(driver, 30);

    if (url == null) {
      driver.navigate().to(URL_SMS_PAGE);
      Thread.sleep(1000 * 4);
      driver.switchTo().frame(2);
      if (DriverUtils.isElementExist(driver, By.id("huoqu_buttons"))) {
        driver.findElementById("huoqu_buttons").click();
        throw new VerifyException("1");
      }
    }

    if (!url.equals("1")) {
      return url;
    }
    wait.until(ExpectedConditions
        .presenceOfElementLocated(By.id("input"))).clear();
    wait.until(ExpectedConditions
        .presenceOfElementLocated(By.id("input"))).sendKeys(code);
    wait.until(ExpectedConditions
        .presenceOfElementLocated(By.id("sign_in"))).click();

    Thread.sleep(1000 * 4);

    String cid = MD5.encode(phone);
    String nowMonth = JodatimeUtils.now(JodatimeUtils.FORMAT_YYYYMM_02);
    for (int i = 0; i < 6; i++) {
      if (i == 0) {

      } else {
        String queryMonth = DateTimeFormat.forPattern(JodatimeUtils.FORMAT_YYYYMM_02)
            .parseDateTime(nowMonth)
            .minusMonths(i).toString(JodatimeUtils.FORMAT_YYYYMM_02);

        if (!DriverUtils.isElementExist(driver, By.xpath("//li[@value='" + queryMonth + "']"))) {
          continue;
        }
        driver.switchTo().defaultContent();
        driver.executeScript("window.scrollTo(0,0)");
        Thread.sleep(1000);
        driver.switchTo().frame(2);
        wait.until(ExpectedConditions
            .presenceOfElementLocated(By.xpath("//li[@value='" + queryMonth + "']"))).click();
        Thread.sleep(1000 * 5);
      }

      while (true) {
        String html = driver.getPageSource();
        Document doc = Jsoup.parse(html);
        if (doc.selectFirst("#smsmmsResultTab").attr("style").contains("none")) {
          break;
        }
        Elements eleTr = doc.select("#smsmmsResultTab table tbody tr");
        for (int j = 0; j < eleTr.size(); j++) {
          Elements elesTh = eleTr.get(j).select("th");
          SmsInfo smsInfo = new SmsInfo();
          smsInfo.setCid(cid);
          smsInfo.setTime(elesTh.get(0).text());
          smsInfo.setMsgType(elesTh.get(1).text().contains("短信") ? "SMS" : "MMS");
          smsInfo.setSendType(elesTh.get(2).text().equals("发送") ? "SEND" : "RECEIVE");
          smsInfo.setPeerNumber(elesTh.get(3).text());
          smsInfo.setFee(elesTh.get(4).text());
          smsInfo.setCreateTime(new Date());
          smsInfo.setId(MD5.encode(phone + smsInfo.getTime()));
          try {
            smsInfoDao.insert(smsInfo);
          } catch (Exception e) {
            continue;
          }
        }
        if (!DriverUtils.isElementExist(driver,
            By.xpath("//div[@class='score_page_r' and contains(text(),'尾页')]"))) {
          break;
        }
        try {
          driver.switchTo().defaultContent();
          for (int j = 0; j < 10; j++) {
            if (driver.findElement(By.id("center_loadingBg")).getAttribute("style")
                .contains("block")) {
              Thread.sleep(1000 * 4);
            } else {
              break;
            }
          }
          driver.switchTo().frame(2);
          driver.findElement(By.xpath("//div[@class='score_page_r' and contains(text(),'下一页')]"))
              .click();
        } catch (Exception e) {
          break;
        }
        Thread.sleep(1000 * 5);
      }
    }
    log.info("{}采集{}信息结束<---", "联通", "短信");
    return null;
  }

  private String crawlerNetInfo(ChromeDriver driver, String phone, String code, String url)
      throws Exception {
    log.info("{}采集{}信息开始--->", "联通", "上网流量");
    WebDriverWait wait = new WebDriverWait(driver, 30);
    if (url == null || !URL_NET_PAGE.equals(driver.getCurrentUrl())) {
      for (int i = 0; i < 3; i++) {
        driver.navigate().to(URL_NET_PAGE);
        Thread.sleep(1000 * 5);
        if (DriverUtils.isElementExist(driver, By.id("huoqu_buttons"))) {
          try {
            wait.until(ExpectedConditions
                .presenceOfElementLocated(By.id("huoqu_buttons"))).click();
          } catch (Exception e) {
            continue;
          }
          throw new VerifyException("2");
        } else {
          continue;
        }
      }
      log.warn("采集上网流量异常：{}", driver.findElement(By.id("queryError")).getText());
      return "";
    }
    if (!url.equals("2")) {
      return url;
    }
    if (DriverUtils.isElementExist(driver, By.id("input"))) {
      wait.until(ExpectedConditions
          .presenceOfElementLocated(By.id("input"))).clear();
      wait.until(ExpectedConditions
          .presenceOfElementLocated(By.id("input"))).sendKeys(code);
      wait.until(ExpectedConditions
          .presenceOfElementLocated(By.id("sign_in"))).click();
    }

    Thread.sleep(1000 * 10);

    String cid = MD5.encode(phone);
    String nowDay = JodatimeUtils.now(JodatimeUtils.FORMAT_YYYYMMDD_01);
    for (int i = 0; i < 1; i++) {
      if (i == 0) {

      } else {
        String queryDay = DateTimeFormat.forPattern(JodatimeUtils.FORMAT_YYYYMMDD_01)
            .parseDateTime(nowDay)
            .minusDays(i).toString(JodatimeUtils.FORMAT_YYYYMMDD_01);
        wait.until(ExpectedConditions
            .presenceOfElementLocated(By.id("endTime"))).sendKeys(queryDay);
        Thread.sleep(1000 * 5);
      }

//      int page = 1;
      while (true) {
        String html = driver.getPageSource();
        Document doc = Jsoup.parse(html);

//        String page_ = "";
//        Elements optionEles = doc.select("#select_op option");
//        for (Element optionEle : optionEles) {
//          if (StringUtils.isNotEmpty(optionEle.attr("selected"))) {
//            page_ = optionEle.attr("value");
//          }
//        }
//        log.info("当前页码page={}", page);
//        log.info("当前页码page_={}", page_);
//        if (!page_.equals(page + "")) {
//          WebElement mySelectElm = driver.findElement(By.id("select_op"));
//          Select mySelect = new Select(mySelectElm);
//          mySelect.selectByValue(page + "");
//          Thread.sleep(1000 * 6);
//          continue;
//        }

        Elements eles = doc.select("#callFlowContent table tbody tr");
        if (eles == null) {
          break;
        }
        for (Element eleTr : eles) {
          Elements elesTh = eleTr.select("th");
          NetInfo netInfo = new NetInfo();
          netInfo.setCid(cid);
          netInfo.setTime(elesTh.get(0).text());
          netInfo.setLocation(elesTh.get(1).text());
          netInfo.setNetType(elesTh.get(2).text());
          netInfo.setServiceName(elesTh.get(3).text());
          netInfo.setSubflow(elesTh.get(5).text());
          netInfo.setFee(elesTh.get(6).text());
          netInfo.setCreateTime(new Date());
          netInfo.setId(MD5.encode(phone + netInfo.getTime()));

          try {
            netInfoDao.insert(netInfo);
          } catch (Exception e) {
            continue;
          }
        }
        if (!DriverUtils.isElementExist(driver,
            By.xpath("//div[@class='score_page_r' and contains(text(),'尾页')]"))) {
          break;
        }
        try {
          driver.switchTo().defaultContent();
          for (int j = 0; j < 10; j++) {
            if (driver.findElement(By.id("center_loadingBg")).getAttribute("style")
                .contains("block")) {
              Thread.sleep(1000 * 4);
            } else {
              break;
            }
          }
          driver.switchTo().frame(2);
          driver.findElement(By.xpath("//div[@class='score_page_r' and contains(text(),'下一页')]"))
              .click();
        } catch (Exception e) {
          break;
        }
        Thread.sleep(1000 * 5);
//        page++;
      }
    }
    log.info("{}采集{}信息结束<---", "联通", "上网流量");
    return null;
  }

  private void crawlerPackageInfo(ChromeDriver driver, String phone) throws Exception {
    log.info("{}采集{}信息开始--->", "联通", "套餐");
    String cid = MD5.encode(phone);
    driver.navigate().to("https://iservice.10010.com/e4/skip.html?menuCode=000100040001");
    Thread.sleep(1000 * 5);
    String html = driver.getPageSource();
    Document doc = Jsoup.parse(html);
    Element llEle = doc.selectFirst("#flowPackage");
    if (llEle != null) {
      Elements eles = llEle.select(".gapLR.padT15.flow4g_list");
      for (Element ele : eles) {
        try {
          PackageInfo packageInfo = new PackageInfo();
          packageInfo.setCid(cid);
          packageInfo.setItem(ele.selectFirst(".marginQueryT2").text());
          packageInfo.setTotal(ele.selectFirst("dl dd span").text());
          packageInfo.setUsed(ele.selectFirst("dt span").text());
          packageInfo.setUnit(ele.selectFirst("dl dd em").text());
          packageInfo.setCreateTime(new Date());
          packageInfo.setId(MD5.encode(phone + packageInfo.getItem()));
          packageInfoDao.insert(packageInfo);
        } catch (Exception e) {
        }
      }
    }
    Element yyEle = doc.selectFirst("#voicePackage");
    if (yyEle != null) {
      Elements eles = yyEle.select(".gapLR.padT15.flow4g_list");
      for (Element ele : eles) {
        try {
          PackageInfo packageInfo = new PackageInfo();
          packageInfo.setCid(cid);
          packageInfo.setItem(ele.selectFirst(".left.marginQueryT2").text());
          packageInfo.setTotal(ele.selectFirst("dl dd span").text());
          packageInfo.setUsed(ele.selectFirst("dt span").text());
          packageInfo.setUnit(ele.selectFirst("dl dd em").text());
          packageInfo.setCreateTime(new Date());
          packageInfo.setId(MD5.encode(phone + packageInfo.getItem()));
          packageInfoDao.insert(packageInfo);
        } catch (Exception e) {
        }
      }
    }
    log.info("{}采集{}信息结束<---", "联通", "套餐");
  }

  private void crawlerCallInfo(ChromeDriver driver, String phone) throws Exception {
    log.info("{}采集{}信息开始--->", "联通", "通话");
    String cid = MD5.encode(phone);
    String endDate;
    String beginDate = "";
    for (int i = 0; i < 6; i++) {
      if (i == 0) {
        endDate = JodatimeUtils.now(JodatimeUtils.FORMAT_YYYYMMDD_01);
        beginDate = getFirstDay(endDate);
      } else {
        String tmp = beginDate;
        beginDate = getLastBeginDate(tmp);
        endDate = getLastEndDate(tmp);
      }

      String url = URL_CALL_DETAIL2_PAGE.replace("BEGINDATE", beginDate)
          .replace("ENDDATE", endDate);
      driver.navigate().to(url);
      Thread.sleep(1000 * 5);
      while (true) {
        String html = driver.getPageSource();
        Document doc = Jsoup.parse(html);
        if (doc.selectFirst("#callDetailContent table") == null) {
          break;
        }
        Elements eles = doc.select("#callDetailContent table tbody tr");
        for (Element eleTr : eles) {
          try {
            Elements elesTh = eleTr.select("th");
            CallInfo callInfo = new CallInfo();
            callInfo.setCid(cid);
            callInfo.setTime(elesTh.get(2).text());
            callInfo.setDuration(elesTh.get(3).text());
            callInfo.setDialType(elesTh.get(4).text().equals("主叫") ? "DIAL" : "DIALED");
            callInfo.setPeerNumber(elesTh.get(5).text());
            callInfo.setLocation(elesTh.get(6).text());
            callInfo.setLocationType(elesTh.get(8).text());
            callInfo.setFee(elesTh.get(9).text());
            callInfo.setRemark("U");
            callInfo.setCreateTime(new Date());
            callInfo.setId(MD5.encode(phone + callInfo.getTime()));
            callInfoDao.insert(callInfo);
          } catch (Exception e) {
            continue;
          }
        }
        if (!DriverUtils.isElementExist(driver,
            By.xpath("//div[@class='score_page_r' and contains(text(),'尾页')]"))) {
          break;
        }
        driver.findElement(By.xpath("//div[@class='score_page_r' and contains(text(),'下一页')]"))
            .click();
        Thread.sleep(1000 * 5);
      }
    }
    log.info("{}采集{}信息结束<---", "联通", "通话");
  }


  private void crawlerBillInfo(ChromeDriver driver, String phone) throws Exception {
    log.info("{}采集{}信息开始--->", "联通", "账单");
    WebDriverWait wait = new WebDriverWait(driver, 30);
    String cid = MD5.encode(phone);
    String url = "https://iservice.10010.com/e4/skip.html?menuCode=000100020001";
    for (int i = 0; i < 3; i++) {
      driver.navigate().to(url);
      Thread.sleep(1000 * 5);
      if (url.equals(driver.getCurrentUrl())) {
        break;
      }
    }
    String nowMonth = JodatimeUtils.now(JodatimeUtils.FORMAT_YYYYMM_02);
    for (int i = 1; i <= 6; i++) {
      String yyyy_MM = DateTimeFormat.forPattern(JodatimeUtils.FORMAT_YYYYMM_02)
          .parseDateTime(nowMonth).minusMonths(i).toString(JodatimeUtils.FORMAT_YYYYMM_01);
      if (i == 1) {

      } else {
        String queryMonth = DateTimeFormat.forPattern(JodatimeUtils.FORMAT_YYYYMM_02)
            .parseDateTime(nowMonth)
            .minusMonths(i).toString(JodatimeUtils.FORMAT_YYYYMM_02);
        try {
          wait.until(
              ExpectedConditions
                  .presenceOfElementLocated(By.xpath("//li[@dat='" + queryMonth + "']"))).click();
        } catch (Exception e) {
          continue;
        }
        Thread.sleep(1000 * 5);
      }
      String html = driver.getPageSource();
      Document doc = Jsoup.parse(html);
      if (doc.selectFirst("#queryError").attr("style").contains("block")) {
        continue;
      }
      String base_fee = "";
      String extra_service_fee = "";
      String voice_fee = "";
      String web_fee = "";
      String sms_fee = "";
      String total_fee = "";

      if (doc.selectFirst(".historyBillA") != null) {
        if (doc.selectFirst(".historyBillA").text().contains("消费合计")) {
          total_fee = doc.selectFirst(".historyBillA").text().split("消费合计")[1];

        }
      }
      Elements eles = doc.select("#data_womemberinfoDetail .messageTitP");
      for (Element ele : eles) {
        String str = ele.text();
        if (str.contains("月固定费")) {
          base_fee = str.split("：")[1];
        } else if (str.contains("增值业务费")) {
          extra_service_fee = str.split("：")[1];
        } else if (str.contains("语音通话费")) {
          voice_fee = str.split("：")[1];
        } else if (str.contains("上网费")) {
          web_fee = str.split("：")[1];
        }
      }
      eles = doc.select("#data_womemberinfoDetail .messageUL");
      for (Element ele : eles) {
        String str = ele.text();
        if (str.contains("点对点短信")) {
          sms_fee = str.split("点对点短信")[1];
        }
      }

      BillInfo billInfo = new BillInfo();
      billInfo.setCid(cid);
      billInfo.setBillMonth(yyyy_MM);
      billInfo.setBaseFee(base_fee);
      billInfo.setExtraServiceFee(extra_service_fee);
      billInfo.setVoiceFee(voice_fee);
      billInfo.setWebFee(web_fee);
      billInfo.setSmsFee(sms_fee);
      billInfo.setTotalFee(total_fee);
      billInfo.setCreateTime(new Date());
      billInfo.setId(MD5.encode(phone + billInfo.getBillMonth()));

      try {
        billInfoDao.insert(billInfo);
      } catch (Exception e) {
        continue;
      }
    }
    log.info("{}采集{}信息结束<---", "联通", "账单");
  }

  public Result ckLogin(String sessionid, String pwd, String code, String url) {
    ChromeDriver driver = getDriver(sessionid);
    if (driver == null) {
      return new Result(Constants.SYSTEMERROR, "验证码过期请重新发送验证码");
    }
    try {
      WebDriverWait wait = new WebDriverWait(driver, 30);
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("userPwd"))).clear();
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("userPwd"))).sendKeys(pwd);
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("userCK"))).clear();
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("userCK"))).sendKeys(code);
      String block = driver.findElementById("checkAgreeDiv").getAttribute("style");
      if (block.contains("block")) {
        wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("checkAgree"))).click();
      }
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("login1"))).click();
      Thread.sleep(1000 * 2);
      if (DriverUtils.isElementExist(driver, By.id("error"))) {
        String error = driver.findElementById("error").getText();
        return new Result(Constants.SYSTEMERROR, error);
      }
//      wait.until(
//          ExpectedConditions.presenceOfElementLocated(By.id("personalInfo")));
//      if (!URL_LOGIN_PAGE.equals(driver.getCurrentUrl())) {
//        return new Result(Constants.SYSTEMERROR, "密码或验证码不正确");
//      }
    } catch (Exception e) {
      e.printStackTrace();
      if (driver != null) {
        driver.quit();
        delDriver(sessionid);
      }
    }
    return new Result(Constants.SUCCESS, Constants.getMessage(Constants.SUCCESS));
//    return crawlerData(sessionid, code, url);
  }

  // 采集个人信息
  public Result custinfoQry(String sessionId) {
    String nm = getPhone(sessionId);//获取手机号
    try {
      ChromeDriver driver = getDriver(sessionId);
      log.info("{}采集{}信息开始--->", "联通", "基本");
      driver.navigate().to(URL_CUSTOMER_INFO_PAGE_);
      Thread.sleep(1000 * 10);
      driver.switchTo().frame(2);
      String html = driver.getPageSource();
      Document doc = Jsoup.parse(html);
      CustomerInfo customerInfo = new CustomerInfo();
      Elements trEles = doc.select(".ly_gr_l table tr");
      customerInfo.setMobile(trEles.get(0).selectFirst("td").text());
      customerInfo.setId(MD5.encode(customerInfo.getMobile()));
      customerInfo.setLevel(trEles.get(1).selectFirst("td").text());
      Elements dlEles = doc.select("#userInfocontext .data_basic_c.ly_gr_zl dl");
      customerInfo.setName(dlEles.get(0).selectFirst("dd").text());
      customerInfo.setIdcard(dlEles.get(2).selectFirst("dd").text());
      dlEles = doc.select(".ly_gr_l2 dl");
      customerInfo.setPackageName(dlEles.get(0).selectFirst("dd").text());
      customerInfo.setOpenTime(doc.selectFirst(".data_basic_c2_r dl dd").text());

      for (int i = 1; i <= 3; i++) {
        driver.navigate().to("https://iservice.10010.com/e4/skip.html?menuCode=000100010002");
        Thread.sleep(5000 * i);
        if (DriverUtils.isElementExist(driver, By.id("h_2"))) {
          break;
        }
      }
      html = driver.getPageSource();
      String balance = Jsoup.parse(html).selectFirst("#h_2").text();
      customerInfo.setAvailableBalance(balance);
      customerInfo.setIsCert("1");
      customerInfo.setState("1");
      customerInfo.setCreateTime(new Date());
      customerInfoDao.insert(customerInfo);
      log.info("{}采集{}信息结束<---", "联通", "基本");
      return new Result(Constants.SUCCESS, JSON.toJSON(customerInfo),"个人信息采集成功");
    } catch (Exception e) {
      e.printStackTrace();
      log.warn("采集个人信息失败 {}, {}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }

  // 采集账单信息
  public Result billinfoQry(String sessionId) {
    String nm = getPhone(sessionId);//获取手机号
    try {
      ChromeDriver driver = getDriver(sessionId);
      log.info("{}采集{}信息开始--->", "联通", "账单");
      WebDriverWait wait = new WebDriverWait(driver, 10);
      String cid = MD5.encode(nm);
      String url = "https://iservice.10010.com/e4/skip.html?menuCode=000100020001";
      for (int i = 0; i < 3; i++) {
        driver.navigate().to(url);
        Thread.sleep(1000 * 5);
        String html = driver.getPageSource();
        Document doc = Jsoup.parse(html);
        if (doc.selectFirst("#queryError") != null) {
          break;
        }
      }
      List<BillInfo> list = new ArrayList<>();
      String nowMonth = JodatimeUtils.now(JodatimeUtils.FORMAT_YYYYMM_02);
      for (int i = 1; i <= 6; i++) {
        String yyyy_MM = DateTimeFormat.forPattern(JodatimeUtils.FORMAT_YYYYMM_02)
            .parseDateTime(nowMonth).minusMonths(i).toString(JodatimeUtils.FORMAT_YYYYMM_01);
        if (i == 1) {

        } else {
          String queryMonth = DateTimeFormat.forPattern(JodatimeUtils.FORMAT_YYYYMM_02)
              .parseDateTime(nowMonth)
              .minusMonths(i).toString(JodatimeUtils.FORMAT_YYYYMM_02);
          try {
            wait.until(
                ExpectedConditions
                    .presenceOfElementLocated(By.xpath("//li[@dat='" + queryMonth + "']"))).click();
          } catch (Exception e) {
            continue;
          }
        }
        Thread.sleep(1000 * 7);
        String html = driver.getPageSource();
        Document doc = Jsoup.parse(html);
        if (doc.selectFirst("#queryError").attr("style").contains("block") || StringUtils
            .isEmpty(doc.selectFirst("#queryError").attr("style"))) {
          continue;
        }
        String base_fee = "";
        String extra_service_fee = "";
        String voice_fee = "";
        String web_fee = "";
        String sms_fee = "";
        String total_fee = "";

        if (doc.selectFirst(".historyBillA") != null) {
          if (doc.selectFirst(".historyBillA").text().contains("消费合计")) {
            total_fee = doc.selectFirst(".historyBillA").text().split("消费合计")[1];

          }
        }
        Elements eles = doc.select("#data_womemberinfoDetail .messageTitP");

        for (Element ele : eles) {
          String str = ele.text();
          if (str.contains("月固定费")) {
            base_fee = str.split("：")[1];
          } else if (str.contains("增值业务费")) {
            extra_service_fee = str.split("：")[1];
          } else if (str.contains("语音通话费")) {
            voice_fee = str.split("：")[1];
          } else if (str.contains("上网费")) {
            web_fee = str.split("：")[1];
          }
        }
        eles = doc.select("#data_womemberinfoDetail .messageUL");
        for (Element ele : eles) {
          String str = ele.text();
          if (str.contains("点对点短信")) {
            sms_fee = str.split("点对点短信")[1];
          }
        }

        BillInfo billInfo = new BillInfo();
        billInfo.setCid(cid);
        billInfo.setBillMonth(yyyy_MM);
        billInfo.setBaseFee(base_fee);
        billInfo.setExtraServiceFee(extra_service_fee);
        billInfo.setVoiceFee(voice_fee);
        billInfo.setWebFee(web_fee);
        billInfo.setSmsFee(sms_fee);
        billInfo.setTotalFee(total_fee);
        billInfo.setCreateTime(new Date());
        billInfo.setId(MD5.encode(nm + billInfo.getBillMonth()));
        try {
          billInfoDao.insert(billInfo);
          list.add(billInfo);
        } catch (Exception e) {
          continue;
        }
      }
      log.info("{}采集{}信息结束<---", "联通", "账单");
      return new Result(Constants.SUCCESS, JSON.toJSON(list),"账单信息采集成功");
    } catch (Exception e) {
      e.printStackTrace();
      log.warn("采集账单信息失败 {}, {}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }

  /**
   * 通话语音信息
   */
  public Result callinfoQry(String sessionId) {
    String nm = getPhone(sessionId);//获取手机号
    try {
      ChromeDriver driver = getDriver(sessionId);
      log.info("{}采集{}信息开始--->", "联通", "通话");

      if (!driver.getCurrentUrl().equals(URL_CALL_DETAIL_PAGE)) {
        driver.navigate().to(URL_CALL_DETAIL_PAGE);
        Thread.sleep(1000 * 4);
      }

      if (!driver.findElementById("yanzhengma").getAttribute("style").contains("none")) {
        driver.findElementById("huoqu_buttons").click();
        throw new VerifyException("3");
      }

      String cid = MD5.encode(nm);
      String endDate;
      String beginDate = "";
      List<CallInfo> list = new ArrayList<>();
      for (int i = 0; i < 6; i++) {
        if (i == 0) {
          endDate = JodatimeUtils.now(JodatimeUtils.FORMAT_YYYYMMDD_01);
          beginDate = getFirstDay(endDate);
        } else {
          String tmp = beginDate;
          beginDate = getLastBeginDate(tmp);
          endDate = getLastEndDate(tmp);
        }

        String url = URL_CALL_DETAIL2_PAGE.replace("BEGINDATE", beginDate)
            .replace("ENDDATE", endDate);
        driver.navigate().to(url);
        Thread.sleep(1000 * 5);
        while (true) {
          String html = driver.getPageSource();
          Document doc = Jsoup.parse(html);
          if (doc.selectFirst("#callDetailContent table") == null) {
            break;
          }
          Elements eles = doc.select("#callDetailContent table tbody tr");
          for (Element eleTr : eles) {
            try {
              Elements elesTh = eleTr.select("th");
              CallInfo callInfo = new CallInfo();
              callInfo.setCid(cid);
              callInfo.setTime(elesTh.get(2).text());
              callInfo.setDuration(elesTh.get(3).text());
              callInfo.setDialType(elesTh.get(4).text().equals("主叫") ? "DIAL" : "DIALED");
              callInfo.setPeerNumber(elesTh.get(5).text());
              callInfo.setLocation(elesTh.get(6).text());
              callInfo.setLocationType(elesTh.get(8).text());
              callInfo.setFee(elesTh.get(9).text());
              callInfo.setRemark("U");
              callInfo.setCreateTime(new Date());
              callInfo.setId(MD5.encode(nm + callInfo.getTime()));
              callInfoDao.insert(callInfo);
              list.add(callInfo);
            } catch (Exception e) {
              continue;
            }
          }
          if (!DriverUtils.isElementExist(driver,
              By.xpath("//div[@class='score_page_r' and contains(text(),'尾页')]"))) {
            break;
          }
          driver.findElement(By.xpath("//div[@class='score_page_r' and contains(text(),'下一页')]"))
              .click();
          Thread.sleep(1000 * 5);
        }
      }
      log.info("{}采集{}信息结束<---", "联通", "通话");
      return new Result(Constants.SUCCESS, JSON.toJSON(list),"通话语音信息采集成功");
    } catch (VerifyException ve) {
      JSONObject data = new JSONObject();
      data.put("urlCode", ve.getUrl());
      return new Result(Constants.NEW_AUTH, data,"采集中断需要验证码，请注意查收; 位置标示码：" + ve.getUrl());
    } catch (Exception e) {
      e.printStackTrace();
      log.warn("采集通话语音信息失败 {}, {}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR),"采集通话语音信息失败");
    }
  }


  /**
   * 短信彩信信息
   */
  public Result smsinfoQry(String sessionId) {
    String nm = getPhone(sessionId);//获取手机号
    try {
      ChromeDriver driver = getDriver(sessionId);
      log.info("{}采集{}信息开始--->", "联通", "短信");
      WebDriverWait wait = new WebDriverWait(driver, 10);

      if (!driver.getCurrentUrl().equals(URL_SMS_PAGE)) {
        driver.navigate().to(URL_SMS_PAGE);
        Thread.sleep(1000 * 4);
        driver.switchTo().frame(2);
      }

      if (!driver.findElementById("yanzhengma").getAttribute("style").contains("none")) {
        driver.findElementById("huoqu_buttons").click();
        throw new VerifyException("1");
      }

      String cid = MD5.encode(nm);
      String nowMonth = JodatimeUtils.now(JodatimeUtils.FORMAT_YYYYMM_02);
      List<SmsInfo> list = new ArrayList();
      for (int i = 0; i < 6; i++) {
        if (i == 0) {

        } else {
          String queryMonth = DateTimeFormat.forPattern(JodatimeUtils.FORMAT_YYYYMM_02)
              .parseDateTime(nowMonth)
              .minusMonths(i).toString(JodatimeUtils.FORMAT_YYYYMM_02);

          if (!DriverUtils.isElementExist(driver, By.xpath("//li[@value='" + queryMonth + "']"))) {
            continue;
          }
          driver.switchTo().defaultContent();
          driver.executeScript("window.scrollTo(0,0)");
          Thread.sleep(1000);
          driver.switchTo().frame(2);
          wait.until(ExpectedConditions
              .presenceOfElementLocated(By.xpath("//li[@value='" + queryMonth + "']"))).click();
          Thread.sleep(1000 * 5);
        }

        while (true) {
          String html = driver.getPageSource();
          Document doc = Jsoup.parse(html);
          if (doc.selectFirst("#smsmmsResultTab").attr("style").contains("none")) {
            break;
          }
          Elements eleTr = doc.select("#smsmmsResultTab table tbody tr");
          for (int j = 0; j < eleTr.size(); j++) {
            Elements elesTh = eleTr.get(j).select("th");
            SmsInfo smsInfo = new SmsInfo();
            smsInfo.setCid(cid);
            smsInfo.setTime(elesTh.get(0).text());
            smsInfo.setMsgType(elesTh.get(1).text().contains("短信") ? "SMS" : "MMS");
            smsInfo.setSendType(elesTh.get(2).text().equals("发送") ? "SEND" : "RECEIVE");
            smsInfo.setPeerNumber(elesTh.get(3).text());
            smsInfo.setFee(elesTh.get(4).text());
            smsInfo.setCreateTime(new Date());
            smsInfo.setId(MD5.encode(nm + smsInfo.getTime()));
            try {
              smsInfoDao.insert(smsInfo);
              list.add(smsInfo);
            } catch (Exception e) {
              continue;
            }
          }
          if (!DriverUtils.isElementExist(driver,
              By.xpath("//div[@class='score_page_r' and contains(text(),'尾页')]"))) {
            break;
          }
          try {
            driver.switchTo().defaultContent();
            for (int j = 0; j < 10; j++) {
              if (driver.findElement(By.id("center_loadingBg")).getAttribute("style")
                  .contains("block")) {
                Thread.sleep(1000 * 4);
              } else {
                break;
              }
            }
            driver.switchTo().frame(2);
            driver.findElement(By.xpath("//div[@class='score_page_r' and contains(text(),'下一页')]"))
                .click();
          } catch (Exception e) {
            break;
          }
          Thread.sleep(1000 * 5);
        }
      }
      log.info("{}采集{}信息结束<---", "联通", "短信");
      return new Result(Constants.SUCCESS, JSON.toJSON(list),"短信彩信信息采集成功");
    } catch (VerifyException ve) {
      JSONObject data = new JSONObject();
      data.put("urlCode", ve.getUrl());
      return new Result(Constants.NEW_AUTH, data,"采集中断需要验证码，请注意查收; 位置标示码：" + ve.getUrl());
    } catch (Exception e) {
      e.printStackTrace();
      log.warn("采集短信彩信信息失败 {}, {}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR),"短信彩信信息失败");
    }
  }

  /**
   * 上网流量信息
   */
  public Result netinfoQry(String sessionId) {
    String nm = getPhone(sessionId);//获取手机号
    try {
      ChromeDriver driver = getDriver(sessionId);
      log.info("{}采集{}信息开始--->", "联通", "上网流量");
      WebDriverWait wait = new WebDriverWait(driver, 10);

//      if (!URL_NET_PAGE.equals(driver.getCurrentUrl())) {
        driver.navigate().to(URL_NET_PAGE);
        Thread.sleep(1000 * 8);
//      }

//      driver.switchTo().defaultContent();
      if (!driver.findElementById("yanzhengma").getAttribute("style").contains("none")) {
        driver.findElementById("huoqu_buttons").click();
        throw new VerifyException("2");
      }

      String cid = MD5.encode(nm);
      String nowDay = JodatimeUtils.now(JodatimeUtils.FORMAT_YYYYMMDD_01);
      List<NetInfo> list = new ArrayList<>();
      for (int i = 0; i < 1; i++) {
        if (i == 0) {

        } else {
          String queryDay = DateTimeFormat.forPattern(JodatimeUtils.FORMAT_YYYYMMDD_01)
              .parseDateTime(nowDay)
              .minusDays(i).toString(JodatimeUtils.FORMAT_YYYYMMDD_01);
          wait.until(ExpectedConditions
              .presenceOfElementLocated(By.id("endTime"))).sendKeys(queryDay);
          Thread.sleep(1000 * 5);
        }
//      int page = 1;
        while (true) {
          String html = driver.getPageSource();
          Document doc = Jsoup.parse(html);

          Elements eles = doc.select("#callFlowContent table tbody tr");
          if (eles == null) {
            break;
          }
          for (Element eleTr : eles) {
            Elements elesTh = eleTr.select("th");
            NetInfo netInfo = new NetInfo();
            netInfo.setCid(cid);
            netInfo.setTime(elesTh.get(0).text());
            netInfo.setLocation(elesTh.get(1).text());
            netInfo.setNetType(elesTh.get(2).text());
            netInfo.setServiceName(elesTh.get(3).text());
            netInfo.setSubflow(elesTh.get(5).text());
            netInfo.setFee(elesTh.get(6).text());
            netInfo.setCreateTime(new Date());
            netInfo.setId(MD5.encode(nm + netInfo.getTime()));

            try {
              netInfoDao.insert(netInfo);
              list.add(netInfo);
            } catch (Exception e) {
              continue;
            }
          }
          if (!DriverUtils.isElementExist(driver,
              By.xpath("//div[@class='score_page_r' and contains(text(),'尾页')]"))) {
            break;
          }
          try {
            driver.switchTo().defaultContent();
            for (int j = 0; j < 10; j++) {
              if (driver.findElement(By.id("center_loadingBg")).getAttribute("style")
                  .contains("block")) {
                Thread.sleep(1000 * 4);
              } else {
                break;
              }
            }
//            driver.switchTo().frame(2);
            driver.findElement(By.xpath("//div[@class='score_page_r' and contains(text(),'下一页')]"))
                .click();
          } catch (Exception e) {
            break;
          }
          Thread.sleep(1000 * 5);
//        page++;
        }
      }
      log.info("{}采集{}信息结束<---", "联通", "上网流量");
      return new Result(Constants.SUCCESS, JSON.toJSON(list),"上网流量信息采集成功");
    } catch (VerifyException ve) {
      JSONObject data = new JSONObject();
      data.put("urlCode",ve.getUrl());
      return new Result(Constants.NEW_AUTH, data,"采集中断需要验证码，请注意查收; 位置标示码：" + ve.getUrl());
    } catch (Exception e) {
      e.printStackTrace();
      log.warn("采集上网流量信息息失败 {}, {}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR),"上网流量信息采集失败");
    }
  }

  /**
   * 充值记录信息
   */
  public Result rechargeQry(String sessionId) {
    String nm = getPhone(sessionId);//获取手机号
    try {
      ChromeDriver driver = getDriver(sessionId);
      log.info("{}采集{}信息开始--->", "联通", "缴费");
      WebDriverWait wait = new WebDriverWait(driver, 30);
      driver.navigate().to("https://iservice.10010.com/e4/query/calls/paid_record-iframe.html");
      Thread.sleep(1000 * 4);
      // driver.switchTo().frame(2);
      String nowMonth = JodatimeUtils.now(JodatimeUtils.FORMAT_YYYYMM_02);
      List<RechargeInfo> list = new ArrayList<>();
      for (int i = 0; i < 6; i++) {
        driver.switchTo().defaultContent();
        for (int j = 0; j < 10; j++) {
          if (driver.findElement(By.id("center_loadingBg")).getAttribute("style")
              .contains("block")) {
            Thread.sleep(1000 * 4);
          } else {
            break;
          }
        }
        driver.switchTo().frame(2);
        if (i == 0) {

        } else {
          String queryMonth = DateTimeFormat.forPattern(JodatimeUtils.FORMAT_YYYYMM_02)
              .parseDateTime(nowMonth)
              .minusMonths(i).toString(JodatimeUtils.FORMAT_YYYYMM_02);

          if (!DriverUtils
              .isElementExist(driver, By.xpath("//li[@value=\"" + queryMonth + "\"]"))) {
            continue;
          }
          try {
            driver.switchTo().defaultContent();
            driver.executeScript("window.scrollTo(0,0)");
            Thread.sleep(1000);
            driver.switchTo().frame(2);
            wait.until(ExpectedConditions
                .presenceOfElementLocated(By.xpath("//li[@value=\"" + queryMonth + "\"]"))).click();
          } catch (Exception e) {
            driver.navigate()
                .to("https://iservice.10010.com/e4/query/calls/paid_record-iframe.html");
            Thread.sleep(1000 * 4);
            driver.switchTo().frame(2);
            Thread.sleep(1000);
            continue;
          }
          Thread.sleep(1000 * 4);
        }
        String html = driver.getPageSource();
        Document doc = Jsoup.parse(html);
        if (doc.selectFirst("#paymentRecordContent") == null) {
          log.warn("采集缴费信息网络异常");
          return new Result(Constants.SYSTEMERROR, "采集缴费信息网络异常");
        }
        if (doc.selectFirst("#paymentRecordContent").attr("style").contains("none")) {
          continue;
        }
        Elements trEles = doc.select("#paymentRecordContent table tr");
        for (int j = 0; j < trEles.size(); j++) {
          if (j > 0) {
            RechargeInfo rechargeInfo = new RechargeInfo();
            rechargeInfo.setCid(MD5.encode(nm));
            rechargeInfo.setRechargeTime(trEles.get(j).select("th").get(0).text());
            rechargeInfo.setType(trEles.get(j).select("th").get(1).text());
            rechargeInfo.setAmount(trEles.get(j).select("th").get(3).text());
            rechargeInfo.setCreateTime(new Date());
            rechargeInfo.setId(MD5.encode(nm + rechargeInfo.getRechargeTime()));
            try {
              rechargeInfoDao.insert(rechargeInfo);
              list.add(rechargeInfo);
            } catch (Exception e) {
              continue;
            }
          }
        }
      }
      log.info("{}采集{}信息结束<---", "联通", "缴费");
      return new Result(Constants.SUCCESS, JSON.toJSON(list),"充值记录信息采集成功");
    } catch (Exception e) {
      e.printStackTrace();
      log.warn("采集充值记录信息失败 {}, {}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }

  /**
   * 套餐信息
   */
  public Result packageQry(String sessionId) {
    String nm = getPhone(sessionId);//获取手机号
    try {
      ChromeDriver driver = getDriver(sessionId);
      log.info("{}采集{}信息开始--->", "联通", "套餐");
      String cid = MD5.encode(nm);
      driver.navigate().to("https://iservice.10010.com/e4/skip.html?menuCode=000100040001");
      Thread.sleep(1000 * 5);
      String html = driver.getPageSource();
      Document doc = Jsoup.parse(html);
      Element llEle = doc.selectFirst("#flowPackage");
      List<PackageInfo> list = new ArrayList<>();
      if (llEle != null) {
        Elements eles = llEle.select(".gapLR.padT15.flow4g_list");
        for (Element ele : eles) {
          try {
            PackageInfo packageInfo = new PackageInfo();
            packageInfo.setCid(cid);
            packageInfo.setItem(ele.selectFirst(".marginQueryT2").text());
            packageInfo.setTotal(ele.selectFirst("dl dd span").text());
            packageInfo.setUsed(ele.selectFirst("dt span").text());
            packageInfo.setUnit(ele.selectFirst("dl dd em").text());
            packageInfo.setCreateTime(new Date());
            packageInfo.setId(MD5.encode(nm + packageInfo.getItem()));
            packageInfoDao.insert(packageInfo);
            list.add(packageInfo);
          } catch (Exception e) {
          }
        }
      }
      Element yyEle = doc.selectFirst("#voicePackage");
      if (yyEle != null) {
        Elements eles = yyEle.select(".gapLR.padT15.flow4g_list");
        for (Element ele : eles) {
          try {
            PackageInfo packageInfo = new PackageInfo();
            packageInfo.setCid(cid);
            packageInfo.setItem(ele.selectFirst(".left.marginQueryT2").text());
            packageInfo.setTotal(ele.selectFirst("dl dd span").text());
            packageInfo.setUsed(ele.selectFirst("dt span").text());
            packageInfo.setUnit(ele.selectFirst("dl dd em").text());
            packageInfo.setCreateTime(new Date());
            packageInfo.setId(MD5.encode(nm + packageInfo.getItem()));
            packageInfoDao.insert(packageInfo);
            list.add(packageInfo);
          } catch (Exception e) {
          }
        }
      }
      log.info("{}采集{}信息结束<---", "联通", "套餐");
      return new Result(Constants.SUCCESS, JSON.toJSON(list),"套餐信息采集成功");
    } catch (Exception e) {
      e.printStackTrace();
      log.warn("采集套餐信息失败 {}, {}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }

  public Result auth(String sessionId, String code, String urlCode) {
    try {
      ChromeDriver driver = getDriver(sessionId);
      WebDriverWait wait = new WebDriverWait(driver, 10);
      wait.until(ExpectedConditions
          .presenceOfElementLocated(By.id("input"))).clear();
      wait.until(ExpectedConditions
          .presenceOfElementLocated(By.id("input"))).sendKeys(code);
      wait.until(ExpectedConditions
          .presenceOfElementLocated(By.id("sign_in"))).click();
      Thread.sleep(1000);
      if (!driver.findElementById("yanzhengma").getAttribute("style").contains("none")) {
        String html = driver.getPageSource();
        Document doc = Jsoup.parse(html);
        String msg = "验证码错误，请重新输入";
        if (StringUtils.isNotEmpty(doc.selectFirst("#yanzhengma_sms .error_tips").text())) {
          msg = doc.selectFirst("#yanzhengma_sms .error_tips").text();
        }
        return new Result(Constants.SUCCESS, msg);
      }

      if (StringUtils.isNotEmpty(urlCode)) {
        if ("1".equals(urlCode)) {
          // 短信信息
          return smsinfoQry(sessionId);
        } else if ("2".equals(urlCode)) {
          // 上网信息
          return netinfoQry(sessionId);
        } else if ("3".equals(urlCode)) {
          // 通话语音信息
          return callinfoQry(sessionId);
        }
      }
      return new Result(Constants.SUCCESS, "success");
    } catch (Exception e) {
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }

  // 关闭
  public Result quit(String sessionId) {
    try {
      ChromeDriver driver = getDriver(sessionId);
      if (driver != null) {
        driver.quit();
      }
      return new Result(Constants.SUCCESS, "success");
    } catch (Exception e) {
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }


  public Result crawlerData(String sessionid, String code, String url) {
    ChromeDriver driver = getDriver(sessionid);
    String phone = getPhone(sessionid);
    try {
      // 短信信息
      url = crawlerSmsInfo(driver, phone, code, url);
      // 上网信息
      crawlerNetInfo(driver, phone, code, url);
      // 账单信息
      crawlerBillInfo(driver, phone);
      // 用户信息
      crawlerCustomerInfo(driver, new CustomerInfo());
      // 充值记录信息
      crawlerRechargeInfo(driver, phone);
      // 套餐信息
      crawlerPackageInfo(driver, phone);
      // 通话语音信息
      crawlerCallInfo(driver, phone);

      if (driver != null) {
        driver.quit();
        delDriver(sessionid);
      }
    } catch (VerifyException ve) {
      JSONObject data = new JSONObject();
      data.put("urlCode", ve.getUrl());
      return new Result(Constants.NEW_AUTH, data,"采集中断需要验证码，请注意查收; 位置标示码:" + ve.getUrl());
    } catch (AliVerifyException ae) {
      return new Result(Constants.SYSTEMERROR, ae.getAliVerify(),"信息采集失败:"+ ae.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      if (driver != null) {
        driver.quit();
        delDriver(sessionid);
      }
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR),"信息采集失败:"+e.getMessage());
    }
    return new Result(Constants.SUCCESS, Constants.getMessage(Constants.SUCCESS),"信息采集成功");
  }

  public Result inputNum(String sessionid, String num) {
    ChromeDriver driver = getDriver(sessionid);
    if (driver == null) {
      return new Result(Constants.SYSTEMERROR, "登陆信息过期，请重新登录");
    }
    try {
      WebElement ele = driver.findElementById("input");
      if (!URL_CALL_DETAIL_PAGE.equals(driver.getCurrentUrl()) || ele == null) {
        return new Result(Constants.SYSTEMERROR, "登陆信息过期，请重新登录");
      }

      ele.sendKeys(num);
      driver.findElementById("sign_in").click();

      Thread.sleep(10 * 1000);
      String html = driver.getPageSource();
      Document doc = Jsoup.parse(html);
      Element e = doc.selectFirst(".baocuo_r p");
      if (e != null) {
        return new Result(Constants.SUCCESS, e.text());
      }

      crawlerCallDetailContent(getPhone(sessionid), driver);
    } catch (Exception e) {
      driver.quit();
      e.printStackTrace();
    }

    return new Result(Constants.SUCCESS, null);
  }


  private void crawlerCallDetailContent(String phone, ChromeDriver driver) {
    CommDetail m;
    String endDate;
    String beginDate = "";
    try {
      for (int i = 0; i < 6; i++) {
        if (i == 0) {
          endDate = JodatimeUtils.now(JodatimeUtils.FORMAT_YYYYMMDD_01);
          beginDate = getFirstDay(endDate);
        } else {
          String tmp = beginDate;
          beginDate = getLastBeginDate(tmp);
          endDate = getLastEndDate(tmp);
        }

        String url = URL_CALL_DETAIL2_PAGE.replace("BEGINDATE", beginDate)
            .replace("ENDDATE", endDate);
        driver.navigate().to(url);
        Thread.sleep(1000 * 2);

        String html = driver.getPageSource();
        Document doc = Jsoup.parse(html);
        if (doc.selectFirst("#callDetailContent table") == null) {
          continue;
        }
        Elements eles = doc.select("#callDetailContent table tbody tr");
        System.out.println(eles.toString());
        for (Element eleTr : eles) {
          m = new CommDetail();
          Elements elesTh = eleTr.select("th");
          m.setNm(phone);
          m.setStarttime(elesTh.get(2).text());
          m.setId(getCommDetailID(phone, m.getStarttime()));
          m.setCommtime(elesTh.get(3).text());
          m.setCommmode(elesTh.get(4).text());
          m.setAnothernm(elesTh.get(5).text());
          m.setCommplac(elesTh.get(7).text());
          m.setCommtype(elesTh.get(8).text());
          m.setCommfee(elesTh.get(9).text());
          m.setRemark("U");
//          commDetailDao.insert(m);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (driver != null) {

      }
    }
  }


  private void crawlerCallData(ChromeDriver driver) {
    CommDetail m;
    try {
      for (int i = 0; i < 6; i++) {
        if (i > 0) {
          String date = JodatimeUtils.now(JodatimeUtils.FORMAT_YYYYMMDD_01);
          date = getFirstDay(date);
          String beginDate = getLastBeginDate(date);
          String endDate = getLastEndDate(date);
          driver.findElementById("beginDate").sendKeys(beginDate);
          driver.findElementById("endDate").sendKeys(endDate);
          driver.findElementById("query").click();
        }
        String html = driver.getPageSource();
        Document doc = Jsoup.parse(html);
        if (doc.selectFirst("#callDetailContent table") == null) {
          continue;
        }
        Elements eles = doc.select("#callDetailContent table tbody tr");
        System.out.println(eles.toString());
        for (Element eleTr : eles) {
          m = new CommDetail();
          Elements elesTh = eleTr.select("th");
          log.info("插入数据：{}", JSON.toJSONString(m));
//        commDetailDao.insertSelective(m);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }


  private String getFirstDay(String date) {
    return DateTimeFormat.forPattern(JodatimeUtils.FORMAT_YYYYMMDD_01).parseDateTime(date)
        .minusDays(new DateTime().getDayOfMonth()).plusDays(1)
        .toString(JodatimeUtils.FORMAT_YYYYMMDD_01);
  }

  private String getLastBeginDate(String date) {
    return DateTimeFormat.forPattern(JodatimeUtils.FORMAT_YYYYMMDD_01).parseDateTime(date)
        .minusMonths(1).toString(JodatimeUtils.FORMAT_YYYYMMDD_01);
  }

  private String getLastEndDate(String date) {
    return DateTimeFormat.forPattern(JodatimeUtils.FORMAT_YYYYMMDD_01).parseDateTime(date)
        .minusDays(1).toString(JodatimeUtils.FORMAT_YYYYMMDD_01);
  }

  public static void main(String[] args) {
    ChromeDriver driver = null;
    try {
      driver = new ChromeDriver();
      driver.get("https://www.youtube.com/");
//      driver.executeScript("document.documentElement.scrollTop=0");
      driver.executeScript("window.scrollTo(0,0)");

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (driver != null) {
        driver.quit();
      }
    }

  }

  public static void main2(String[] args) {
    ChromeDriver driver = null;
    try {
      ChromeOptions option = new ChromeOptions();
      driver = new ChromeDriver(option);
      driver.get("http://www.baidu.com");

      File file = driver.getScreenshotAs(OutputType.FILE);
      BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
      BufferedOutputStream out = new BufferedOutputStream(
          new FileOutputStream("/Users/longlongl/work/tt_bak/copy2.png"));
      int i;
      while ((i = in.read()) != -1) {
        out.write(i);
      }
      out.flush();

      out.close();
      in.close();

      BufferedImage fullImg = ImageIO.read(file);
      WebElement ele = driver.findElementByClassName("qrcode-img");
      Point point = ele.getLocation();
      // Get width and height of the element
      int eleWidth = ele.getSize().getWidth();
      int eleHeight = ele.getSize().getHeight();

      BufferedImage eleScreenshot = fullImg.getSubimage(point.getX(), point.getY(),
          eleWidth, eleHeight);
      ImageIO.write(eleScreenshot, "png", file);

      File screenshotLocation = new File("/Users/longlongl/work/tt_bak/copy.png");
      FileUtils.copyFile(file, screenshotLocation);


    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (driver != null) {
        driver.quit();
      }
    }
  }

}
