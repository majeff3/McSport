package org.example.mcsport.entity.req.finance;

import ch.qos.logback.core.joran.action.AppenderRefAction;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.sql.In;

import java.time.Instant;

@Getter
@Setter
public class GetCustomerReq {
    private Instant start_time;
    private Instant end_time;
    private String contact_number;
    private String customer_name;
    private Integer page;
    private Integer page_size;
}
