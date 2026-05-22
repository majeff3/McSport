package org.example.mcsport.entity.req.reimbursement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeReimbursementReq {
    private Long reimbursement_id;
    private String sales_order_id;
    private String company_name;
    private String expense_type;
    private Double expense_amount;
    private String currency;
    private Long handler;
    private String remarks;
    private java.time.Instant expense_date;
    private String shipping_number;
    private String ship_company;
    private String attachment_path;  // 圖片URL列表，分號分隔
    private String pdf_path;         // PDF URL列表，分號分隔
}
