package com.changgou.controller;

import com.changgou.file.FastDFSFile;
import com.changgou.util.FastDFSUtil;
import entity.Result;
import entity.StatusCode;
import io.netty.util.internal.StringUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/upload")
@CrossOrigin
public class FileUploadController {

    @PostMapping
    public Result upload(@RequestParam("file")MultipartFile file) throws Exception {

        FastDFSFile fastDFSFile = new FastDFSFile(file.getOriginalFilename(),file.getBytes(), StringUtils.getFilenameExtension(file.getOriginalFilename()));
        String[] uploads = FastDFSUtil.upload(fastDFSFile);
        String url = "http://118.24.105.228:8080/"+uploads[0]+"/"+uploads[1];
        return new Result(true, StatusCode.OK,"上传成功",url);
    }
}
