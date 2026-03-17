package com.aipaint.oss;

import com.aipaint.util.SecurityContextUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

@Component
public class OssUploadUtil {

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${oss.accessKeyId}")
    private String accessKeyId;

    @Value("${oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${oss.bucketName}")
    private String bucketName;

    private static OSS ossClient;

    @PostConstruct
    public void init() {
        // 验证配置是否完整
        if (accessKeyId == null || accessKeyId.isEmpty()) {
            throw new IllegalArgumentException("OSS accessKeyId is not configured");
        }
        if (accessKeySecret == null || accessKeySecret.isEmpty()) {
            throw new IllegalArgumentException("OSS accessKeySecret is not configured");
        }
        if (endpoint == null || endpoint.isEmpty()) {
            throw new IllegalArgumentException("OSS endpoint is not configured");
        }
        if (bucketName == null || bucketName.isEmpty()) {
            throw new IllegalArgumentException("OSS bucketName is not configured");
        }
        
        ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    public String upload(MultipartFile file, String drawType) throws Exception {
        // 创建OSS客户端

        try {
            // 生成文件名
            String fileName = UUID.randomUUID().toString() + "." + getFileExtension(file.getOriginalFilename());
            String objectName = drawType + "/" + SecurityContextUtil.getCurrentUserId() + "/" + fileName;

            // 设置文件ACL为公共读
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setObjectAcl(CannedAccessControlList.PublicRead);
            // 上传文件
            InputStream inputStream = file.getInputStream();
            ossClient.putObject(bucketName, objectName, inputStream, metadata);

            // 生成URL
            return "https://" + bucketName + "." + endpoint + "/" + objectName;
        } finally {

        }
    }

    /**
     * 通过 URL 直接上传到 OSS（不需要下载到本地）
     *
     * @param imageUrl 图片 URL
     * @param drawType 文件类型目录
     * @return 上传后的 OSS URL
     */
    public String uploadFromUrl(String imageUrl, String drawType) throws Exception {
        // 创建 OSS 客户端

        try {
            // 从 URL 获取输入流
            URL url = new URL(imageUrl);
            InputStream inputStream = url.openStream();

            // 生成文件名
            String fileName = UUID.randomUUID().toString() + "." + getFileExtensionFromUrl(imageUrl);
            String objectName = drawType + "/ai/" + SecurityContextUtil.getCurrentUserId() + "/" + fileName;

            // 设置文件 ACL 为公共读
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setObjectAcl(CannedAccessControlList.PublicRead);

            // 直接上传到 OSS
            ossClient.putObject(bucketName, objectName, inputStream, metadata);

           // 生成 URL
            return "https://" + bucketName + "." + endpoint + "/" + objectName;
        } finally {

        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public static void main(String[] args) {
        new OssUploadUtil().getFileExtensionFromUrl("https://dashscope-result-hz.oss-cn-hangzhou.aliyuncs.com/1d/d2/20260312/96f6710c/3697c259-b15c-4de9-a5c7-b1874115fa02-1.png?Expires=1773415669&OSSAccessKeyId=LTAI5tQZd8AEcZX6KZV4G8qL&Signature=EsPKc0QDr1frEYJZOS94Yzl3iH8%3D");
    }

    private String getFileExtensionFromUrl(String url) {
        // 从 URL 中提取文件名
        String fileName = "";
        // 处理 URL 参数
        if (url.contains("?")) {
            fileName = url.substring(0, url.indexOf('?'));
        }
        return getFileExtension(fileName);
    }
}