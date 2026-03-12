package org.example.mcsport.entity.req.reimbursement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchReimbursementReq {
    private String search_text;
    private Integer page;
    private Integer page_size;
}
