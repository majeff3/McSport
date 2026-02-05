package org.example.mcsport.configurator;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class CosClientConfig {

    @Value("${cos.common.secretId}")
    private String secretId;

    @Value("${cos.common.secretKey}")
    private String secretKey;

    @Value("${cos.common.region}")
    private String region;

    @Bean
    public COSClient getCosClient() {

        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        COSClient cosclient = new COSClient(cred, clientConfig);

        return cosclient;
    }
}
