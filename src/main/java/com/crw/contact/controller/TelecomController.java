package com.crw.contact.controller;


import com.crw.common.Result;
import com.crw.crawler.ChinaTelecomExecute;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 中国电信接口
 */
@RestController
@RequestMapping("/telecom")
public class TelecomController extends BaseController {

  @Autowired
  private ChinaTelecomExecute chinaTelecomExecute;


  @ApiOperation("获取登陆验证码")
  @GetMapping("getLoginCode")
  public Result getLoginCode(String phone, String pwd) {
    Result result = chinaTelecomExecute.getLoginCode(getSessionid(), phone, pwd);
    return result;
  }

  @ApiOperation("登陆")
  @GetMapping("login")
  public Result login(String code) {
    Result result = chinaTelecomExecute.login(getSessionid(), code);
    return result;
  }

  @ApiOperation("发送身份证/姓名/验证码验证")
  @RequestMapping(value = "/sendVCardidName", method = RequestMethod.GET)
  public Result sendVCardidName(String cardid, String name, String code) {
    Result rs = chinaTelecomExecute.sendVCardidName(getSessionid(), cardid, name, code);
    return rs;
  }

  public Result sendVSmsCode(String imgCode, String smsCode) {
    Result rs = chinaTelecomExecute.sendVSmsCode(getSessionid(), imgCode, smsCode);
    return rs;
  }

  @ApiOperation("个人信息采集")
  @RequestMapping(value = "/custinfoQry", method = RequestMethod.GET)
  public Result custinfoQry() {
    Result rs = chinaTelecomExecute.custinfoQry(getSessionid());
    return rs;
  }

  @ApiOperation("套餐信息采集")
  @RequestMapping(value = "/packageQry", method = RequestMethod.GET)
  public Result packageQry() {
    Result rs = chinaTelecomExecute.packageQry(getSessionid());
    return rs;
  }

  @ApiOperation("账单信息采集")
  @RequestMapping(value = "/billinfoQry", method = RequestMethod.GET)
  public Result billinfoQry() {
    Result rs = chinaTelecomExecute.billinfoQry(getSessionid());
    return rs;
  }

  @ApiOperation("语音信息采集")
  @RequestMapping(value = "/callinfoQry", method = RequestMethod.GET)
  public Result callinfoQry() {
    Result rs = chinaTelecomExecute.callinfoQry(getSessionid());
    return rs;
  }


}
