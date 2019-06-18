package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //1.创建参数
        Map param = new HashMap();
        param.put("appid", appid);
        param.put("mch_id", partner);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        param.put("body", "品优购");
        param.put("out_trade_no", out_trade_no);
        param.put("total_fee", total_fee);
        param.put("spbill_create_ip", "127.0.0.1");
        param.put("notify_url", "www.baidu.com");
        param.put("trade_type", "NATIVE");

        //2.向微信发送请求
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(xmlParam);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            //3.获得结果
            String result = client.getContent();
            System.out.println(result);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            Map map = new HashMap();
            map.put("code_url", resultMap.get("code_url"));//二维码url
            map.put("total_fee", total_fee);//总金额
            map.put("out_trade_no", out_trade_no);//订单号
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        Map param = new HashMap();
        param.put("appid", appid);
        param.put("mch_id", partner);
        param.put("out_trade_no", out_trade_no);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        String url="https://api.mch.weixin.qq.com/pay/orderquery";
        
        return null;
    }
}
