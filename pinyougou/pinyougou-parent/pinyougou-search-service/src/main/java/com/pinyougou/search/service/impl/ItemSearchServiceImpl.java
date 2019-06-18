package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String, Object> map = new HashMap();
        //1.查询列表
        map.putAll(searchList(searchMap));
        //2.根据查询条件获取商品分类结果
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);
        //3.查询品牌和规格列表
        String categoryName = (String) searchMap.get("category");
        if (!"".equals(categoryName)) {
//            System.out.println("查询了品牌和规格");
//            System.out.println(categoryList.size());
            map.putAll(searchBrandAndSpecList(categoryName));
        } else {
            if (categoryList.size() > 0) {
//                System.out.println("查询了品牌和规格");
//                System.out.println(categoryList.size());
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }

        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List GoodsList) {
        Query query = new SimpleQuery();
        Criteria criteria= new Criteria("item_goodsid").in(GoodsList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }


    //3.查询品牌和规格列表
    private Map searchBrandAndSpecList(Object category) {
        Map map = new HashMap();
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);//获取模板ID
        if (typeId != null) {
            //根据模板ID查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            System.out.println(brandList.size());
            map.put("brandList", brandList);//返回值添加品牌列表
            //根据模板ID查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            System.out.println(specList.size());
            map.put("specList", specList);
        }
        return map;
    }

    //2.根据查询条件获取商品分类结果
    private List searchCategoryList(Map searchMap) {
        List list = new ArrayList();
        Query query = new SimpleQuery();
        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据分组页得到分组结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //根据分组结果集得到分组入口结果集
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : content) {
            list.add(entry.getGroupValue());//将分组结果放入到集合中
        }

        return list;
    }

    //1.按照关键字查询
    private Map searchList(Map searchMap) {
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ", ""));
        Map<String, Object> map = new HashMap();
        //创建高亮对象
        HighlightQuery query = new SimpleHighlightQuery();
        //获得高亮选项,设置高亮域
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//设置高亮的域
        highlightOptions.setSimplePrefix("<font style='color:red'>");//设置高亮前缀
        highlightOptions.setSimplePostfix("</font>");//设置高亮后缀
        query.setHighlightOptions(highlightOptions);//将高亮设置放到高亮选项中
        //1.1关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //1.2商品过滤查询
        if (!"".equals(searchMap.get("category"))) {
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.3品牌过滤查询
        if (!"".equals(searchMap.get("brand"))) {
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.4规格过滤查询
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

        }
        //1.5按价格筛选
        if (!searchMap.get("price").equals("")) {
            String str = (String) searchMap.get("price");
            String[] price = str.split("-");
            //如果起点价格大于0
            if (!price[0].equals("0")) {
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!price[1].equals("*")) {//如果终点价格小于*
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
                SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(simpleFilterQuery);
            }
        }
        //1.6搜索结果分页
        //判断前端是否传来分页数据
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if(pageNo==null){
            pageNo=1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if(pageSize==null){
            pageSize=20;
        }
        //设置初始查询起点
        query.setOffset((pageNo-1)*pageSize);
        //设置查询条数
        query.setRows(pageSize);
        //1.7按照指定规则进行排序
        String sortField = (String) searchMap.get("sortField");
        String  sortValue = (String) searchMap.get("sort");
        if(sortValue.equals("ASC")){
            Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
            query.addSort(sort);
        }
        if(sortValue.equals("DESC")){
            Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
            query.addSort(sort);
        }


        //获得高亮页
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //获得高亮页入口集合
        List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();
        //循环遍历高亮页入口集合,获得每个高亮页对象
        for (HighlightEntry<TbItem> h : highlighted) {
            TbItem entity = h.getEntity();//获取到原实体
            if (h.getHighlights().size() > 0 && h.getHighlights().get(0).getSnipplets().size() > 0) {
                entity.setTitle(h.getHighlights().get(0).getSnipplets().get(0));//设置高亮结果
            }
        }
        map.put("rows", page.getContent());
        map.put("totalPages", page.getTotalPages());//返回总页数
        map.put("total", page.getTotalElements());//返回总记录数
        return map;
    }
}
