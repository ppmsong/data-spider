package com.crw.crawler;

import com.alibaba.fastjson.JSONObject;
import com.crw.common.BaseRemoteExecute;
import com.crw.common.Constants;
import com.crw.common.Result;
import com.crw.config.SystemConfig;
import com.crw.contact.dao.AliBillingRecordDao;
import com.crw.contact.dao.AliTotalssetsDao;
import com.crw.contact.dao.AliUserInfoDao;
import com.crw.contact.dao.AlitransactionDetailDao;
import com.crw.contact.entity.AliBillingRecord;
import com.crw.contact.entity.AliTotalssets;
import com.crw.contact.entity.AliUserInfo;
import com.crw.contact.entity.AlitransactionDetail;
import com.crw.exception.AliVerifyException;
import com.crw.util.DriverUtils;
import com.crw.util.MD5;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlipayRemoteExecute extends BaseRemoteExecute {

  private static final String URL_LOGIN_PAGE = "https://auth.alipay.com/login/homeB.htm?redirectType=parent";
  private static final String URL_TOTALSSETS_PAGE = "https://my.alipay.com/portal/i.htm";
  private static final String URL_BILLING_RECORD_PAGE = "https://consumeprod.alipay.com/record/standard.htm";
  private static final String URL_TAOBAO_ADDRESS_PAGE = "https://member1.taobao.com/member/fresh/deliver_address.htm";
  private static final String URL_TRANSACTION_PAGE = "https://buyertrade.taobao.com/trade/itemlist/list_bought_items.htm";

  private static final int uc_login = 1;


  @Autowired
  private AliTotalssetsDao aliTotalssetsDao;

  @Autowired
  private AliUserInfoDao aliUserInfoDao;

  @Autowired
  private AliBillingRecordDao aliBillingRecordDao;

  @Autowired
  private AlitransactionDetailDao alitransactionDetailDao;

  public Result login() {
    try {

    } catch (Exception e) {

    }
    return new Result(Constants.SUCCESS, "");
  }

  public Result getLoginCheckcode(String sessionid, String username, String password) {
    ChromeDriver driver = getAlipayDriver(sessionid);
    String qrPath;
    try {
      if (driver != null) {

      } else {
        ChromeOptions option = new ChromeOptions();
        option.setHeadless(true);
        option.addArguments("--no-sandbox"); // 禁止沙箱模式
        driver = new ChromeDriver(option);
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
      }
      driver.get(URL_LOGIN_PAGE);
      driver.findElementById("J-qrcode-target").click();
      Thread.sleep(1000 * 1);
      driver.findElementById("J-input-user").sendKeys(username);
      driver.findElementById("password_rsainput").sendKeys(password);
      qrPath = getCheckcode(driver);
      putAlipayDriver(sessionid, driver);
    } catch (Exception e) {
      driver.quit();
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, "没获取到登陆验证码");
    }
    return new Result(Constants.SUCCESS, qrPath);
  }

  private String getCheckcode(ChromeDriver driver) throws Exception {
    String filePath;
    WebElement ele = driver.findElementById("J-checkcode-img");
    File file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
    BufferedImage fullImg = ImageIO.read(file);

    Point point = ele.getLocation();
    // Get width and height of the element
    int eleWidth = ele.getSize().getWidth();
    int eleHeight = ele.getSize().getHeight();

    BufferedImage eleScreenshot = fullImg.getSubimage(point.getX(), point.getY(),
        eleWidth, eleHeight);
    ImageIO.write(eleScreenshot, "png", file);
    String fileName = MD5.encodeLowerCase(driver.getSessionId().toString() + "_checkcode") + ".png";
    filePath = SystemConfig.imagesPath + fileName;
    String absolutePath = this.getClass().getResource("/static").getPath() + filePath;
    log.info("absolutePath={}", absolutePath);
    File screenshotLocation = new File(absolutePath);
    FileUtils.copyFile(file, screenshotLocation);
    return SystemConfig.localAddress + filePath;
  }

  // ------------ 黄金分割 - 扫码 ------------ //

  public Result getLoginQRCode(String sessionid) {
    ChromeDriver driver = getAlipayDriver(sessionid);
    String qrPath;
    JSONObject data = new JSONObject();
    try {
      if (driver != null) {
        // 用户之前打开过浏览器
        if (URL_LOGIN_PAGE == driver.getCurrentUrl()) {
          // 如果是登陆页面则点击发送验证码
          qrPath = getQRCode(driver, driver.findElementByClassName("barcode"));
          putAlipayDriver(sessionid, driver);
          data.put("qrPath", qrPath);
          data.put("urlCode", "1");
          return new Result(Constants.SUCCESS, data);
        } else {
          throw new Exception();
        }
      } else {
        ChromeOptions option = new ChromeOptions();
        option.setHeadless(true);
        option.addArguments("--no-sandbox"); // 禁止沙箱模式
        driver = new ChromeDriver(option);
      }
      driver.get(URL_LOGIN_PAGE);
      qrPath = getQRCode(driver, driver.findElementByClassName("barcode"));
      putAlipayDriver(sessionid, driver);
      data.put("qrPath", qrPath);
      data.put("urlCode", "1");
    } catch (Exception e) {
      driver.quit();
      delDriver(sessionid);
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, "获取到登陆二维码异常");
    }
    return new Result(Constants.SUCCESS, data);
  }


  public Result crawlerData(String sessionid, Integer urlCode) {
    ChromeDriver driver = getAlipayDriver(sessionid);
    try {
      if (driver == null) {
        throw new Exception("登陆信息已过期，请重新登陆");
      } else {
        // 登陆
        urlCode = login(driver, urlCode);
        // 账户金额信息
        urlCode = crawlerTotalssets(driver, sessionid, urlCode);
        // 账单记录
        urlCode = crawlerBillingRecord(driver, sessionid, urlCode);
        // 淘宝订单的收货地址
        urlCode = crawlerTaobaoAddress(driver, sessionid, urlCode);
        driver.quit();
        delDriver(sessionid);
      }
    } catch (AliVerifyException ve) {
      ve.printStackTrace();
      return new Result(Constants.SYSTEMERROR, ve.getAliVerify());
    } catch (Exception e) {
      driver.quit();
      delDriver(sessionid);
      e.printStackTrace();
      return new Result(Constants.SYSTEMERROR, e.getMessage());
    }
    return new Result(Constants.SUCCESS, "数据采集完毕");
  }

  private Integer login(ChromeDriver driver, Integer urlCode) throws Exception {
    if (!urlCode.equals(1)) {
      return urlCode;
    }
    if (DriverUtils.isElementByClassExist(driver, "barcode")) {
      String qrCode = getQRCode(driver, driver.findElementByClassName("barcode"));
      throw new AliVerifyException(urlCode, "需要扫描二维码验证", qrCode);
    }
    return ++urlCode;
  }

  private Integer crawlerTotalssets(ChromeDriver driver, String sessionid, Integer urlCode)
      throws Exception {
    if (!urlCode.equals(2)) {
      return urlCode;
    }
    WebDriverWait wait = new WebDriverWait(driver, 10);
    try {
      if (wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("showAccountAmount")))
          .getAttribute("class")
          .contains("hide-amount")) {
        driver.findElement(By.id("showAccountAmount")).findElement(By.className("show-text"))
            .click();
        Thread.sleep(1000);
      }
      if (wait.until(ExpectedConditions.presenceOfElementLocated(By.id("showYuebaoAmount")))
          .getAttribute("class").contains("hide-amount")) {
        driver.findElement(By.id("showYuebaoAmount")).findElement(By.className("show-text"))
            .click();
        Thread.sleep(1000);
      }
      if (wait.until(ExpectedConditions.presenceOfElementLocated(By.id("showHuabeiAmount")))
          .getAttribute("class").contains("hide-amount")) {
        driver.findElement(By.id("showHuabeiAmount")).findElement(By.className("show-text"))
            .click();
        Thread.sleep(1000);
      }
    } catch (Exception e) {
      if (DriverUtils.isElementByIdExist(driver, "risk_qrcode_cnt")) {
        String qrCode = getQRCode(driver, driver.findElementById("risk_qrcode_cnt"));
        throw new AliVerifyException(urlCode, "需要扫描二维码验证", qrCode);
      }
    }
    String html = driver.getPageSource();
    Document doc = Jsoup.parse(html);
    Date now = new Date();
    // 获取账户名
    String accountName = doc.selectFirst("#J-userInfo-account-userEmail").text();

    putAlipayAccount(sessionid, accountName);

    AliUserInfo aliUserInfo = new AliUserInfo();
    aliUserInfo.setAccountName(accountName);
    aliUserInfo.setId(MD5.encode(aliUserInfo.getAccountName()));
    aliUserInfo.setCreateTime(now);

    try {
      aliUserInfoDao.insertSelective(aliUserInfo);
    } catch (Exception e) {
    }

    AliTotalssets aliTotalssets = new AliTotalssets();
    aliTotalssets.setId(aliUserInfo.getId());
    aliTotalssets.setBalance(doc.selectFirst("#account-amount-container").text());
    aliTotalssets.setYuebaoAmt(doc.selectFirst("#J-assets-mfund-amount").text());
    aliTotalssets.setYuebaoTotalProfit(doc.selectFirst("#J-income-num").text());
    aliTotalssets.setHuabeiCanLimit(doc.selectFirst("#available-amount-container").text());
    aliTotalssets.setHuabeiLimit(doc.selectFirst("#credit-amount-container").text());
    aliTotalssets.setCreateTime(now);

    try {
      aliTotalssetsDao.insertSelective(aliTotalssets);
    } catch (Exception e) {
    }
    return ++urlCode;
  }

  private Integer crawlerBillingRecord(ChromeDriver driver, String sessionid, Integer urlCode)
      throws Exception {
    if (!urlCode.equals(3)) {
      return urlCode;
    }
    String accountName = getAlipayAccount(sessionid);

    for (int i = 0; i < 5; i++) {
      if (URL_BILLING_RECORD_PAGE.equals(driver.getCurrentUrl())) {
        break;
      }
      isRiskQrcodeCnt(driver, urlCode);
      driver.navigate().to(URL_BILLING_RECORD_PAGE);
    }

    boolean isNextPage = true;
    while (isNextPage) {
      if (!DriverUtils.isElementByIdExist(driver, "tradeRecordsIndex")) {
        return ++urlCode;
      }
      String html = driver.getPageSource();
      Document doc = Jsoup.parse(html);

      Elements eles = doc.select("#tradeRecordsIndex tbody tr");
      for (Element eleTr : eles) {
        Elements eleTd = eleTr.select("td");
        AliBillingRecord aliBillingRecord = new AliBillingRecord();
        aliBillingRecord.setTime(eleTd.get(1).text());
        aliBillingRecord.setName(eleTd.get(2).selectFirst(".consume-title").text());
        aliBillingRecord
            .setConsumptionName(eleTd.get(2).selectFirst(".name.p-inline.ft-gray").text());
        aliBillingRecord.setTradingNumber(eleTd.get(2).selectFirst("div a").attr("title"));
        aliBillingRecord.setAmount(eleTd.get(3).selectFirst(".amount-pay").text());
        aliBillingRecord.setTradingStatus(eleTd.get(5).selectFirst(".text-muted").text());
        aliBillingRecord.setCreateTime(new Date());
        aliBillingRecord.setCid(MD5.encode(accountName));
        aliBillingRecord.setId(MD5.encode(accountName + aliBillingRecord.getTime()));
        try {
          aliBillingRecordDao.insertSelective(aliBillingRecord);
        } catch (Exception e) {
        }
      }
      if (DriverUtils.isElementByClassExist(driver, "page-next page-trigger")) {
        driver.findElementByClassName("page-next page-trigger").click();
        Thread.sleep(1000 * 5);
        if (!URL_BILLING_RECORD_PAGE.equals(driver.getCurrentUrl())) {
          isRiskQrcodeCnt(driver, urlCode);
        }
      } else {
        isNextPage = false;
      }
    }
    return ++urlCode;
  }

  private Integer crawlerTaobaoAddress(ChromeDriver driver, String sessionid, Integer urlCode)
      throws Exception {
    if (!urlCode.equals(4)) {
      return urlCode;
    }
    String accountName = getAlipayAccount(sessionid);
    WebDriverWait wait = new WebDriverWait(driver, 10);

    driver.navigate().to(URL_TOTALSSETS_PAGE);

    wait.until(ExpectedConditions
        .presenceOfElementLocated(By.xpath("//a[@seed='global-account-member']"))).click();
    wait.until(
        ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@seed='last-blueLinkT4']")))
        .click();

    List<String> tabs = new ArrayList(driver.getWindowHandles());
    driver.switchTo().window(tabs.get(1));

    try {
      driver.switchTo().frame(0);
    } catch (Exception e) {

    }
    if (DriverUtils.isElementExist(driver, By.id("J_GetCode"))) {
      String phone = driver.findElementById("J_MobileVal").getAttribute("value");
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.id("J_GetCode"))).click();
      throw new AliVerifyException(4, "需要验证，已给手机号" + phone + "发送了验证码，注意查收。");
    }

    driver.switchTo().window(tabs.get(1));
    wait.until(
        ExpectedConditions.presenceOfElementLocated(By.id("J_SiteNavMytaobao"))).click();
    wait.until(
        ExpectedConditions.presenceOfElementLocated(By.id("bought"))).click();

    boolean isNext = true;
    int page = 1;
    while (isNext) {
      String html = driver.getPageSource();
      Document doc = Jsoup.parse(html);
      Elements elesTr = doc.select(".index-mod__order-container___1ur4-.js-order-container");
      for (int i = 0; i < elesTr.size(); i++) {
        log.info("page={},row_index={}", page, i);
        Element ele = elesTr.get(i);
        Elements eleTd = ele.select("tbody").get(1).select("tr td");

        if (eleTd.get(4).text().contains("虚拟物品") || eleTd.get(4).text().contains("自动充值")) {
          continue;
        }
        // 订单状态
        String orderStatus = eleTd.get(5).selectFirst("div p").text();
        if (!"交易成功".equals(orderStatus)) {
          continue;
        }
        if (eleTd.get(5).selectFirst("#viewDetail") == null) {
          continue;
        }
        // 订单日期
        String orderTime = ele.selectFirst(".bought-wrapper-mod__create-time___yNWVS").text();
        // 订单号
        String orderid = ele.selectFirst(".bought-wrapper-mod__head-info-cell___29cDO").text()
            .split("订单号:")[1].trim();
        // 店铺名称
        String storeName = ele.selectFirst(".seller-mod__name___37vL8") == null ? ""
            : ele.selectFirst(".seller-mod__name___37vL8").text();
        // 商品名称
        String productName = ele.selectFirst(".sol-mod__no-br___1PwLO p").text()
            .replace("[交易快照]", "");
        // 订单价格
        String orderPrice = ele.selectFirst(".price-mod__price___157jz").text();

        String href = eleTd.get(5).selectFirst("#viewDetail").attr("href");
        // 收货人
        String consignee = "";
        // 收货人 联系电话
        String consigneeTel = "";
        // 收货地址
        String shippingAddr = "";

        driver.switchTo().window(tabs.get(1));
        driver.getPageSource();

        try {
          wait.until(
              ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@href='" + href + "']")))
              .click();
        } catch (Exception ex) {
          continue;
        }

        Thread.sleep(1000);
        tabs = new ArrayList(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(2));
        String addrHtml = driver.getPageSource();
        Document docAddr = Jsoup.parse(addrHtml);

        if (docAddr.selectFirst(".address-memo-mod__an-box___y5ixN") != null) {
          log.info("1 {}", docAddr.selectFirst(".address-memo-mod__an-box___y5ixN dd").text());
          String address = docAddr.selectFirst(".address-memo-mod__an-box___y5ixN dd").text();
          consignee = address.split("，")[0];
          consigneeTel = address.split("，")[1];
          shippingAddr = address.split("，")[2];
        }
        if (StringUtils.isNotEmpty(docAddr.select(".simple-list.logistics-list tbody tr").text())) {
          log.info("2 {}", docAddr.select(".simple-list.logistics-list tbody tr").text());
          String address = docAddr.select(".simple-list.logistics-list tbody tr").get(2)
              .select("td")
              .get(1).text();
          consignee = address.split("，")[0];
          consigneeTel = address.split("，")[1];
          shippingAddr = address.split("，")[3];
        }
        if (docAddr.selectFirst(".trade-imfor-dd") != null) {
          log.info("3 {}", docAddr.selectFirst(".trade-imfor-dd").text());
          String address = docAddr.selectFirst(".trade-imfor-dd").text();
          consignee = address.split(",")[0];
          consigneeTel = address.split(",")[1];
          shippingAddr = address.split(",")[2];
        }
        if (StringUtils.isNotEmpty(consignee)) {
          AlitransactionDetail model = new AlitransactionDetail();
          model.setId(MD5.encode(accountName + orderid));
          model.setCid(MD5.encode(accountName));
          model.setConsigneeName(consignee);
          model.setConsigneeStreet(shippingAddr);
          model.setConsigneeTel(consigneeTel);
          model.setOrderid(orderid);
          model.setOrderPrice(orderPrice);
          model.setOrderTime(orderTime);
          model.setOrderStatus(orderStatus);
          model.setProductName(productName);
          model.setStoreName(storeName);
          model.setCreateTime(new Date());
          try {
            alitransactionDetailDao.insertSelective(model);
          } catch (Exception e) {
          }
        }
        driver.close();
      }
      driver.switchTo().window(tabs.get(1));

      if (DriverUtils.isElementExist(driver, By.className("pagination-next"))) {
        if (DriverUtils.isElementExist(driver, By.xpath(
            "//li[contains(@class,'pagination-next') and contains(@class,'pagination-disabled')]"))) {
          break;
        }
        driver.findElementByClassName("pagination-next").click();
        page++;
        boolean netError = true;
        for (int j = 0; j < 12; j++) {
          if ((j + 1) % 4 == 0) {
            driver.navigate().refresh();
            Thread.sleep(1000 * 5);
            wait.until(
                ExpectedConditions
                    .presenceOfElementLocated(By.xpath("//input[@type='text' and @value='1']")))
                .clear();
            wait.until(
                ExpectedConditions
                    .presenceOfElementLocated(By.xpath("//input[@type='text' and @value='1']")))
                .sendKeys(page + "");
            wait.until(
                ExpectedConditions
                    .presenceOfElementLocated(By.xpath("//span[@class='pagination-options-go']")))
                .click();
          }
          Thread.sleep(1000 * 5);
          String page_ = driver.findElement(By.xpath(
              "//li[contains(@class,'pagination-item') and contains(@class,'pagination-item-active')]"))
              .getAttribute("title");
          log.info("page={}, page_={}", page, page_);
          if (page_.equals(page + "")) {
            netError = false;
            break;
          }
        }
        if (netError) {
          throw new AliVerifyException(4, "网络错误，请重新采集数据");
        }
      } else {
        isNext = false;
      }
    }

    return ++urlCode;
  }

  private void isRiskQrcodeCnt(ChromeDriver driver, Integer urlCode) throws Exception {
    if (DriverUtils.isElementByIdExist(driver, "risk_qrcode_cnt")) {
      String qrCode = getQRCode(driver, driver.findElementById("risk_qrcode_cnt"));
      throw new AliVerifyException(urlCode, "需要扫描二维码验证", qrCode);
    }
  }

  private String getQRCode(ChromeDriver driver, WebElement ele) throws Exception {
    File file = ele.getScreenshotAs(OutputType.FILE);
    try {
      String fileName =
          MD5.encodeLowerCase(driver.getSessionId().toString()) + ".png";
      String folderPath = SystemConfig.imagesPath;
      File folder = new File(folderPath);
      if (!folder.exists()) {
        folder.mkdirs();
      }
      String absolute = folderPath + fileName;
      log.info("absolute={}", absolute);
      File screenshotLocation = new File(absolute);
      FileUtils.copyFile(file, screenshotLocation);

      return SystemConfig.localAddress + absolute.split("static")[1];
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

    String folderPath = SystemConfig.imagesPath;
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


  public static void main(String[] args) throws Exception {
    Vertx vertx = Vertx.vertx();
//    Future future = Future.future(as -> {
//      for (int i = 0; i < 2; i++) {
//        try {
//          Thread.sleep(1000);
//          System.out.println("i");
//        } catch (InterruptedException e) {
//          e.printStackTrace();
//        }
//      }
//    });
//    vertx.eventBus().request("news.uk.sport", "message", future);
//    vertx.eventBus().consumer("news.uk.sport", message -> {
//      System.out.println("I have received a message2: " + message.body());
//    });

    DeliveryOptions options = new DeliveryOptions();
    options.addHeader("some-header", "some-value");

    vertx.eventBus().consumer("news.uk.sport", message -> {
      System.out.println("1: " + message.body());
      message.reply("how interesting!");
    });
    MessageConsumer<String> consumer = vertx.eventBus().consumer("news.uk.sport");
    consumer.handler(message -> {
      try {
        System.out.println("2: " + message.body());
        message.reply("2 how interesting!");
      } catch (Exception e) {
      }
    });
    vertx.eventBus().request("news.uk.sport", "sdfsdfsdf", ar -> {
      if (ar.succeeded()) {
        System.out.println("Received reply: " + ar.result().body());
      }
    });
    vertx.eventBus().send("news.uk.sport", "sadfsdfsdf", options);
    vertx.eventBus().publish("news.uk.sport", "Yay! Someone kicked a ball");
    System.out.println("我最快");


  }


}
