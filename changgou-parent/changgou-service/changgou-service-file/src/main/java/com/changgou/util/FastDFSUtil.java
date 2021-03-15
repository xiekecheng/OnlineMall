package com.changgou.util;

import com.changgou.file.FastDFSFile;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

public class FastDFSUtil {

    static {

        try {
            //获取claspath下的配置文件的路径
            String filePath = new ClassPathResource("fdfs_client.conf").getPath();
            //加载tracker链接信息
            ClientGlobal.init(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }


    }

    public static String[] upload(FastDFSFile fastDFSFile) throws Exception{

        NameValuePair[] nameValuePair = new NameValuePair[1];
        nameValuePair[0] = new NameValuePair("作者","罗贯中");
        //创建TrackClient来连接trackerServer
        TrackerClient trackerClient = new TrackerClient();
        //获取TrackerServer
        TrackerServer trackerServer = trackerClient.getConnection();
        //通过TrackerServer获取StorageClient
        StorageClient storageClient = new StorageClient(trackerServer, null);
        //使用StorageClient来上传文件
        String[] uploadStrings = storageClient.upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), nameValuePair);
        return uploadStrings;
    }
}
