package org.example.mcsport.entity.req.reimbursement;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@Getter
@Setter
public class AddReimbursementReq {
    private String sales_order_id;
    private String company_name;
    private String expense_type;
    private Double expense_amount;
    private String currency;
    private Long handler;
    private String remarks;
    private Instant expense_date;
    private String shipping_number;
    private String ship_company;
}
