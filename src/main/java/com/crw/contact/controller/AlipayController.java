package com.crw.contact.controller;

import com.crw.common.Result;
import com.crw.crawler.AlipayRemoteExecute;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("alipay")
public class AlipayController extends BaseController {

  @Autowired
  private AlipayRemoteExecute alipayRemoteExecute;

  //  @ApiOperation("获取账号密码登陆验证码")
//  @GetMapping("getCheckcode")
  public Result getCheckcode(String username, String password) {
    Result result = alipayRemoteExecute.getLoginCheckcode(getSessionid(), username, password);
    return result;
  }

  @ApiOperation("获取登陆二维码")
  @GetMapping("getLoginQRCode")
  public Result getLoginQRCode() {
    Result result = alipayRemoteExecute.getLoginQRCode(getSessionid());
    return result;
  }


  @ApiOperation("扫码确认")
  @GetMapping("scanCodeConfirm")
  public Result scanCodeConfirm(Integer urlCode) {
    Result result = alipayRemoteExecute.crawlerData(getSessionid(), urlCode);
    return result;
  }


}
