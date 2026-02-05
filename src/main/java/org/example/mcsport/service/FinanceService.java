package org.example.mcsport.service;

import java.time.Instant;

public interface FinanceService {

    Object generateOrder(String orderNumber);

    Object addCustomer(String customer_name, String customer_po_number, String contact_phone,
                       String delivery_address, String remarks, String customer_type, String industry);

    Object createPreOrder(String store_short_from, String pre_order_product,
                          String customer_name, String age_range, String po_number,
                          String contact_phone, String remark);

    Object getPreOrder(Integer page, Integer page_size, String status);

    Object getCustomers(Instant start_time, Instant end_time, String contact_number,
                        String customer_name, Integer page, Integer page_size);
}
