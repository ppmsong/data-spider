package com.crw.contact.controller;


import com.crw.common.Result;
import com.crw.crawler.ChinaMobileRemoteExecute;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 中国移动接口
 */
@RestController
@RequestMapping("/mobile")
public class MobileController extends BaseController {


  @Autowired
  private ChinaMobileRemoteExecute mobileRemoteExecute;

  @ApiOperation("发送登陆短信验证码")
  @RequestMapping(value = "/sendSmsCode", method = RequestMethod.POST)
  public Result getSMSPwd(@RequestParam String phone) {
    Result rs = mobileRemoteExecute.getSMSPwd(getSessionid(), phone);
    return rs;
  }

  @ApiOperation("移动登陆")
  @RequestMapping(value = "/login", method = RequestMethod.POST)
  public Result login(@RequestParam String smsCode) {
    Result rs = mobileRemoteExecute.login(getSessionid(), smsCode);
    return rs;
  }

//  @ApiOperation("移动登陆页面")
//  @RequestMapping(value = "/loginForm", method = RequestMethod.GET)
//  public Result loginForm() {
//    Result rs = mobileRemoteExecute.loginForm();
//    return rs;
//  }

//  @ApiOperation("详单查询页面")
//  @RequestMapping(value = "/billqry", method = RequestMethod.GET)
//  public Result billqry() {
//    Result rs = mobileRemoteExecute.billQry(getSessionid());
//    return rs;
//  }


//  @ApiOperation("发送身份验证短信验证码")
//  @RequestMapping(value = "/vecSmspwd", method = RequestMethod.GET)
//  public Result getSMSPwd() {
//    Result rs = mobileRemoteExecute.getVecSMSPwd(getSessionid());
//    return rs;
//  }

  @ApiOperation("身份验证")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "servPwd", value = "服务密码", paramType = "query", required = true),
      @ApiImplicitParam(name = "smsPwd", value = "随机密码", paramType = "query", required = true),
      @ApiImplicitParam(name = "imgCode", value = "验证码", paramType = "query", required = true)})
  @RequestMapping(value = "/auth", method = RequestMethod.POST)
  public Result auth(@RequestParam String servPwd, @RequestParam String smsPwd,
      @RequestParam String imgCode) {
    Result rs = mobileRemoteExecute.auth(getSessionid(), servPwd, smsPwd, imgCode);
    return rs;
  }


  @ApiOperation("个人信息采集")
  @RequestMapping(value = "/custinfoQry", method = RequestMethod.GET)
  public Result custinfoQry() {
    Result rs = mobileRemoteExecute.custinfoQry(getSessionid());
    return rs;
  }

  @ApiOperation("账单信息采集")
  @RequestMapping(value = "/billinfoQry", method = RequestMethod.GET)
  public Result billinfoQry() {
    Result rs = mobileRemoteExecute.billinfoQry(getSessionid());
    return rs;
  }

  @ApiOperation("语音信息采集")
  @RequestMapping(value = "/callinfoQry", method = RequestMethod.GET)
  public Result callinfoQry() {
    Result rs = mobileRemoteExecute.callinfoQry(getSessionid());
    return rs;
  }

  @ApiOperation("短彩信信息采集")
  @RequestMapping(value = "/smsinfoQry", method = RequestMethod.GET)
  public Result smsinfoQry() {
    Result rs = mobileRemoteExecute.smsinfoQry(getSessionid());
    return rs;
  }

  @ApiOperation("上网流量信息采集")
  @RequestMapping(value = "/netinfoQry", method = RequestMethod.GET)
  public Result netinfoQry() {
    Result rs = mobileRemoteExecute.netinfoQry(getSessionid());
    return rs;
  }


  @ApiOperation("充值记录信息采集")
  @RequestMapping(value = "/rechargeQry", method = RequestMethod.GET)
  public Result rechargeQry() {
    Result rs = mobileRemoteExecute.rechargeQry(getSessionid());
    return rs;
  }

  @ApiOperation("套餐信息采集")
  @RequestMapping(value = "/packageQry", method = RequestMethod.GET)
  public Result packageQry() {
    Result rs = mobileRemoteExecute.packageQry(getSessionid());
    return rs;
  }


  @ApiOperation("关闭结束采集")
  @PostMapping(value = "quit")
  public Result quit() {
    Result rs = mobileRemoteExecute.quit(getSessionid());
    return rs;
  }


}
