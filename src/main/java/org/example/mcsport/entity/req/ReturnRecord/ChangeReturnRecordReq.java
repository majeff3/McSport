package org.example.mcsport.entity.req.ReturnRecord;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ChangeReturnRecordReq {
    private Long return_id;
    private String sale_number;
    private String customer_name;
    private String salesperson;
    private Instant return_date;
    private Instant warehousing_date;
    private String sku_id;
    private String product_name;
    private String color;
    private String size;
    private Integer quantity;
    private Double unit_price;
    private String return_reason;
    private String quality_status;
    private String shelves;
    private String final_result;
    private String remark;
}
