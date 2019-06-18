package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

@Component
public class itemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        System.out.println("监听到消息");
        try {//获取到消息,把消息转换为文本形式
            TextMessage textMessage = (TextMessage)message;
            String text = textMessage.getText();
            //将信息转换为对象
            List<TbItem> list = JSON.parseArray(text,TbItem.class);
            itemSearchService.importList(list);
            System.out.println("成功导入");
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
