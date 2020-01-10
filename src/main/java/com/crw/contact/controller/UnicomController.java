package com.crw.contact.controller;


import com.crw.common.Result;
import com.crw.crawler.ChinaUnicomRemoteExecute;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 中国联通接口
 */
@RestController
@RequestMapping("unicom")
public class UnicomController extends BaseController {

  @Autowired
  private ChinaUnicomRemoteExecute chinaUnicomRemoteExecute;

  @ApiOperation("联通获取登陆验证码")
  @PostMapping("sendCode")
  public Result sendCode(String phone) {
    Result result = chinaUnicomRemoteExecute.sendCode(getSessionid(), phone);
    return result;
  }

  @ApiOperation("联通服务密码登陆")
  @PostMapping("ckLogin")
  public Result ckLogin(String pwd, String code) {
    Result result = chinaUnicomRemoteExecute.ckLogin(getSessionid(), pwd, code, null);
    return result;
  }


  @ApiOperation("个人信息采集")
  @RequestMapping(value = "/custinfoQry", method = RequestMethod.GET)
  public Result custinfoQry() {
    Result rs = chinaUnicomRemoteExecute.custinfoQry(getSessionid());
    return rs;
  }

  @ApiOperation("账单信息采集")
  @RequestMapping(value = "/billinfoQry", method = RequestMethod.GET)
  public Result billinfoQry() {
    Result rs = chinaUnicomRemoteExecute.billinfoQry(getSessionid());
    return rs;
  }

  @ApiOperation("语音信息采集")
  @RequestMapping(value = "/callinfoQry", method = RequestMethod.GET)
  public Result callinfoQry() {
    Result rs = chinaUnicomRemoteExecute.callinfoQry(getSessionid());
    return rs;
  }

  @ApiOperation("短彩信信息采集")
  @RequestMapping(value = "/smsinfoQry", method = RequestMethod.GET)
  public Result smsinfoQry() {
    Result rs = chinaUnicomRemoteExecute.smsinfoQry(getSessionid());
    return rs;
  }

  @ApiOperation("上网流量信息采集")
  @RequestMapping(value = "/netinfoQry", method = RequestMethod.GET)
  public Result netinfoQry() {
    Result rs = chinaUnicomRemoteExecute.netinfoQry(getSessionid());
    return rs;
  }

  @ApiOperation("充值记录信息采集")
  @RequestMapping(value = "/rechargeQry", method = RequestMethod.GET)
  public Result rechargeQry() {
    Result rs = chinaUnicomRemoteExecute.rechargeQry(getSessionid());
    return rs;
  }

  @ApiOperation("套餐信息采集")
  @RequestMapping(value = "/packageQry", method = RequestMethod.GET)
  public Result packageQry() {
    Result rs = chinaUnicomRemoteExecute.packageQry(getSessionid());
    return rs;
  }

  @ApiOperation("短信身份认证")
  @PostMapping(value = "auth")
  public Result auth(String code, String urlCode) {
    Result rs = chinaUnicomRemoteExecute.auth(getSessionid(), code, urlCode);
    return rs;
  }

  @ApiOperation("关闭结束采集")
  @PostMapping(value = "quit")
  public Result quit() {
    Result rs = chinaUnicomRemoteExecute.quit(getSessionid());
    return rs;
  }

  //  @ApiOperation("联通输入查询验证码")
//  @GetMapping("queryCode")
  public Result queryCode(String code, String url) {
    Result result = chinaUnicomRemoteExecute.crawlerData(getSessionid(), code, url);
    return result;
  }


  //  @ApiOperation("联通输入身份证后6位号码")
//  @GetMapping("inputNum")
  public Result inputNum(String num) {
    Result result = chinaUnicomRemoteExecute.inputNum(getSessionid(), num);
    return result;
  }

  //  @ApiOperation("联通随机密码登陆")
//  @GetMapping("login")
  public Result login(String code) {
    Result result = chinaUnicomRemoteExecute.login(getSessionid(), code);
    return result;
  }

}
