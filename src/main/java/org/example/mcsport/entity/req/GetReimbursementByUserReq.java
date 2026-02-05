package org.example.mcsport.entity.req;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class GetReimbursementByUserReq {
    private Long user_id;
    private Instant start_date;
    private Instant end_date;
    private Integer page;
    private Integer page_size;
    private String status;
}
