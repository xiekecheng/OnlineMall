package com.changgou.search.controller;

import com.changgou.search.service.SkuService;
import com.changgou.search.service.impl.SkuServiceImpl;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/****
 * @Description:
 * @Author: xiekecheng
 * @Date: 2021/03/29 13:06
 ***/
@RestController
@RequestMapping("/search")
@CrossOrigin
public class SkuController {
    @Autowired
    private SkuService skuService;

    @GetMapping("/import")
    public Result importData(){
        skuService.importSku();
        return new Result(true, StatusCode.OK,"导入数据到索引库成功");
    }

    @GetMapping
    public Map<String,Object> search(@RequestParam(required = false) Map<String,String> searchMap){
        return skuService.search(searchMap);
    }

    @GetMapping("/keyword")
    public Map<String,Object> keywordSearch(@RequestParam(required = false) Map<String,String> searchMap){
        return skuService.keywordSearch(searchMap);
    }
}
