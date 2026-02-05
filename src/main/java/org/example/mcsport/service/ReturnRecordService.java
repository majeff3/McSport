package org.example.mcsport.service;

import java.time.Instant;

public interface ReturnRecordService {
    Object addReturnRecord(String sale_number, String customer_name, String salesperson,
                           Instant return_date, Instant warehousing_date, String sku_id, String product_name,
                           String color, String size, Integer quantity, Double unit_price, String return_reason,
                           String quality_status, String shelves, String final_result, String remark, Long user_id);

    Object findReturnRecord(String sale_number, Integer page, Integer page_size);

    Object changeReturnRecord(String sale_number, String customer_name, String salesperson,
                              Instant return_date, Instant warehousing_date, String sku_id, String product_name,
                              String color, String size, Integer quantity, Double unit_price, String return_reason,
                              String quality_status, String shelves, String final_result, String remark, Long return_id);
}
