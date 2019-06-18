package com.pinyougou.shop.controller;

import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;
@RestController
public class UploadController {
    @Value("${FILE_SERVER_URL}")//获取fastdfs的IP地址
    private String FILE_SERVER_URL;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file) throws Exception {
        //1 获取文件的扩展名
        String originalFilename = file.getOriginalFilename();
        String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        try {
            //2 创建一个fastdfs的客户端
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            //3 执行上传处理
            String path = fastDFSClient.uploadFile(file.getBytes(), extName);
            //4 将返回的file_load和FILE_SERVER_URL进行拼接
            String url = FILE_SERVER_URL+path;
            return new Result(true, url);
        } catch (Exception e) {
            return new Result(false, "上传失败");
        }
    }
}
