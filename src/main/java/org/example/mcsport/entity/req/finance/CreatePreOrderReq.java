package org.example.mcsport.entity.req.finance;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePreOrderReq {
    private String store_short_from;
    private String pre_order_product;
    private String customer_name;
    private String age_range;
    private String po_number;
    private String contact_phone;
    private String remark;
}
