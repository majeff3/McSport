package org.example.mcsport.util;

import com.qcloud.cos.COSClient;
import org.example.mcsport.configurator.CosConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class ImageUtil {
    public List<Map<String, String>> getBase64Image(String filePath, CosConfig cosConfig, COSClient cosClient) throws IOException {
        List<String> fileList = List.of(filePath.split(";"));
        List<Map<String, String>> imagesData = new ArrayList<>();
        for(String file : fileList){
            String cosPath = cosConfig.getReimbursementPath() + file;

            byte[] imageBytes = cosConfig.getFileByte(cosClient, cosPath);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            Map<String, String> imageMap = new HashMap<>();
            imageMap.put("fileName", file);
            // 這裡假設是 JPG，若是 PNG 可根據副檔名判斷
            if(file.contains("jpg")){
                imageMap.put("contentType", "image/jpeg");
            } else if (file.contains("png")) {
                imageMap.put("contentType", "image/png");
            }else {
                imageMap.put("contentType", "image/gif");
            }

            imageMap.put("data", base64Image);
            imagesData.add(imageMap);
        }
        return imagesData;
    }
}
