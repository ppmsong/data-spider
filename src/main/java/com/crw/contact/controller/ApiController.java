package com.crw.contact.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.crw.common.Constants;
import com.crw.common.Result;
import com.crw.crawler.AlipayRemoteExecute;
import com.crw.util.FontUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("api")
public class ApiController extends BaseController {

  @Autowired
  UnicomController unicomController;
  @Autowired
  MobileController mobileController;
  @Autowired
  AlipayController alipayController;
  @Autowired
  RestTemplate restTemplate;


  @ApiOperation("查询手机号码归属地")
  @PostMapping("phonearea")
  public Object phonearea(String mobile) {
    return FontUtil.decodeUnicode(restTemplate.getForObject("https://cx.shouji.360.cn/phonearea.php?number="+mobile,String.class));
  }


  //移动
  //支付宝
  @ApiOperation("运营商入口")
  @PostMapping("collect")
  public Result collect(String type,int position) {
    if ("unicom".equals(type)){
      //联通
      return collectUnicomData(position);
    }
    if ("mobile".equals(type)){
      //移动
      return collectMobileData(position);
    }
    if ("alipay".equals(type)){
      //支付宝

    }
    return new Result(Constants.INPUTERROR,"请求无效");

  }


      /**
       * 联通api数据采集
       * @return
       */
      private Result collectUnicomData(int position){
        Result result = null;
        JSONObject apis = getUnicomApis();
        Iterator iter = apis.entrySet().iterator();
        while (iter.hasNext()){
          Map.Entry<String,Integer> entry = (Map.Entry)iter.next();
          System.out.println(entry.getKey()+"==="+entry.getValue());
          //从上次请求失败的接口开始
          if (entry.getValue()>=position){
            if ("billinfoQry".equals(entry.getKey())){
              result = unicomController.billinfoQry();
            }
            if ("callinfoQry".equals(entry.getKey())){
              result = unicomController.callinfoQry();
            }
            if ("custinfoQry".equals(entry.getKey())){
              result = unicomController.custinfoQry();
            }
            if ("netinfoQry".equals(entry.getKey())){
              result = unicomController.netinfoQry();
            }
            if ("packageQry".equals(entry.getKey())){
              result = unicomController.packageQry();
            }
            if ("rechargeQry".equals(entry.getKey())){
              result = unicomController.rechargeQry();
            }
            if ("smsinfoQry".equals(entry.getKey())){
              result = unicomController.smsinfoQry();
            }
            System.out.println("result:"+JSON.toJSONString(result));
            if (null!=result && 0==result.getCode()){

            }else{
              if (null==result)return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
              if (Constants.NEW_AUTH == result.getCode()){
                JSONObject data = JSONObject.parseObject(result.getData().toString());
                data.put("position",entry.getValue());
                result.setData(data);
              }
              break;

            }
          }
        }
        return result;
      }

    /**
     *  移动api数据采集
     * @param position
     * @return
     */
    private Result collectMobileData(int position){
      Result result = null;
      JSONObject apis = getMobileApis();
      Iterator iter = apis.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry<String, Integer> entry = (Map.Entry) iter.next();
        System.out.println(entry.getKey() + "===" + entry.getValue());
        //从上次请求失败的接口开始
        if (entry.getValue()>=position){
          if ("custinfoQry".equals(entry.getKey())){
            result = mobileController.custinfoQry();
          }
          if ("billinfoQry".equals(entry.getKey())){
            result = mobileController.billinfoQry();
          }
          if ("callinfoQry".equals(entry.getKey())){
            result = mobileController.callinfoQry();
          }
          if ("smsinfoQry".equals(entry.getKey())){
            result = mobileController.smsinfoQry();
          }
          if ("netinfoQry".equals(entry.getKey())){
            result = mobileController.netinfoQry();
          }
          if ("rechargeQry".equals(entry.getKey())){
            result = mobileController.rechargeQry();
          }
          if ("packageQry".equals(entry.getKey())){
            result = mobileController.packageQry();
          }
          System.out.println("result:"+JSON.toJSONString(result));
          if (null!=result && 0==result.getCode()){

          }else{
            if (null==result)return new Result(Constants.SYSTEMERROR, Constants.getMessage(Constants.SYSTEMERROR));
            if (Constants.NEW_AUTH == result.getCode()){
              JSONObject data = JSONObject.parseObject(result.getData().toString());
              data.put("position",entry.getValue());
              result.setData(data);
            }
            break;

          }
        }
      }

      return result;
    }


      /**
       * 获取联通api数据采集接口
       * @return
       */
      private JSONObject getUnicomApis(){
            JSONObject apis = new JSONObject(new LinkedHashMap());
            apis.put("billinfoQry",1);
            apis.put("callinfoQry",2);
            apis.put("custinfoQry",3);
            apis.put("netinfoQry",4);
            apis.put("packageQry",5);
            apis.put("rechargeQry",6);
            apis.put("smsinfoQry",7);
            return apis;
      }

      /**
       * 获取移动api数据采集接口
       * @return
       */
      private JSONObject getMobileApis(){
        JSONObject apis = new JSONObject(new LinkedHashMap());
        apis.put("custinfoQry",1);
        apis.put("billinfoQry",2);
        apis.put("callinfoQry",3);
        apis.put("smsinfoQry",4);
        apis.put("netinfoQry",5);
        apis.put("rechargeQry",6);
        apis.put("packageQry",7);
        return apis;
      }

}
