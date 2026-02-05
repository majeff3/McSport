package org.example.mcsport.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

public interface ReimbursementService {

    Object addReimbursement(String sales_order_id, String company_name, String expense_type,
                            Double expense_amount, String currency, Long handler, String remarks,
                            Instant expense_date, Long user_id, String shipping_number, String ship_company);

    Object getReimbursement(Instant start_time, Instant end_time, Long user_id, Long handler,
                            String status, Integer page, Integer page_size, String  company);

    Object appendImage(Long reimbursement_id, MultipartFile[] file, String attachment_path);

    Object changeReimbursementStatus(Long reimbursement_id, String user_name, String status);

    Object getReimbursementByUser(Long user_id, Instant start_date, Instant end_date,
                                  Integer page, Integer page_size, String status);

    Object countAllPending();

    Object changeReimbursement(Long reimbursement_id, Long user_id,
                               String sales_order_id, String company_name, String expense_type,
                               Double expense_amount, String currency, Long handler, String remarks,
                               Instant expense_date, String shipping_number, String ship_company);

    Workbook exportExcel(Instant start_time, Instant end_time, String status, String  company, Long user_id) throws Exception;

    Object getImage(String file_path) throws Exception;

    Object appendPDF(Long reimbursement_id, MultipartFile[] file, String pdf_path);

    Object getPDF(String file_path) throws IOException;
}
