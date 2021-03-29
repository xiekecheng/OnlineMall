package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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

        //1.获取关键字的值
        return null;
    }
}
