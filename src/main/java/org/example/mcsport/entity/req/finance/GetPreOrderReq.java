package org.example.mcsport.entity.req.finance;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetPreOrderReq {
    private Integer page;
    private Integer page_size;
    private String status;
}
