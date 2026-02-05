package org.example.mcsport.util;

import com.qcloud.cos.COSClient;
import org.example.mcsport.configurator.CosConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class PdfUtil {
    public List<Map<String, String>> getBase64Pdf(String filePath, CosConfig cosConfig, COSClient cosClient) throws IOException {
        List<String> fileList = List.of(filePath.split(";"));
        List<Map<String, String>> imagesData = new ArrayList<>();
        for(String file : fileList){
            String cosPath = cosConfig.getReimbursementPath() + file;

            byte[] pdfBytes = cosConfig.getFileByte(cosClient, cosPath);
            String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
            Map<String, String> pdfMap = new HashMap<>();
            pdfMap.put("fileName", file);
            pdfMap.put("contentType", "application/pdf");

            pdfMap.put("data", base64Pdf);
            imagesData.add(pdfMap);
        }
        return imagesData;
    }
}
