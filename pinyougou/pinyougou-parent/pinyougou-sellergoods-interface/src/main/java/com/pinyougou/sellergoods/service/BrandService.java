package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService {
    //访问service的接口,然后自动调用实现类的方法
    //查询所有商品信息
    public List<TbBrand> findAll();
    //根据页码数查询页数
    public PageResult findPage(int pageNum,int pageSize);
    //增加
    public void add(TbBrand brand);
    //修改查询
    public TbBrand findOne(Long id);
    //修改之前查询的
    public void update(TbBrand brand);
    //删除品牌
    public void delete(Long[] ids);
    //条件查询
    public PageResult findPage(TbBrand brand,int pageNum,int pageSize);

    List<Map> selectOptionList();
}
