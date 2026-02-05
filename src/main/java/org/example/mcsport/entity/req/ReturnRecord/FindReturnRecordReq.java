package org.example.mcsport.entity.req.ReturnRecord;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindReturnRecordReq {
    private String sale_number;
    private Integer page;
    private Integer page_size;
}
