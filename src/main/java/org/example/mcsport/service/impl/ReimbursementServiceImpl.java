package org.example.mcsport.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import jakarta.annotation.Resource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.mcsport.configurator.CosConfig;
import org.example.mcsport.entity.mariadb.ExpenseRecord;
import org.example.mcsport.entity.mariadb.UserTab;
import org.example.mcsport.repository.mariadb.ExpenseRecordRepository;
import org.example.mcsport.repository.mariadb.UserRepository;
import org.example.mcsport.service.ReimbursementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

@Service
public class ReimbursementServiceImpl implements ReimbursementService {
    @Autowired
    private COSClient cosClient;
    @Autowired
    private CosConfig cosConfig;

    @Resource
    private ExpenseRecordRepository expenseRecordRepository;

    @Resource
    private UserRepository userRepository;

    // ==================== 新流程：上傳/刪除附件 ====================

    @Override
    public Object uploadAttachment(MultipartFile file, String type) {
        Map<String, Object> result = new HashMap<>();
        if (file == null || file.isEmpty()) {
            result.put("message", "file is empty");
            return result;
        }
        try {
            String subPath = "image".equals(type)
                    ? cosConfig.getImagePath()
                    : cosConfig.getPdfPath();

            String originalName = Objects.requireNonNull(file.getOriginalFilename());
            String ext = originalName.substring(originalName.lastIndexOf("."));
            String filePath = subPath + System.currentTimeMillis() + "/" + UUID.randomUUID() + ext;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            cosClient.putObject(cosConfig.getBucketName(), cosConfig.getReimbursementPath() + filePath, file.getInputStream(), metadata);

            result.put("file_path", filePath);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("讀取上傳文件失敗", e);
        }
    }

