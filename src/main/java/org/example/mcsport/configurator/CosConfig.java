package org.example.mcsport.configurator;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Data
@Configuration
public class CosConfig {
    @Value("${cos.common.imagePath}")
    private String imagePath;

    @Value("${cos.common.reimbursementPath}")
    private String reimbursementPath;

    @Value("${cos.common.bucketName}")
    private String bucketName;

    @Value("${cos.common.pdfPath}")
    private String pdfPath;

    public String getPresignedImageUrl(COSClient cosClient, String key, int expirationMinutes) {
        // 1. 计算过期时间：当前时间 + N 分钟
        Date expiration = new Date(System.currentTimeMillis()
                + expirationMinutes * 60L * 1000L);

        // 2. 构造请求：GET 下载/预览
        GeneratePresignedUrlRequest request =
                new GeneratePresignedUrlRequest(bucketName, key, HttpMethodName.GET);
        request.setExpiration(expiration); // 设置过期时间

        // 3. 使用已有的 cosClient 生成预签名 URL
        URL url = cosClient.generatePresignedUrl(request);

        // 4. 转成字符串返回
        return url.toString();
    }

    public byte[] getFileByte(COSClient cosClient, String key) throws IOException {
        COSObject cosObject = cosClient.getObject(bucketName, key);
        InputStream input = cosObject.getObjectContent();
        byte[] bytes = IOUtils.toByteArray(input);
        return bytes;
    }
}
