package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.search.pojo.SkuInfo;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/****
 * @Description:
 * @Author: xiekecheng
 * @Date: 2021/04/01 14:53
 ***/
public class SearchResultMapperImpl implements SearchResultMapper {
    @Override
    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
        List<T> content = new ArrayList<>();
        if (response.getHits() == null || response.getHits().getTotalHits() <= 0) {
            return new AggregatedPageImpl<T>(content);
        }
        for (SearchHit hit : response.getHits()) {
            String sourceAsString = hit.getSourceAsString();
            SkuInfo skuInfo = JSON.parseObject(sourceAsString, SkuInfo.class);

            // 获取高亮域
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField highlightField = highlightFields.get("name");

            // 有高亮则设置高亮的值
            if (highlightField!=null){
                StringBuffer stringBuffer = new StringBuffer();
                for (Text fragment : highlightField.getFragments()) {
                    stringBuffer.append(fragment.toString());
                }
                skuInfo.setName(stringBuffer.toString());
            }
            content.add((T) skuInfo);

        }
        return new AggregatedPageImpl<T>(content,pageable,response.getHits().getTotalHits(),response.getAggregations());

    }
}
