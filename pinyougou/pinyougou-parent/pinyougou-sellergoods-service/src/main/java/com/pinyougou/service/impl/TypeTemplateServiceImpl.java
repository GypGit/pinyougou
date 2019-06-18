package com.pinyougou.service.impl;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.pojo.TbTypeTemplateExample;
import com.pinyougou.pojo.TbTypeTemplateExample.Criteria;
import com.pinyougou.sellergoods.service.TypeTemplateService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TbTypeTemplateMapper typeTemplateMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbTypeTemplate> findAll() {
        return typeTemplateMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.insert(typeTemplate);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.updateByPrimaryKey(typeTemplate);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbTypeTemplate findOne(Long id) {
        return typeTemplateMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            typeTemplateMapper.deleteByPrimaryKey(id);
        }
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbTypeTemplateExample example = new TbTypeTemplateExample();
        Criteria criteria = example.createCriteria();

        if (typeTemplate != null) {
            if (typeTemplate.getName() != null && typeTemplate.getName().length() > 0) {
                criteria.andNameLike("%" + typeTemplate.getName() + "%");
            }
            if (typeTemplate.getSpecIds() != null && typeTemplate.getSpecIds().length() > 0) {
                criteria.andSpecIdsLike("%" + typeTemplate.getSpecIds() + "%");
            }
            if (typeTemplate.getBrandIds() != null && typeTemplate.getBrandIds().length() > 0) {
                criteria.andBrandIdsLike("%" + typeTemplate.getBrandIds() + "%");
            }
            if (typeTemplate.getCustomAttributeItems() != null && typeTemplate.getCustomAttributeItems().length() > 0) {
                criteria.andCustomAttributeItemsLike("%" + typeTemplate.getCustomAttributeItems() + "%");
            }

        }

        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(example);
        saveToRedis();
        return new PageResult(page.getTotal(), page.getResult());
    }

    private void saveToRedis() {
        //获取模板数据
        List<TbTypeTemplate> typeTemplateList = findAll();
        for (TbTypeTemplate typeTemplate : typeTemplateList) {
            List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(),Map.class);
            redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(), brandList);
            //存储规格列表
            List<Map> specList = findSpecList(typeTemplate.getId());//根据模板ID查询规格列表
            redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), specList);
        }

    }

    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;

    @Override
    public List<Map> findSpecList(Long id) {
        TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);//通过模板ID查询模板
        List<Map> list = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);//获取到模板specIds这个字符串,并把它转换成map类型的
        for (Map map : list) {
            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
            criteria.andSpecIdEqualTo(new Long((Integer) map.get("id")));
            List<TbSpecificationOption> options = specificationOptionMapper.selectByExample(example);
            map.put("options", options);
        }
        return list;
    }

}
