package com.changgou.util;

import com.changgou.file.FastDFSFile;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

    public static StorageClient getStorageClient() throws IOException {
        //创建TrackClient来连接trackerServer
        TrackerClient trackerClient = new TrackerClient();
        //获取TrackerServer
        TrackerServer trackerServer = trackerClient.getConnection();
        //通过TrackerServer获取StorageClient
        return new StorageClient(trackerServer, null);
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

    /**
     * 查看文件信息
     * @param groupName
     * @param remoteFileName
     * @return
     * @throws Exception
     */
    public static FileInfo getFile(String groupName,String remoteFileName) throws Exception {
        //创建TrackClient来连接trackerServer
        TrackerClient trackerClient = new TrackerClient();
        //获取TrackerServer
        TrackerServer trackerServer = trackerClient.getConnection();
        //通过TrackerServer获取StorageClient
        StorageClient storageClient = new StorageClient(trackerServer, null);

        return storageClient.get_file_info(groupName, remoteFileName);

    }

    public static InputStream downloadFile(String groupName, String remoteFileName) throws Exception{
        //创建TrackClient来连接trackerServer
        TrackerClient trackerClient = new TrackerClient();
        //获取TrackerServer
        TrackerServer trackerServer = trackerClient.getConnection();
        //通过TrackerServer获取StorageClient
        StorageClient storageClient = new StorageClient(trackerServer, null);


        byte[] bytes = storageClient.download_file(groupName, remoteFileName);
        InputStream inputStream = new ByteArrayInputStream(bytes);
        return inputStream;
    }

    public static void deleteFile(String groupName, String remoteFileName) throws Exception {
        StorageClient storageClient = getStorageClient();
        storageClient.delete_file(groupName, remoteFileName);
    }

    public void getStorageServerInfo() throws IOException {
        //创建TrackClient来连接trackerServer
        TrackerClient trackerClient = new TrackerClient();
        //获取TrackerServer
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
        storeStorage.getStorePathIndex();


    }

    public static void main(String[] args) throws Exception {
        //FileInfo group1 = getFile("group1", "M00/00/00/rBsADGBOOaSARRP2AAAABXii0VE807.txt");
        //System.out.println(group1.getSourceIpAddr());
        //System.out.println(group1.toString());

        /*
        InputStream inputStream = downloadFile("group1", "M00/00/00/rBsADGBOOaSARRP2AAAABXii0VE807.txt");
        FileOutputStream fileOutputStream = new FileOutputStream("/Users/xiekecheng/Desktop/test/test.txt");
        byte[] buffer = new byte[1024];
        while (inputStream.read(buffer)!=-1){
            fileOutputStream.write(buffer);
        }
        fileOutputStream.flush();
        fileOutputStream.close();
        inputStream.close();

         */

        deleteFile("group1", "M00/00/00/rBsADGBOOaSARRP2AAAABXii0VE807.txt");




    }
}
