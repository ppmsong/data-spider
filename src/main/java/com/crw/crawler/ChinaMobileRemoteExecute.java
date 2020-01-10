package com.crw.crawler;

import com.alibaba.fastjson.JSON;
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
import com.crw.contact.entity.CustomerInfo;
import com.crw.contact.entity.NetInfo;
import com.crw.contact.entity.PackageInfo;
import com.crw.contact.entity.RechargeInfo;
import com.crw.contact.entity.SmsInfo;
import com.crw.util.DriverUtils;
import com.crw.util.JodatimeUtils;
import com.crw.util.MD5;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChinaMobileRemoteExecute extends BaseRemoteExecute {

  private static final Logger logger = LogManager
      .getLogger(ChinaMobileRemoteExecute.class.getName());

  private static final String BILLQRY_URL = "https://shop.10086.cn/i/?f=billqry";
  private static final String BILLDETAILQRY_URL = "https://shop.10086.cn/i/?f=billdetailqry";

  private static final String URL_LOGIN_PAGE = "https://login.10086.cn/";

  @Autowired
  private CustomerInfoDao customerInfoDao;

  @Autowired
  private BillInfoDao billInfoDao;

  @Autowired
  private CallInfoDao callInfoDao;

  @Autowired
  private SmsInfoDao smsInfoDao;

  @Autowired
  private NetInfoDao netInfoDao;

  @Autowired
  private RechargeInfoDao rechargeInfoDao;

  @Autowired
  private PackageInfoDao packageInfoDao;

  public Result loginForm() {
    try {
      WebDriver driver = new ChromeDriver();
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
      driver.get("https://login.10086.cn/");
      driver.manage().window().maximize();
      return new Result(Constants.SUCCESS, ((RemoteWebDriver) driver).getSessionId().toString());
    } catch (Exception e) {
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }

  /**
   * 登陆短信验证码发送
   */
  public Result getSMSPwd(String sessionId, String login) {
    try {

      ChromeOptions chromeOptions = new ChromeOptions();
      chromeOptions.addArguments("headless");//无界面参数
      chromeOptions.addArguments("no-sandbox");//禁用沙盒
      //chromeOptions.addArguments("window-size=960,1080");
      chromeOptions.addArguments("--disable-notifications");

      ChromeDriver driver = getDriver(sessionId);
      if (driver != null) {
        if (!"https://login.10086.cn/".equals(driver.getCurrentUrl())) {
          driver.get("https://login.10086.cn/");
        }
      } else {
        driver = new ChromeDriver(chromeOptions);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get("https://login.10086.cn/");
      }

      driver.findElement(By.id("sms_login_1")).click();
      driver.findElement(By.id("sms_name")).sendKeys(login.trim());

      WebDriverWait wait = new WebDriverWait(driver, 10);
      wait.until(ExpectedConditions.elementToBeClickable(By.id("getSMSPwd1"))).click();
      putDriver(sessionId, driver);
      putPhone(sessionId, login);

      return new Result(Constants.SUCCESS, Constants.getMessage(Constants.SUCCESS));
    } catch (Exception e) {
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }


  /**
   * 登陆
   */
  public Result login(String sessionId, String smsCode) {
    try {
      ChromeDriver driver = getDriver(sessionId);
      if (driver == null || !URL_LOGIN_PAGE.equals(driver.getCurrentUrl())) {
        throw new Exception();
      }
      driver.findElement(By.id("sms_pwd_l")).clear();
      driver.findElement(By.id("sms_pwd_l")).sendKeys(smsCode);
      driver.findElement(By.id("submit_bt")).click();
      Thread.sleep(1000 * 3);
      if (URL_LOGIN_PAGE.equals(driver.getCurrentUrl())) {
        return new Result(Constants.SYSTEMERROR, "短信随机码不正确或已过期");
      }
      return new Result(Constants.SUCCESS, Constants.getMessage(Constants.SUCCESS));
    } catch (Exception e) {
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }


  /**
   * 身份验证  短信验证码发送
   */
  public Result getVecSMSPwd(String sessionId) {
    try {
      ChromeDriver driver = getDriver(sessionId);

      WebDriverWait wait = new WebDriverWait(driver, 10);
      //点击发送短信验证码
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.xpath("//span[contains(@id,'send-sms')]")))
          .click();
      //关闭弹出框
      Thread.sleep(1000 * 5);
      try {
        driver.switchTo().alert().accept();
      } catch (Exception ep) {
      }

      // 获取验证码图片
      String imgUrl = getQRCode(driver,
          driver.findElement(By.xpath("//img[contains(@id,'imageVec')]")));

      return new Result(Constants.SYSTEMERROR,
          "需要身份验证,注意查收手机验证码; 图片验证码URL: " + SystemConfig.localAddress + imgUrl);
    } catch (Exception e) {
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }


  public Result billQry(String sessionId) {
    try {
      ChromeDriver driver = getDriver(sessionId);
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
      driver.get(BILLQRY_URL);

      //点击查询账单按钮
      driver.findElement(By.id("jump_detail")).click();

      WebDriverWait wait = new WebDriverWait(driver, 10);
      //点击通话详单
      wait.until(ExpectedConditions
          .presenceOfElementLocated(By.xpath("//li[@eventcode='UCenter_billdetailqry_type_THXD']")))
          .click();
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("month1"))).click();

      //判断是否需要身份验证
      boolean imgEle = DriverUtils.isElementExist(driver, By.id("imageVec"));

      File screenshotLocation = null;
      if (imgEle) {
        //验证码图片
        WebElement imageEle = wait
            .until(ExpectedConditions.visibilityOfElementLocated(By.id("imageVec")));
        //截取具体验证码区域
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        BufferedImage fullImg = ImageIO.read(screenshot);
        Point point = imageEle.getLocation();

        // Get width and height of the element
        int eleWidth = imageEle.getSize().getWidth();
        int eleHeight = imageEle.getSize().getHeight();

        BufferedImage eleScreenshot = fullImg.getSubimage(point.getX(), point.getY(),
            eleWidth, eleHeight);
        ImageIO.write(eleScreenshot, "png", screenshot);
        // Copy the element screenshot to disk
        screenshotLocation = new File("/Users/xiaoq/Desktop/" + UUID.randomUUID() + ".png");
        FileUtils.copyFile(screenshot, screenshotLocation);
        putDriver(sessionId, driver);
      }
      return new Result(Constants.SUCCESS, screenshotLocation);
    } catch (Exception e) {
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }


  /**
   * 查询详单时身份验证
   *
   * @param servPwd 服务密码
   * @param smsPwd 随机密码
   * @param imgCode 验证码
   */
  public Result auth(String sessionId, String servPwd, String smsPwd, String imgCode) {
    try {
      ChromeDriver driver = getDriver(sessionId);
      WebElement servpasswd = driver.findElement(By.id("vec_servpasswd"));
      servpasswd.clear();
      servpasswd.sendKeys(servPwd);
      Thread.sleep(1000);
      WebElement smspasswd = driver.findElement(By.id("vec_smspasswd"));
      smspasswd.clear();
      smspasswd.sendKeys(smsPwd);
      Thread.sleep(1000);
      WebElement imgcode = driver.findElement(By.id("vec_imgcode"));
      imgcode.clear();
      imgcode.sendKeys(imgCode);
      Thread.sleep(1000);
      driver.findElement(By.id("vecbtn")).click();

      String msg = "success";

      boolean tipEle = DriverUtils.isElementExist(driver, By.id("tip"));
      if (tipEle) {
        //验证信息
        String tip = driver.findElement(By.id("tip")).getText();
        String detailerrmsg = driver.findElement(By.id("detailerrmsg")).getText();

        if (StringUtils.isNotEmpty(tip)) {
          msg = tip;
        } else if (StringUtils.isNotEmpty(detailerrmsg)) {
          msg = detailerrmsg;
        }
      }
      return new Result(Constants.SUCCESS, msg);
    } catch (Exception e) {
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }

  //------------------------------------------数据采集-------------------------------------------

  /**
   * 个人信息
   */
  public Result custinfoQry(String sessionId) {

    String nm = getPhone(sessionId);//获取手机号
    try {

      logger.warn("开始采集个人信息：" + nm);
      ChromeDriver driver = getDriver(sessionId);
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
      driver.get(BILLQRY_URL);

      WebDriverWait wait = new WebDriverWait(driver, 10);
      wait.until(ExpectedConditions.presenceOfElementLocated(By.id("jump_detail")));
      wait.until(ExpectedConditions.presenceOfElementLocated(By.id("js_expense")));

      Thread.sleep(2000);

      Document expense_doc = Jsoup.parse(driver.getPageSource());
      Element expense = expense_doc.getElementById("js_expense");

      //本机号码当前可用余额
      String availableBalance = expense
          .select("div[class=expense1 fr] > div > p.mgt-15 > span[class=color-1 balance]").text();

      //点击我的信息
      wait.until(ExpectedConditions.elementToBeClickable(By.linkText("我的信息"))).click();
      //点击个人信息菜单
      wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@alis='custinfoqry']")))
          .click();
      wait.until(ExpectedConditions.presenceOfElementLocated(By.id("image-alert")));

      String name = "";//机主姓名
      String openTime = "";//入网时间
      String state = "0"; //本机号码状态  1 正常   0 异常
      String packageName = ""; //本机号码当前套餐名称

      String level = ""; //用户星级
      String province = ""; //号码归属地
      String isCert = "0"; //本机实名状态 1 已审核  0 未审核
      String email = ""; //机主电子邮箱
      String address = ""; //地址

      Thread.sleep(3000);
      Document doc = Jsoup.parse(driver.getPageSource());
      Element table = doc.getElementById("table_net");
      Elements trs = table.select("tbody > tr");

      if (trs != null && trs.size() > 0) {
        Elements tds0 = trs.get(0).select("td");
        name = tds0.get(1).text();

        String stateMsg = tds0.get(3).text();
        if (stateMsg.contains("正常")) {
          state = "1";
        }

        Elements tds1 = trs.get(1).select("td");
        String brand = tds1.get(3).text(); //所属品牌

        Elements tds2 = trs.get(2).select("td");
        String pname = tds2.get(1).text(); //当前套餐
        packageName = brand + pname;
        openTime = tds2.get(3).text();

        Elements tds3 = trs.get(3).select("td");
        String cert = tds3.get(3).text();
        if (cert.contains("已审核")) {
          isCert = "1";
        }

        Elements tds4 = trs.get(4).select("td");
        level = tds4.get(1).select("div").text();
      }

      Element tableUserInfo = doc.getElementById("table_userInfo");
      Elements infoTrs = tableUserInfo.select("tbody > tr");
      if (infoTrs != null && infoTrs.size() > 0) {
        email = infoTrs.get(1).select("td.tl > span").text();
        address = infoTrs.get(3).select("td.tl > span").text();
      }

      province = doc.getElementById("dropdownMenu4").text();

      CustomerInfo customerInfo = new CustomerInfo();
      customerInfo.setId(MD5.encode(nm));
      customerInfo.setMobile(nm);
      customerInfo.setName(name);
      customerInfo.setOpenTime(openTime);
      customerInfo.setPackageName(packageName);
      customerInfo.setAvailableBalance(availableBalance);
      customerInfo.setLevel(level);
      customerInfo.setProvince(province);
      customerInfo.setIsCert(isCert);
      customerInfo.setState(state);
      customerInfo.setEmail(email);
      customerInfo.setAddress(address);
      customerInfo.setCreateTime(new Date());
      customerInfoDao.insert(customerInfo);

      return new Result(Constants.SUCCESS, JSON.toJSON(customerInfo));
    } catch (Exception e) {
      e.printStackTrace();
      logger.warn("采集个人信息失败 {}, {}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }

  /**
   * 账单信息
   */
  public Result billinfoQry(String sessionId) {

    String nm = getPhone(sessionId);//获取手机号
    try {

      logger.warn("开始采集账单信息：" + nm);
      ChromeDriver driver = getDriver(sessionId);
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
      driver.get(BILLQRY_URL);
      Thread.sleep(1000 * 6);
      WebDriverWait wait = new WebDriverWait(driver, 10);
      List<BillInfo> list = new ArrayList<>();
      for (int i = 1; i <= 13; i++) {

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("month-tab"))).click();
        Thread.sleep(1000);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("month" + i))).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("curmontconsum")));

        //账单月
        String billMonth = driver.findElement(By.id("month-tab")).getText();
        String year = billMonth.split("-")[0];

        String baseFee = "";//套餐固定费用
        String billStartDate = ""; //起始时间
        String billEndDate = ""; //结束时间
        String voiceFee = ""; //语音费
        String webFee = ""; //网络流量费
        String smsFee = ""; //短彩信费
        String extraFee = ""; //其他费用
        String extraServiceFee = ""; //增值业务费
        String discount = ""; //优惠费
        String totalFee = "";//总费用
        String point = ""; //积分

        Thread.sleep(2000);
        Document doc = Jsoup.parse(driver.getPageSource());

        String currMonth = doc.getElementById("curr-month").text();
        String[] ss = currMonth.split("-");
        billStartDate = year + "-" + ss[0].replace("月", "-").replace("日", "");
        billEndDate = year + "-" + ss[1].replace("月", "-").replace("日", "");

        if (baseFee.equals("0.00")) {
          continue;
        }

        Elements divs = doc.getElementById("curmontconsum").select("div.ued-daCon1");
        if (divs != null && divs.size() > 0) {
          baseFee = divs.get(0).selectFirst("p > span.fr").text();
          //如果固定费用为0 说明当前月份没有账单
          voiceFee = divs.get(1).selectFirst("p > span.fr").text();
          webFee = divs.get(2).selectFirst("p > span.fr").text();
          smsFee = divs.get(3).selectFirst("p > span.fr").text();
          extraServiceFee = divs.get(4).selectFirst("p > span.fr").text();
          extraFee = divs.get(6).selectFirst("p > span.fr").text();
        }

        Element othercounsum = doc.getElementById("othercounsum");
        discount = othercounsum.select("div > p > span").text();

        Element js_expense = doc.getElementById("js_expense");
        totalFee = js_expense
            .select("div[class=expense1 fr] > div[class=expense-box] > p[class=curmonth] >span")
            .text();
        point = js_expense.select(
            "div[class=expense1 fr] > div[class=expense-box] > p[class=mgt-15 mgb-30] >span")
            .text();

        BillInfo billInfo = new BillInfo();
        billInfo.setId(MD5.encode(nm + billMonth));
        billInfo.setCid(MD5.encode(nm));
        billInfo.setBaseFee(baseFee);
        billInfo.setBillMonth(billMonth);
        billInfo.setBillStartDate(billStartDate);
        billInfo.setBillEndDate(billEndDate);
        billInfo.setVoiceFee(voiceFee);
        billInfo.setWebFee(webFee);
        billInfo.setSmsFee(smsFee);
        billInfo.setExtraFee(extraFee);
        billInfo.setExtraServiceFee(extraServiceFee);
        billInfo.setDiscount(discount);
        billInfo.setTotalFee(totalFee);
        billInfo.setPoint(point);
        billInfo.setCreateTime(new Date());

        if (i == 1) {
          billInfoDao.insert(billInfo);
        } else {
          list.add(billInfo);
        }
        logger.warn("采集{} {}账单信息", nm, billMonth);
      }
      if (list.size() > 0) {
        billInfoDao.insertBatch(list);
      }
      return new Result(Constants.SUCCESS, JSON.toJSON(list));

    } catch (Exception e) {
      e.printStackTrace();
      logger.warn("采集账单信息失败 {}, {}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }


  /**
   * 通话语音信息
   */
  public Result callinfoQry(String sessionId) {
    ChromeDriver driver = getDriver(sessionId);
    String nm = getPhone(sessionId);//获取手机号
    try {
      List<CallInfo> list = new ArrayList<>();
      driver.get(BILLDETAILQRY_URL);
      Thread.sleep(1000 * 2);
      WebDriverWait wait = new WebDriverWait(driver, 10);
      //点击查询账单按钮
//      wait.until(ExpectedConditions.presenceOfElementLocated(By.id("jump_detail"))).click();
      //点击通话详单
      wait.until(ExpectedConditions
          .presenceOfElementLocated(By.xpath("//li[@eventcode='UCenter_billdetailqry_type_THXD']")))
          .click();
      Thread.sleep(1000 * 2);
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("month1"))).click();
      Thread.sleep(1000 * 2);
      if (DriverUtils.isElementExist(driver, By.id("vec_imgcode"))) {
        return getVecSMSPwd(sessionId);
      }

      for (int i = 1; i <= 7; i++) {

        String month = driver.findElement(By.id("month" + i)).getText();
        String year = month.substring(0, 4);

        logger.warn("移动通话详单数据:本机：{}  通话月份：{}", nm, month);
        if (i > 1) {
          driver.findElement(By.id("month" + i)).click();
          Thread.sleep(1000 * 3);
        }

        while (true) {
          Document doc = Jsoup.parse(driver.getPageSource());
          Element tbody = doc.getElementById("tbody");
          if (tbody != null) {
            Elements trs = tbody.select("tr");
            if (trs != null && trs.size() > 0) {
              for (Element tr : trs) {
                Elements tds = tr.select("td");

                String dtype = tds.get(2).text();
                //呼叫类型. DIAL-主叫; DIALED-被叫
                String dialType = "DIALED";
                if (dtype.contains("主叫")) {
                  dialType = "DIAL";
                }

                String stime = year + "-" + tds.get(0).text();//起始时间
                String id = MD5.encode(nm + stime);
                CallInfo callInfo = new CallInfo();
                callInfo.setId(id);
                callInfo.setCid(MD5.encode(nm));
                callInfo.setTime(stime);//起始时间
                callInfo.setLocation(tds.get(1).text());//通信地点
                callInfo.setDialType(dialType);//通信方式
                callInfo.setPeerNumber(tds.get(3).text());//对方号码
                callInfo.setDuration(tds.get(4).text());//通信时长
                callInfo.setLocationType(tds.get(5).text());//通信类型
                callInfo.setFee(tds.get(7).text());//通信费
                callInfo.setCreateTime(new Date());
                list.add(callInfo);
              }
            } else {
              break;
            }

            //是否有下一页 是否可以点击  TODO
//            try {
//              if (StringUtils
//                  .isEmpty(driver.findElementByXPath("//a[@class='next']").getAttribute("style"))) {
//                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@class='next']")))
//                    .click();
//                Thread.sleep(1000 * 3);
//              }
//            } catch (Exception e) {
              break;
//            }
          } else {
            break;
          }
        }
      }
      if (list.size() > 0) {
        //数据库插入
        int num = callInfoDao.insertBatch(list);
        logger.warn("成功插入{} 移动通话语音详单数据 {}条", nm, num);
      }

      return new Result(Constants.SUCCESS, JSON.toJSON(list));
    } catch (Exception e) {
      e.printStackTrace();
      logger.warn("采集移动通话语音详单数据发生异常 {},{}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }


  /**
   * 短信\彩信信息
   */
  public Result smsinfoQry(String sessionId) {
    ChromeDriver driver = getDriver(sessionId);
    String nm = getPhone(sessionId);//获取手机号
    try {
      List<SmsInfo> list = new ArrayList<>();
      driver.get(BILLQRY_URL);

      WebDriverWait wait = new WebDriverWait(driver, 10);
      //点击查询账单按钮
      wait.until(ExpectedConditions.presenceOfElementLocated(By.id("jump_detail"))).click();
      wait.until(ExpectedConditions
          .presenceOfElementLocated(By.xpath("//li[@eventcode='UCenter_billdetailqry_type_DCXD']")))
          .click();
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("month1"))).click();

      if (DriverUtils.isElementExist(driver, By.id("vec_imgcode"))) {
        return getVecSMSPwd(sessionId);
      }

      for (int i = 1; i <= 7; i++) {

        String month = driver.findElement(By.id("month" + i)).getText();
        String year = month.substring(0, 4);

        logger.warn("移动短信/彩信详单数据:本机：{}  通话月份：{}", nm, month);
        if (i > 1) {
          driver.findElement(By.id("month" + i)).click();
          Thread.sleep(2000);
        }
        Document doc = Jsoup.parse(driver.getPageSource());
        Element tbody = doc.getElementById("tbody");
        if (tbody != null) {
          Elements trs = tbody.select("tr");
          if (trs != null && trs.size() > 0) {
            for (Element tr : trs) {
              Elements tds = tr.select("td");

              //通信方式：SEND-发送; RECEIVE-收取
              String stype = tds.get(3).text();
              String sendType = "SEND";
              if (stype.contains("收取")) {
                sendType = "RECEIVE";
              }

              //信息类型 ：SMS-短信; MMS-彩信
              String mtype = tds.get(4).text();
              String msgType = "MMS";
              if (mtype.contains("短信")) {
                msgType = "SMS";
              }

              String stime = year + "-" + tds.get(0).text();//起始时间
              String id = MD5.encode(nm + stime);

              SmsInfo smsInfo = new SmsInfo();
              smsInfo.setId(id);
              smsInfo.setCid(MD5.encode(nm));
              smsInfo.setPeerNumber(tds.get(2).text()); //对方号码
              smsInfo.setTime(stime); //收发短信时间
              smsInfo.setLocation(tds.get(1).text()); //通信地点
              smsInfo.setSendType(sendType);
              smsInfo.setMsgType(msgType);
              smsInfo.setServiceName(tds.get(5).text());
              smsInfo.setFee(tds.get(7).text());
              smsInfo.setCreateTime(new Date());

              list.add(smsInfo);
            }
          }

          //是否有下一页 是否可以点击
          ExpectedConditions.elementToBeClickable(By.xpath("//a[@class='next']"));
        }
      }
      if (list.size() > 0) {
        //数据库插入
        int num = smsInfoDao.insertBatch(list);
        logger.warn("成功插入{} 短信彩信信息数据 {}条 ", nm, num);
      }

      return new Result(Constants.SUCCESS, JSON.toJSON(list));
    } catch (Exception e) {
      e.printStackTrace();
      logger.warn("采集短信彩信数据发生异常 {},{}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }


  /**
   * 上网流量信息
   */
  public Result netinfoQry(String sessionId) {
    ChromeDriver driver = getDriver(sessionId);
    String nm = getPhone(sessionId);//获取手机号
    try {
      List<NetInfo> list = new ArrayList<>();
      driver.get(BILLQRY_URL);

      WebDriverWait wait = new WebDriverWait(driver, 10);
      //点击查询账单按钮
      wait.until(ExpectedConditions.presenceOfElementLocated(By.id("jump_detail"))).click();
      wait.until(ExpectedConditions
          .presenceOfElementLocated(By.xpath("//li[@eventcode='UCenter_billdetailqry_type_SWXD']")))
          .click();
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("month1"))).click();

      if (DriverUtils.isElementExist(driver, By.id("vec_imgcode"))) {
        return getVecSMSPwd(sessionId);
      }

      for (int i = 1; i <= 7; i++) {

        String month = driver.findElement(By.id("month" + i)).getText();
        String year = month.substring(0, 4);

        logger.warn("移动上网流量数据:本机：{}  通话月份：{}", nm, month);
        if (i > 1) {
          driver.findElement(By.id("month" + i)).click();
          Thread.sleep(2000);
        }
        Document doc = Jsoup.parse(driver.getPageSource());
        Element tbody = doc.getElementById("tbody");
        if (tbody != null) {
          Elements trs = tbody.select("tr");
          if (trs != null && trs.size() > 0) {
            for (Element tr : trs) {
              Elements tds = tr.select("td");

              String stime = year + "-" + tds.get(0).text();//起始时间
              String id = MD5.encode(nm + stime);

              NetInfo netInfo = new NetInfo();
              netInfo.setId(id);
              netInfo.setCid(MD5.encode(nm));
              netInfo.setTime(stime);
              netInfo.setDuration(tds.get(4).text());
              netInfo.setLocation(tds.get(1).text());
              netInfo.setSubflow(tds.get(5).text());
              netInfo.setNetType(tds.get(3).text());
              netInfo.setServiceName(tds.get(2).text());
              netInfo.setFee(tds.get(7).text());
              netInfo.setCreateTime(new Date());

              list.add(netInfo);
            }
          }

          //是否有下一页 是否可以点击
          ExpectedConditions.elementToBeClickable(By.xpath("//a[@class='next']"));
        }
      }
      if (list.size() > 0) {
        //数据库插入
        int num = netInfoDao.insertBatch(list);
        logger.warn("成功插入{} 上网流量数据 {}条 ", nm, num);
      }
      return new Result(Constants.SUCCESS, JSON.toJSON(list));
    } catch (Exception e) {
      e.printStackTrace();
      logger.warn("采集上网流量数据发生异常 {},{}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }

  /**
   * 充值记录信息
   */
  public Result rechargeQry(String sessionId) {
    ChromeDriver driver = getDriver(sessionId);
    String nm = getPhone(sessionId);//获取手机号
    try {
      List<RechargeInfo> list = new ArrayList<>();
      driver.get(BILLQRY_URL);
      logger.warn("开始采集充值记录信息：" + nm);
      WebDriverWait wait = new WebDriverWait(driver, 10);

      //点击缴费记录查询
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@alis='recharhisqry']")))
          .click();
      Thread.sleep(1000);
      wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//li[@i='2']"))).click();
      Thread.sleep(1000 * 6);
//      if (!DriverUtils.isElementExist(driver, By.id("payHis-data"))) {
//        return new Result(Constants.SUCCESS, "没有充值记录");
//      }

      String html = driver.getPageSource();
      Document doc = Jsoup.parse(html);
      Elements elesTr = doc.select("#payHis-data > tr");
      for (Element ele : elesTr) {
        Elements tds = ele.select("td");
        String stime = tds.get(0).text();
        if (stime.equals("您选择的时间段内没有交费记录哦") || stime.contains("请选择时间段查询您的交费记录")) {
          break;
        }
        String id = MD5.encode(nm + stime);
        RechargeInfo rechargeInfo = new RechargeInfo();
        rechargeInfo.setId(id);
        rechargeInfo.setCid(MD5.encode(nm));
        rechargeInfo.setRechargeTime(stime);
        rechargeInfo.setAmount(tds.get(1).text());
        rechargeInfo.setType(tds.get(3).text());
        rechargeInfo.setCreateTime(new Date());

        list.add(rechargeInfo);
      }

      if (list.size() > 0) {
        //数据库插入
        int num = rechargeInfoDao.insertBatch(list);
        logger.warn("成功插入{} 充值记录数据 {}条 ", nm, num);
      }

      return new Result(Constants.SUCCESS, JSON.toJSON(list));
    } catch (Exception e) {
      e.printStackTrace();
      logger.warn("采集充值记录数据发生异常 {},{}", nm, e.toString());
      return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
    }
  }

  /**
   * 套餐信息
   */
  public Result packageQry(String sessionId) {
    ChromeDriver driver = getDriver(sessionId);
    String nm = getPhone(sessionId);//获取手机号
    try {
      List<PackageInfo> list = new ArrayList<>();
      driver.get(BILLQRY_URL);
      logger.warn("开始采集套餐信息息：" + nm);
      WebDriverWait wait = new WebDriverWait(driver, 10);

      //点击缴费记录查询
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@alis='packremainqry']")))
          .click();
      Thread.sleep(1000 * 6);
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("mealInfo")));

      String html = driver.getPageSource();
      Document doc = Jsoup.parse(html);
      Elements elesTr = doc.select("#mealInfo tbody tr");

      if (elesTr.size() > 0) {
        String nowMonth = JodatimeUtils.now(JodatimeUtils.FORMAT_YYYYMM_01);
        String id = MD5.encode(nm + nowMonth);

        PackageInfo packageInfo = new PackageInfo();
        packageInfo.setId(id);
        packageInfo.setCid(MD5.encode(nm));
        packageInfo.setItem(elesTr.get(0).selectFirst("td").text());
        packageInfo.setTotal(elesTr.get(1).select("td span").get(2).text());
        packageInfo.setUsed(elesTr.get(2).selectFirst("td p span").text());
        packageInfo.setUnit("MB");
        packageInfo.setCreateTime(new Date());

        try {
          packageInfoDao.insert(packageInfo);
          logger.warn("成功插入{} 上网流量数据 {}条 ", nm, 1);
        } catch (Exception ex) {
        }
      }
      return new Result(Constants.SUCCESS, "套餐信息采集完毕");
    } catch (Exception e) {
      e.printStackTrace();
      logger.warn("采集套餐信息数据发生异常 {},{}", nm, e.toString());
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
    return absolute.split("static")[1];
  }


}