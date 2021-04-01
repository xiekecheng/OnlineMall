package com.changgou.search.service;

import java.util.Map;

public interface SkuService {
    /**
     * 从mysql导入数据到elastic search
     */
    void importSku();

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    Map<String,Object> search(Map<String,String> searchMap);

    //Map<String, Object> search1(Map<String, String> searchMap);

    Map<String,Object> keywordSearch(Map<String, String> searchMap);

}
