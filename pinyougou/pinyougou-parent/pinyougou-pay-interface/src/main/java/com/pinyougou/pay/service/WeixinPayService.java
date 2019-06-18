package com.pinyougou.pay.service;

import java.util.Map;

public interface WeixinPayService {

    //生成二维码url
    public Map createNative(String out_trade_no,String total_fee);

    //根据订单号查询支付状态
    public Map queryPayStatus(String out_trade_no);
}
