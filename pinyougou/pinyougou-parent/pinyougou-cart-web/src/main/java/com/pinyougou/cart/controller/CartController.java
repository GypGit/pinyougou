package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    //购物车列表
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        //获取用户登录名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人信息"+username);
        //获取cookie中的购物车信息
        String cartListString = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString == null || cartListString.equals("")) {//如果是空,则返回空的购物车列表信息
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        if (username.equals("anonymousUser")) {//未登陆状态
            System.out.println("未登录状态下,读取cookie中的购物车信息...");

            return cartList_cookie;
        } else {//已登陆状态
            System.out.println();
            List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);
            //将redis中购物车信息和cookie中的信息合并
            List<Cart> mergeCartList = cartService.mergeCartList(cartList_cookie, cartListFromRedis);
            //将cookie中的购物车信息删除
            util.CookieUtil.deleteCookie(request, response, "cartList");
            //将合并后的购物车信息存储到redis中
            cartService.saveCartListToRedis(username, mergeCartList);
            System.out.println("登录状态下,将购物车信息合并起来...");
            return mergeCartList;
        }
    }

    //添加商品到购物车
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人"+username);
        try {
            List<Cart> cartList = findCartList();//获取购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (username.equals("anonymousUser")) {//如果用户未登录
                //将数据存入到cookie中
                util.CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24);
                System.out.println("未登录状态,向cookie中存入购物车列表信息...");
            } else {//用户已经登陆
                //向redis中存入购物车列表信息
                cartService.saveCartListToRedis(username, cartList);
                System.out.println("登陆状态下,将购物车信息存入到redis中...");
            }
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }
}


