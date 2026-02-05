package org.example.mcsport.entity.req.finance;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCustomerReq {
    private String customer_name;
    private String customer_po_number;
    private String contact_phone;
    private String delivery_address;
    private String remarks;
    private String customer_type;
    private String industry;
}
