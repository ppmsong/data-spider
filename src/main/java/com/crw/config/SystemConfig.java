package com.crw.config;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SystemConfig {

  @Autowired
  private Environment environment;

  // 支付宝验证码图片路径
  public static String imagesPath;

  // 移动运营商验证码图片路径
  public static String imagesPathMobile;

  public static String localAddress;

  //代理ip和端口 例127.0.0.1:8888
  public static String proxyIpAndPort;

  @Value("${dataSpider.proxyIpAndPort}")
  public void setProxyIpAndPort(String proxyIpAndPort) {
    SystemConfig.proxyIpAndPort = proxyIpAndPort;
  }

  @Value("${dataSpider.mobile.imagesPath}")
  public void setImagesPathMobile(String imagesPath) {
    SystemConfig.imagesPathMobile = imagesPath;
  }

  @Value("${dataSpider.alipay.imagesPath}")
  public void setImagesPath(String imagesPath) {
    SystemConfig.imagesPath = imagesPath;
  }

  @Value("${dataSpider.domain}")
  public void setLocalAddress(String domain) throws UnknownHostException {
    if (StringUtils.isEmpty(domain)) {
      SystemConfig.localAddress = Inet4Address.getLocalHost().getHostAddress()
          + ":" + environment.getProperty("server.port");
    }else{
      SystemConfig.localAddress = domain
              + ":" + environment.getProperty("server.port");
    }
  }


}
