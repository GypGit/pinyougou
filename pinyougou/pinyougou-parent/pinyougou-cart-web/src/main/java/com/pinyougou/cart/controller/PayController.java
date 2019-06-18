package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;

    @RequestMapping("/createNative")
    public Map createNative() {
        IdWorker idWorker = new IdWorker();
        long out_trade_no = idWorker.nextId();
        return weixinPayService.createNative(out_trade_no + "", "1");

    }
}
