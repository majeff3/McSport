package org.example.mcsport.entity.req;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class AppendPdfReq {
    private Long reimbursement_id;
    private MultipartFile[] file;
    private String pdf_path;
}
