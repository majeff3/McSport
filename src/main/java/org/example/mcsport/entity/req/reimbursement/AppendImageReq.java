package org.example.mcsport.entity.req.reimbursement;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class AppendImageReq {
    private Long reimbursement_id;
    private MultipartFile[] file;
    private String attachment_path;
}
