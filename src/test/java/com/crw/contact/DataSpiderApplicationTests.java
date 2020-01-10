package com.crw.contact;

import com.crw.contact.entity.CommDetail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@PropertySource({"classpath:application.yml"})
public class DataSpiderApplicationTests {



    @Test
    public void contextLoads() throws Exception {

        CommDetail a = new CommDetail();
        a.setId("11111");
        a.setNm("2222");
        a.setCommfee("were");
        a.setCommtype("rewr");

        List<CommDetail> b = new ArrayList<>();
        b.add(a);

        /*String sid = "0277ded1-a5fa-40f6-a396-9c192049051c";
        String url = "http://www.baidu.com";

        //System.setProperty("webdriver.chrome.driver", "/Users/xiaoq/Desktop/workspace/chromedriver");


        ChromeOptions options = new ChromeOptions();
        //options.addArguments("headless");//无界面参数
       // options.addArguments("no-sandbox");//禁用沙盒 就是被这个参数搞了一天

        WebDriver driver = new ChromeDriver(options);
        //WebDriver driver = new ChromeDriver();

        driver.get("http://www.baidu.com/");
        Thread.sleep(5000);  // Let the user actually see something!
        WebElement searchBox = driver.findElement(By.name("q"));
        searchBox.sendKeys("baidu");
        searchBox.submit();
        Thread.sleep(5000);  // Let the user actually see something!
        driver.quit();*/
    }

}
