package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

public interface CartService {

    // 添加商品到购物车
    public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);

    //读取redis中的购物车列表信息
    public List<Cart> findCartListFromRedis(String username);

    //将购物车存储到redis中
    public void saveCartListToRedis(String username,List<Cart> cartList);

    //合并redis中和cookie中的购物车信息
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