    @Override
    public Object deleteAttachment(String filePath) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (filePath == null || filePath.isEmpty()) {
                result.put("message", "file_path is empty");
                return result;
            }
            cosClient.deleteObject(cosConfig.getBucketName(), cosConfig.getReimbursementPath() + filePath);
            result.put("message", "success");
        } catch (Exception e) {
            result.put("message", "delete failed: " + e.getMessage());
        }
        return result;
    }

    @Override
    public byte[] getImageBytes(String filePath) throws IOException {
        return cosConfig.getFileByte(cosClient, cosConfig.getReimbursementPath() + filePath);
    }

    @Override
    public byte[] getPdfBytes(String filePath) throws IOException {
        return cosConfig.getFileByte(cosClient, cosConfig.getReimbursementPath() + filePath);
    }

    // ==================== 報銷單 CRUD ====================

    @Override
    public Object addReimbursement(String sales_order_id, String company_name, String expense_type,
                                   Double expense_amount, String currency, Long handler, String remarks,
                                   Instant expense_date, Long user_id, String shipping_number, String ship_company,
                                   String attachment_path, String pdf_path) {

        Map<String, Object> result = new HashMap<>();
        if (shipping_number != null && !shipping_number.isEmpty()
                && expenseRecordRepository.existsExpenseRecordByShippingNumber(shipping_number)) {
            result.put("message", "Shipping number already exists");
            return result;
        }

        ExpenseRecord expenseRecord = new ExpenseRecord();
        expenseRecord.setExpenseType(expense_type);
        expenseRecord.setExpenseAmount(new BigDecimal(expense_amount));
        expenseRecord.setRemarks(remarks);
        expenseRecord.setCurrency(currency);
        expenseRecord.setRecorder(user_id);
        expenseRecord.setHandler(handler);
        expenseRecord.setCompanyName(company_name);
        expenseRecord.setSalesOrderId(sales_order_id);
        expenseRecord.setExpenseDate(expense_date);
        expenseRecord.setAttachmentPath(attachment_path != null ? attachment_path : "");
        expenseRecord.setPdfPath(pdf_path != null ? pdf_path : "");
        expenseRecord.setStatus("pending");
        expenseRecord.setUpdatedDate(Instant.now());
        expenseRecord.setCreatedDate(Instant.now());
        expenseRecord.setShippingNumber(shipping_number);
        expenseRecord.setShipCompany(ship_company);

        ExpenseRecord saved = expenseRecordRepository.save(expenseRecord);
        result.put("reimbursement_id", saved.getId());
        return result;
    }

    @Override
    public Object getReimbursement(Instant start_time, Instant end_time, Long user_id, Long handler,
                                   String status, Integer page, Integer page_size, String company) {
        int offset = (page - 1) * page_size;
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            user_id = handler;
        }

        List<ExpenseRecord> list = expenseRecordRepository.findExpenseRecordByTimeAndStatus(status, page_size, offset, start_time, end_time, company, user_id);
        Long total = expenseRecordRepository.countAllByStatusAndExpenseDateBetween(status, start_time, end_time, company, user_id);

        Map<Long, UserTab> userMap = getUserMap();
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("reimbursements", buildReimbursementList(list, userMap));
        return result;
    }

    @Override
    public Object changeReimbursementStatus(Long reimbursement_id, String user_name, String status, String review_comment) {
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if (!authorities.stream().anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"))) {
            return "Unauthorized";
        }
        ExpenseRecord expenseRecord = expenseRecordRepository.findById(reimbursement_id).orElse(null);
        if (expenseRecord == null) {
            return "Expense record not exist!";
        }
        expenseRecord.setStatus(status);
        Long user_id = ((org.example.mcsport.service.impl.jwt.UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        expenseRecord.setReviewer(user_id.toString());
        expenseRecord.setReviewComment(review_comment);
        expenseRecord.setUpdatedDate(Instant.now());
        expenseRecordRepository.save(expenseRecord);
        return "success";
    }

    @Override
    public Object getReimbursementByUser(Long user_id, Instant start_date, Instant end_date,
                                         Integer page, Integer page_size, String status) {
        int offset = (page - 1) * page_size;
        List<ExpenseRecord> list = expenseRecordRepository.findExpenseRecordByHandlerWithTime(user_id, start_date, end_date, page_size, offset, status);
        Long total = expenseRecordRepository.countAllByStatusAndExpenseDateBetween(status, start_date, end_date, null, user_id);

        BigDecimal totalMOP = BigDecimal.ZERO, totalCNY = BigDecimal.ZERO, totalHKD = BigDecimal.ZERO, totalUSD = BigDecimal.ZERO;
        Map<Long, UserTab> userMap = getUserMap();

        List<Object> reimbursements = new ArrayList<>();
        for (ExpenseRecord er : list) {
            Map<String, Object> temp = buildReimbursementMap(er, userMap);
            reimbursements.add(temp);
            switch (er.getCurrency()) {
                case "MOP": totalMOP = totalMOP.add(er.getExpenseAmount()); break;
                case "CNY": totalCNY = totalCNY.add(er.getExpenseAmount()); break;
                case "HKD": totalHKD = totalHKD.add(er.getExpenseAmount()); break;
                case "USD": totalUSD = totalUSD.add(er.getExpenseAmount()); break;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("reimbursement", reimbursements);
        result.put("total_amount_MOP", totalMOP);
        result.put("total_amount_CNY", totalCNY);
        result.put("total_amount_HKD", totalHKD);
        result.put("total_amount_USD", totalUSD);
        result.put("total_pages", total);
        return result;
    }

    @Override
    public Object countAllPending() {
        List<ExpenseRecord> allPending = expenseRecordRepository.findAllByStatus("pending");
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ExpenseRecord er : allPending) {
            BigDecimal rate = BigDecimal.ONE;
            switch (er.getCurrency()) {
                case "HKD": rate = new BigDecimal("1.01"); break;
                case "CNY": rate = new BigDecimal("1.10"); break;
                case "USD": rate = new BigDecimal("8"); break;
            }
            totalAmount = totalAmount.add(er.getExpenseAmount().multiply(rate));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("total_pending_amount", totalAmount);
        return result;
    }

    @Override
    public Object changeReimbursement(Long reimbursement_id, Long user_id,
                                      String sales_order_id, String company_name, String expense_type,
                                      Double expense_amount, String currency, Long handler, String remarks,
                                      Instant expense_date, String shipping_number, String ship_company,
                                      String attachment_path, String pdf_path) {
        ExpenseRecord er = expenseRecordRepository.findById(reimbursement_id).orElse(null);
        if (er == null) return "Failed to find reimbursement: " + reimbursement_id;
        if (!er.getStatus().equals("pending") && !er.getStatus().equals("rejected")) return "The reimbursement has been pended!";

        UserTab userTab = userRepository.findById(user_id).orElse(null);
        if (userTab == null) return "User not Found!";
        if (!er.getRecorder().equals(userTab.getId()) && !userTab.getRoles().contains("ADMIN")) return "User unmatch!";

        er.setSalesOrderId(sales_order_id);
        er.setCompanyName(company_name);
        er.setExpenseType(expense_type);
        er.setExpenseAmount(BigDecimal.valueOf(expense_amount));
        er.setCurrency(currency);
        er.setHandler(handler);
        er.setRemarks(remarks);
        er.setExpenseDate(expense_date);
        er.setUpdatedDate(Instant.now());
        er.setRecorder(user_id);
        er.setStatus("pending");
        er.setShippingNumber(shipping_number);
        er.setShipCompany(ship_company);
        er.setAttachmentPath(attachment_path != null ? attachment_path : "");
        er.setPdfPath(pdf_path != null ? pdf_path : "");

        Map<String, Object> result = new HashMap<>();
        result.put("reimbursement_id", expenseRecordRepository.save(er).getId());
        return result;
    }

    @Override
    public Workbook exportExcel(Instant start_time, Instant end_time, String status, String company, Long user_id) {
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            user_id = null;
        }

        Map<Long, UserTab> userMap = getUserMap();
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("pending", "待審核");
        statusMap.put("approved", "已通過");
        statusMap.put("rejected", "已拒絕");
        statusMap.put("processing", "處理中");
        statusMap.put("completed", "已完成");

        List<ExpenseRecord> allExpense = expenseRecordRepository.findExpenseRecordByTimeAndCompany(status, start_time, end_time, company, user_id);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("報銷單");
        Row header = sheet.createRow(0);
        String[] headers = {"ID", "公司名稱", "費用類型", "速遞單號", "速遞公司", "金額", "幣種", "報銷人", "記錄人", "審核狀態", "費用日期", "備注", "更新時間", "銷售訂單編號", "審查評語"};
        for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);

        int rowNum = 1;
        for (ExpenseRecord er : allExpense) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(er.getId());
            row.createCell(1).setCellValue(er.getCompanyName());
            row.createCell(2).setCellValue(er.getExpenseType());
            row.createCell(3).setCellValue(er.getShippingNumber());
            row.createCell(4).setCellValue(er.getShipCompany());
            row.createCell(5).setCellValue(er.getExpenseAmount().toString());
            row.createCell(6).setCellValue(er.getCurrency());
            row.createCell(7).setCellValue(userMap.get(er.getHandler()).getName());
            row.createCell(8).setCellValue(userMap.get(er.getRecorder()).getName());
            row.createCell(9).setCellValue(statusMap.get(er.getStatus()));
            String dateStr = String.valueOf(er.getExpenseDate().atZone(ZoneId.of("Asia/Shanghai"))).substring(0, 10);
            row.createCell(10).setCellValue(dateStr);
            row.createCell(11).setCellValue(er.getRemarks());
            row.createCell(12).setCellValue(er.getUpdatedDate().toString());
            row.createCell(13).setCellValue(er.getSalesOrderId() != null ? er.getSalesOrderId() : "");
            row.createCell(14).setCellValue(er.getReviewComment() != null ? er.getReviewComment() : "");
        }
        return workbook;
    }

    // 保留舊接口（向後兼容）
    @Override
    public Object getImage(String file_path) throws Exception {
        return getOldBase64Images(file_path);
    }

    @Override
    public Object appendImage(Long reimbursement_id, MultipartFile[] file, String attachment_path) {
        ExpenseRecord er = expenseRecordRepository.findById(reimbursement_id).orElse(null);
        if (er == null) return "Expense record not exist!";
        String allFilePath = uploadFilesToCos(file, "image");
        String total = mergePaths(attachment_path, allFilePath);
        er.setAttachmentPath(total);
        er.setUpdatedDate(Instant.now());
        return "success " + expenseRecordRepository.save(er).getId();
    }

    @Override
    public Object appendPDF(Long reimbursement_id, MultipartFile[] file, String pdf_path) {
        ExpenseRecord er = expenseRecordRepository.findById(reimbursement_id).orElse(null);
        if (er == null) return "Expense record not exist!";
        String allFilePath = uploadFilesToCos(file, "pdf");
        String total = mergePaths(pdf_path, allFilePath);
        er.setPdfPath(total);
        er.setUpdatedDate(Instant.now());
        return expenseRecordRepository.save(er).getId();
    }

    @Override
    public Object getPDF(String file_path) throws IOException {
        return getOldBase64Pdfs(file_path);
    }

    @Override
    public Object searchReimbursement(String search_text, Integer page, Integer page_size, Long user_id) {
        int offset = (page - 1) * page_size;
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            user_id = null;
        }

        Long total = expenseRecordRepository.countSearchRecord(user_id, search_text);
        List<ExpenseRecord> list = expenseRecordRepository.searchExpenseRecordByShippingNumberAndSalesOrderId(user_id, page_size, offset, search_text);

        Map<Long, UserTab> userMap = getUserMap();
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("reimbursements", buildReimbursementList(list, userMap));
        return result;
    }

    // ==================== 私有工具方法 ====================

    private Map<Long, UserTab> getUserMap() {
        Map<Long, UserTab> map = new HashMap<>();
        for (UserTab u : userRepository.findAll()) map.put(u.getId(), u);
        return map;
    }

    private List<Object> buildReimbursementList(List<ExpenseRecord> records, Map<Long, UserTab> userMap) {
        List<Object> list = new ArrayList<>();
        for (ExpenseRecord er : records) list.add(buildReimbursementMap(er, userMap));
        return list;
    }

    private Map<String, Object> buildReimbursementMap(ExpenseRecord er, Map<Long, UserTab> userMap) {
        Map<String, Object> temp = new HashMap<>();
        temp.put("id", er.getId());
        temp.put("salesOrderId", er.getSalesOrderId());
        temp.put("companyName", er.getCompanyName());
        temp.put("expenseType", er.getExpenseType());
        temp.put("expenseAmount", er.getExpenseAmount());
        temp.put("currency", er.getCurrency());
        temp.put("handler", userMap.containsKey(er.getHandler()) ? userMap.get(er.getHandler()).getName() : "unknown");
        temp.put("recorder", userMap.containsKey(er.getRecorder()) ? userMap.get(er.getRecorder()).getName() : "unknown");
        temp.put("status", er.getStatus());
        temp.put("expenseDate", er.getExpenseDate());
        temp.put("remarks", er.getRemarks());
        temp.put("attachmentPath", er.getAttachmentPath());
        temp.put("pdfPath", er.getPdfPath());
        temp.put("reviewComment", er.getReviewComment());
        temp.put("updatedDate", er.getUpdatedDate());
        if (er.getExpenseType() != null && er.getExpenseType().contains("速遞費")) {
            temp.put("shippingNumber", er.getShippingNumber());
            temp.put("shippingCompany", er.getShipCompany());
        }
        return temp;
    }

    private String uploadFilesToCos(MultipartFile[] files, String type) {
        if (files == null) return "";
        StringBuilder sb = new StringBuilder();
        for (MultipartFile f : files) {
            if (f.isEmpty()) continue;
            String subPath = "image".equals(type) ? cosConfig.getImagePath() : cosConfig.getPdfPath();
            String originalName = Objects.requireNonNull(f.getOriginalFilename());
            String ext = originalName.substring(originalName.lastIndexOf("."));
            String filePath = subPath + System.currentTimeMillis() + "/" + UUID.randomUUID() + ext;
            try {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(f.getSize());
                metadata.setContentType(f.getContentType());
                cosClient.putObject(cosConfig.getBucketName(), cosConfig.getReimbursementPath() + filePath, f.getInputStream(), metadata);
                sb.append(filePath).append(";");
            } catch (IOException e) {
                throw new RuntimeException("讀取上傳文件失敗", e);
            }
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private String mergePaths(String existing, String newPath) {
        if (newPath != null && !newPath.isEmpty() && existing != null && !existing.isEmpty())
            return existing + ";" + newPath;
        if (newPath != null && !newPath.isEmpty()) return newPath;
        if (existing != null && !existing.isEmpty()) return existing;
        return "";
    }

    private List<Map<String, String>> getOldBase64Images(String filePath) throws IOException {
        List<String> fileList = List.of(filePath.split(";"));
        List<Map<String, String>> result = new ArrayList<>();
        for (String file : fileList) {
            byte[] bytes = cosConfig.getFileByte(cosClient, cosConfig.getReimbursementPath() + file);
            Map<String, String> map = new HashMap<>();
            map.put("fileName", file);
            map.put("contentType", file.contains("png") ? "image/png" : "image/jpeg");
            map.put("data", Base64.getEncoder().encodeToString(bytes));
            result.add(map);
        }
        return result;
    }

    private List<Map<String, String>> getOldBase64Pdfs(String filePath) throws IOException {
        List<String> fileList = List.of(filePath.split(";"));
        List<Map<String, String>> result = new ArrayList<>();
        for (String file : fileList) {
            byte[] bytes = cosConfig.getFileByte(cosClient, cosConfig.getReimbursementPath() + file);
            Map<String, String> map = new HashMap<>();
            map.put("fileName", file);
            map.put("contentType", "application/pdf");
            map.put("data", Base64.getEncoder().encodeToString(bytes));
            result.add(map);
        }
        return result;
    }
}
