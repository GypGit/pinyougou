package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据skuId获取商品SKU信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")) {
            throw new RuntimeException("商品状态无效");
        }
        //2.获取商家ID
        String sellerId = item.getSellerId();
        //3.判断购物车列表中是否有本商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);
        if (cart == null) {//4.购物车列表中没有本商家购物车
            //4.1新建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            TbOrderItem orderItem = createOrderItem(item,num);
            List orderItemList=new ArrayList();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2将购物车对象添加到购物车列表中
            cartList.add(cart);

        } else {//5.购物车列表中有本商家购物车
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
            if(orderItem==null){
                //5.1. 如果没有，新增购物车明细
                orderItem=createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem);
            }else{
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue())  );
                //如果数量操作后小于等于0，则移除购物车明细
                if(orderItem.getNum()<=0){
                    cart.getOrderItemList().remove(orderItem);//移除购物车明细
                }
                //如果移除后购物车对象的明细数量为0，则将购物车对象移除
                if(cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }
    @Autowired
    private RedisTemplate redisTemplate;
    //读取redis中的购物车信息
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中读取购物车列表信息..."+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if(cartList==null){
        cartList=new ArrayList<>();
        }
        return cartList;
    }
    //将购物车信息存储到redis中的
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis中存储购物车..."+username);
    redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        //首先遍历其中一个购物车列表中的数据
        for (Cart cart : cartList2) {
            //继续遍历商家中每个购物信息
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                 cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }


    //根据商家ID查询购物车对象
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    //创建订单明细
    private TbOrderItem createOrderItem(TbItem item,Integer num){
        if(num<=0){
            throw new RuntimeException("数量非法");
        }
        TbOrderItem orderItem=new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }
    //判断购物车明细表中是否有该商家商品
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList ,Long itemId ){
        for(TbOrderItem orderItem :orderItemList){
            if(orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }
}



