package org.example.mcsport.entity.req.reimbursement;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class GetReimbursementReq {
    private Instant start_time;
    private Instant end_time;
    private String status;
    private String company;
    private Long handler;
    private Integer page;
    private Integer page_size;
}
