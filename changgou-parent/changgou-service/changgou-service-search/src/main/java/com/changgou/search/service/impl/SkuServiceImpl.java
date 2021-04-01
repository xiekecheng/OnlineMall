package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/****
 * @Description:
 * @Author: xiekecheng
 * @Date: 2021/03/28 12:33
 ***/
@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    /**
     * esTemplate 执行索引库的增删改查
     */
    @Autowired
    private ElasticsearchTemplate esTemplate;


    @Override
    public void importSku() {
        //用feign调用已审核的商品
        Result<List<Sku>> listResult = skuFeign.findByStatus("1");
        List<SkuInfo> skuInfos = JSON.parseArray(JSON.toJSONString(listResult.getData()), SkuInfo.class);
        //调用changgou-service-goods微服务
        for (SkuInfo skuInfo : skuInfos) {
            //parseObject 将字符串解析成对象
            Map<String, Object> specMap=JSON.parseObject(skuInfo.getSpec());
            skuInfo.setSpecMap(specMap);
        }
        skuEsMapper.saveAll(skuInfos);

    }

    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        // 搜索条件构造
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        HashMap<String, Object> resultMap = new HashMap<>();

        if (searchMap!=null&&searchMap.size()>0){
            //根据关键词搜索
            String keywords = searchMap.get("keywords");
            if (!StringUtils.isEmpty(keywords)){
                //查询name域的关键词
                //nativeSearchQueryBuilder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name"));
                nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("name",keywords));
            }
        }


        // 分组 查询分类
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategorygroup").field("categoryName").size(50));

        // 分组 查询品牌
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrandgroup").field("brandName").size(50));

        // 分组 查询规格
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpecgroup").field("spec.keyword").size(50));



        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(searchMap.get("brand"))){
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandName",searchMap.get("brand")));
        }
        if (!StringUtils.isEmpty(searchMap.get("category"))) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryName", searchMap.get("category")));
        }
        // 规格过滤查询
        if (searchMap!=null){
            for (String key : searchMap.keySet()) {
                if (key.startsWith("spec_")){
                    boolQueryBuilder.filter(QueryBuilders.termQuery("specMap."+key.substring(5)+".keyword", searchMap.get(key)));

                }
            }
        }
        // 价格过滤查询  price = 0-500
        String price = searchMap.get("price");
        if (!StringUtils.isEmpty(price)){
            String[] split = price.split("-");
            if (!split[1].equalsIgnoreCase("*")){
                // 如果不是末尾,则取区间范围split[0]-split[1]
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").from(split[0],true).to(split[1]));
            }else {
                // 如果是末尾则取大于split[0]
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(split[0]));

            }
        }

        // 构建分页
        // 默认第一页
        int pageNum = 1;
        if (!StringUtils.isEmpty(searchMap.get("pageNum"))){
            pageNum = Integer.parseInt(searchMap.get("pageNum"));
        }
        int pageSize = 10;
        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum-1,pageSize));

        //排序 排序域 sortField 排序规则 sortRule
        String sortField = searchMap.get("sortField");
        String sortRule = searchMap.get("sortRule");
        if (!StringUtils.isEmpty(sortField)&&!StringUtils.isEmpty(sortRule)){
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(sortField).order(sortRule.equals("DESC") ? SortOrder.DESC : SortOrder.ASC));
        }

        // 设置高亮字段
        nativeSearchQueryBuilder.withHighlightFields(new HighlightBuilder.Field("name"));
        nativeSearchQueryBuilder.withHighlightBuilder(new HighlightBuilder().preTags("<em style=\"color:red\">").postTags("</em>"));



        //构建过滤查询
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        // 构建查询对象
        NativeSearchQuery build = nativeSearchQueryBuilder.build();

        // 执行搜索
        AggregatedPage<SkuInfo> page = esTemplate.queryForPage(build, SkuInfo.class);
        StringTerms categoryStringTerms = (StringTerms) page.getAggregation("skuCategorygroup");
        StringTerms brandStringTerms = (StringTerms) page.getAggregation("skuBrandgroup");
        StringTerms specStringTerms = (StringTerms) page.getAggregation("skuSpecgroup");



        if(StringUtils.isEmpty(searchMap.get("brand"))){
            ArrayList<String> brandList = getStringsList(brandStringTerms);
            resultMap.put("brandList",brandList);
        }

        if(StringUtils.isEmpty(searchMap.get("category"))) {
            ArrayList<String> categoryList = getStringsList(categoryStringTerms);
            resultMap.put("categoryList",categoryList);
        }

        HashMap<String, Set<String>> specMap = getStringsMap(specStringTerms);
        // 数据
        List<SkuInfo> rows = page.getContent();
        // 总记录数
        long totalElements = page.getTotalElements();
        //总页数
        int totalPages = page.getTotalPages();

        resultMap.put("rows",rows);
        resultMap.put("totalElements",totalElements);
        resultMap.put("totalPages",totalPages);
        resultMap.put("specMap",specMap);
        return resultMap;
    }

    private ArrayList<String> getStringsList(StringTerms stringTerms) {
        ArrayList<String> stringList = new ArrayList<>();
        if (stringTerms !=null){
            for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();
                stringList.add(keyAsString);
            }
        }
        return stringList;
    }

    private HashMap<String, Set<String>> getStringsMap(StringTerms stringTerms) {
        ArrayList<String> stringList = new ArrayList<>();
        HashMap<String, Set<String>> map = new HashMap<>();
        if (stringTerms !=null){
            for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();
                stringList.add(keyAsString);
            }
        }

        for (String specJson : stringList) {
            Map<String,String> specMap = JSON.parseObject(specJson, Map.class);
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                Set<String> specValues = map.get(key);
                if (specValues==null){
                    specValues = new HashSet<>();
                }
                specValues.add(value);
                map.put(key,specValues);

            }
        }
        return map;
    }

    @Override
    public Map keywordSearch(Map<String, String> searchMap) {

        //1.获取关键字的值
        String keywords = searchMap.get("keywords");

        if (StringUtils.isEmpty(keywords)) {
            keywords = "华为";//赋值给一个默认的值
        }
        //2.创建查询对象 的构建对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        //3.设置查询的条件

        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("name", keywords));

        //4.构建查询对象
        NativeSearchQuery query = nativeSearchQueryBuilder.build();

        //5.执行查询
        AggregatedPage<SkuInfo> skuPage = esTemplate.queryForPage(query, SkuInfo.class);


        //6.返回结果
        Map resultMap = new HashMap<>();
        resultMap.put("rows", skuPage.getContent());
        resultMap.put("total", skuPage.getTotalElements());
        resultMap.put("totalPages", skuPage.getTotalPages());

        return resultMap;
    }




    private Map<String, Set<String>> getStringSetMap(StringTerms stringTermsSpec) {
        //key :规格的名称
        //value :规格名称对应的选项的多个值集合set
        Map<String, Set<String>> specMap = new HashMap<String, Set<String>>();
        Set<String> specValues = new HashSet<String>();
        if (stringTermsSpec != null) {
            //1. 获取分组的结果集
            for (StringTerms.Bucket bucket : stringTermsSpec.getBuckets()) {
                //2.去除结果集的每一行数据()   {"手机屏幕尺寸":"5.5寸","网络":"电信4G","颜色":"白","测试":"s11","机身内存":"128G","存储":"16G","像素":"300万像素"}
                String keyAsString = bucket.getKeyAsString();

                //3.转成JSON 对象  map  key :规格的名称  value:规格名对应的选项的单个值
                Map<String, String> map = JSON.parseObject(keyAsString, Map.class);
                for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
                    String key = stringStringEntry.getKey();//规格名称   手机屏幕尺寸
                    String value = stringStringEntry.getValue();//规格的名称对应的单个选项值 5.5寸

                    //先从原来的specMap中 获取 某一个规格名称 对应的规格的选项值集合
                    specValues = specMap.get(key);
                    if (specValues == null) {
                        specValues = new HashSet<>();
                    }
                    specValues.add(value);
                    //4.提取map中的值放入到返回的map中
                    specMap.put(key, specValues);
                }
            }
        }
        return specMap;
    }

    private List<String> getStringsBrandList(StringTerms stringTermsBrand) {
        List<String> brandList = new ArrayList<>();
        if (stringTermsBrand != null) {
            for (StringTerms.Bucket bucket : stringTermsBrand.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();//品牌的名称 huawei
                brandList.add(keyAsString);
            }
        }
        return brandList;
    }

    /**
     * 获取分组结果   商品分类的分组结果
     *
     * @param stringTermsCategory
     * @return
     */
    private List<String> getStringsCategoryList(StringTerms stringTermsCategory) {
        List<String> categoryList = new ArrayList<>();
        if (stringTermsCategory != null) {
            for (StringTerms.Bucket bucket : stringTermsCategory.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();
                System.out.println(keyAsString);//就是商品分类的数据
                categoryList.add(keyAsString);
            }
        }
        return categoryList;
    }
}
