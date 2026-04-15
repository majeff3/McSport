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
import org.example.mcsport.util.ImageUtil;
import org.example.mcsport.util.PdfUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.netty.udp.UdpServer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

@Service
public class ReimbursementServiceImpl implements ReimbursementService {
    @Autowired
    private ImageUtil imageUtil;
    @Autowired
    private PdfUtil pdfUtil;

    @Autowired
    private COSClient cosClient;
    @Autowired
    private CosConfig cosConfig;

    @Resource
    private ExpenseRecordRepository expenseRecordRepository;

    @Resource
    private UserRepository userRepository;

    @Override
    public Object addReimbursement(String sales_order_id, String company_name, String expense_type,
                                   Double expense_amount, String currency, Long handler, String remarks,
                                   Instant expense_date, Long user_id, String shipping_number, String ship_company){

        Map<String, Object> result = new HashMap<>();
        if(expenseRecordRepository.existsExpenseRecordByShippingNumber(shipping_number)){
            result.put("message","Shipping number already exists");
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
        expenseRecord.setAttachmentPath("");
        expenseRecord.setStatus("pending");
        expenseRecord.setUpdatedDate(Instant.now());
        expenseRecord.setCreatedDate(Instant.now());

        //快遞單相關
        expenseRecord.setShippingNumber(shipping_number);
        expenseRecord.setShipCompany(ship_company);

        ExpenseRecord savedExpenseRecord = expenseRecordRepository.save(expenseRecord);
        result.put("reimbursement_id",savedExpenseRecord.getId());
        return result;
    }

    @Override
    public Object getReimbursement(Instant start_time, Instant end_time, Long user_id, Long handler,
                                   String status, Integer page, Integer page_size, String company) {
        int offset = (page-1) * page_size;
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if (authorities.stream().anyMatch(grantedAuthority
                -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))){
            user_id = handler;
        }

        List<ExpenseRecord> expenseRecordList = expenseRecordRepository.findExpenseRecordByTimeAndStatus
                (status, page_size, offset, start_time, end_time, company, user_id);

        Long total = expenseRecordRepository.countAllByStatusAndExpenseDateBetween
                (status, start_time, end_time, company, user_id);

        List<UserTab> userTabList = userRepository.findAll();
        Map<Long, UserTab> idMapUser = new HashMap<>();
        for(UserTab userTab : userTabList){
            idMapUser.put(userTab.getId(), userTab);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        List<Object> reimbursements =  new ArrayList<>();
        for (ExpenseRecord expenseRecord : expenseRecordList){
            Map<String, Object> temp = new HashMap<>();
            temp.put("attachmentPath", expenseRecord.getAttachmentPath());
            temp.put("companyName", expenseRecord.getCompanyName());
            temp.put("currency", expenseRecord.getCurrency());
            temp.put("expenseAmount", expenseRecord.getExpenseAmount());
            temp.put("expenseDate", expenseRecord.getExpenseDate());
            temp.put("expenseType", expenseRecord.getExpenseType());
            temp.put("handler", idMapUser.get(expenseRecord.getHandler()).getName());
            temp.put("id", expenseRecord.getId());
            temp.put("remarks", expenseRecord.getRemarks());
            temp.put("salesOrderId", expenseRecord.getSalesOrderId());
            temp.put("status", expenseRecord.getStatus());
            temp.put("updatedDate", expenseRecord.getUpdatedDate());
            temp.put("recorder", idMapUser.get(expenseRecord.getRecorder()).getName());
            temp.put("pdfPath", expenseRecord.getPdfPath());
            temp.put("reviewComment", expenseRecord.getReviewComment());
            //temp.put("imageURLs", expenseRecord.getAttachmentPath());
            if(expenseRecord.getExpenseType().contains("速遞費")){
                temp.put("shippingNumber", expenseRecord.getShippingNumber());
                temp.put("shippingCompany", expenseRecord.getShipCompany());
            }
            reimbursements.add(temp);
        }
        result.put("reimbursements",reimbursements);
        return result;
    }

    @Override
    public Object appendImage(Long reimbursement_id, MultipartFile[] file, String attachment_path) {
        ExpenseRecord expenseRecord = expenseRecordRepository.findById(reimbursement_id).orElse(null);
        if(expenseRecord == null){
            return "Expense record not exist!";
        }
        String allFilePath ="";

        if (file != null) {
            for(MultipartFile multipartFile : file){
                if(multipartFile.isEmpty()){continue;}
                String filePath =
                        cosConfig.getImagePath() +
                        System.currentTimeMillis() + "/" +
                        UUID.randomUUID() +
                        Objects.requireNonNull(multipartFile.getOriginalFilename()).
                                substring(multipartFile.getOriginalFilename().lastIndexOf("."));
                try{
                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(multipartFile.getSize()); // 必须设置长度，否则部分实现会回退到临时文件
                    metadata.setContentType(multipartFile.getContentType());
                    InputStream inputStream = multipartFile.getInputStream();
                    cosClient.putObject(cosConfig.getBucketName(), cosConfig.getReimbursementPath() +filePath, inputStream, metadata);
                    allFilePath+=filePath+";";
                }catch (IOException e){
                    throw new RuntimeException("读取上传文件失败", e);
                }catch (NullPointerException e) {
                    throw new RuntimeException("图片名不能为空", e);
                }

                /*try {
                    // 获取保存路径的绝对路径
                    Path path = Paths.get("images/" +filePath);
                    // 确保目录存在
                    Files.createDirectories(path.getParent());
                    // 保存文件
                    Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                    allFilePath+=filePath+";";
                }catch (IOException e) {
                    e.printStackTrace();
                    return "Failed to upload file: " + e.getMessage();
                }*/
            }
            allFilePath = allFilePath.substring(0,allFilePath.length()-1);
        }
        String total = "";

        if(!allFilePath.isEmpty()&&!attachment_path.isEmpty()){
            total = attachment_path+";"+allFilePath;
        } else if (!allFilePath.isEmpty()) {
            total=allFilePath;
        }else if (!attachment_path.isEmpty()){
            total = attachment_path;
        }
        expenseRecord.setAttachmentPath(total);
        expenseRecord.setUpdatedDate(Instant.now());
        return "success " + expenseRecordRepository.save(expenseRecord).getId();
    }

    @Override
    public Object changeReimbursementStatus(Long reimbursement_id, String user_name, String status, String review_comment) {

        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if (!authorities.stream().anyMatch(grantedAuthority
                -> Objects.equals(grantedAuthority.getAuthority(), "ROLE_ADMIN"))){
            return "Unauthorized";
        }
        ExpenseRecord expenseRecord = expenseRecordRepository.findById(reimbursement_id).orElse(null);
        if(expenseRecord == null){
            return "Expense record not exist!";
        }

        expenseRecord.setStatus(status);
        expenseRecord.setReviewComment(review_comment);
        expenseRecord.setUpdatedDate(Instant.now());
        expenseRecordRepository.save(expenseRecord);
        return "success";
    }

    @Override
    public Object getReimbursementByUser(Long user_id, Instant start_date, Instant end_date,
                                         Integer page, Integer page_size, String status) {
        int offset = (page-1) * page_size;
        List<ExpenseRecord> expenseRecordList = expenseRecordRepository.findExpenseRecordByHandlerWithTime
                (user_id, start_date, end_date, page_size, offset, status);

        Long total = expenseRecordRepository.countAllByStatusAndExpenseDateBetween(status, start_date, end_date, null, user_id);
        BigDecimal totalAmountMOP = new BigDecimal(0);
        BigDecimal totalAmountCNY = new BigDecimal(0);
        BigDecimal totalAmountHKD = new BigDecimal(0);
        BigDecimal totalAmountUSD = new BigDecimal(0);
        for (ExpenseRecord expenseRecord: expenseRecordList){
            switch (expenseRecord.getCurrency()){
                case "MOP":
                    totalAmountMOP = totalAmountMOP.add(expenseRecord.getExpenseAmount());
                    break;
                case "CNY":
                    totalAmountCNY = totalAmountCNY.add(expenseRecord.getExpenseAmount());
                    break;
                case "HKD":
                    totalAmountHKD = totalAmountHKD.add(expenseRecord.getExpenseAmount());
                    break;
                case "USD":
                    totalAmountUSD = totalAmountUSD.add(expenseRecord.getExpenseAmount());
                    break;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("reimbursement", expenseRecordList);
        result.put("total_amount_MOP", totalAmountMOP);
        result.put("total_amount_CNY", totalAmountCNY);
        result.put("total_amount_HKD", totalAmountHKD);
        result.put("total_amount_USD", totalAmountUSD);
        result.put("total_pages", total);
        return result;
    }

    @Override
    public Object countAllPending() {
        List<ExpenseRecord> allPending = expenseRecordRepository.findAllByStatus("pending");
        BigDecimal totalAmount = new BigDecimal(0);
        for(ExpenseRecord expenseRecord : allPending){
            BigDecimal currency = new BigDecimal(1);
            switch (expenseRecord.getCurrency()){
                case "MOP":break;
                case "HKD":
                    currency = new BigDecimal(1.01);
                    break;
                case "CNY":
                    currency = new BigDecimal(1.10);
                    break;
                case "USD":
                    currency = new BigDecimal(8);
                    break;
            }
            BigDecimal temp = expenseRecord.getExpenseAmount().multiply(currency);
            totalAmount = totalAmount.add(temp);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("total_pending_amount", totalAmount);
        return result;
    }

    @Override
    public Object changeReimbursement(Long reimbursement_id, Long user_id,
                                      String sales_order_id, String company_name, String expense_type,
                                      Double expense_amount, String currency, Long handler, String remarks,
                                      Instant expense_date, String shipping_number, String ship_company) {
        ExpenseRecord expenseRecord = expenseRecordRepository.findById(reimbursement_id).orElse(null);
        if (expenseRecord == null){
            return "Failed to find reimbursement: " + reimbursement_id;
        }

        if(!expenseRecord.getStatus().equals("pending")&&!expenseRecord.getStatus().equals("rejected")){
            return "The reimbursement has been pended!";
        }

        UserTab userTab = userRepository.findById(user_id).orElse(null);
        if(userTab == null){
            return "User not Found!";
        }

        if(!expenseRecord.getRecorder().equals(userTab.getId())&&!userTab.getRoles().contains("ADMIN")){
            return "User unmatch!";
        }

        expenseRecord.setSalesOrderId(sales_order_id);
        expenseRecord.setCompanyName(company_name);
        expenseRecord.setExpenseType(expense_type);
        expenseRecord.setExpenseAmount(BigDecimal.valueOf(expense_amount));
        expenseRecord.setCurrency(currency);
        expenseRecord.setHandler(handler);
        expenseRecord.setRemarks(remarks);
        expenseRecord.setExpenseDate(expense_date);
        expenseRecord.setUpdatedDate(Instant.now());
        expenseRecord.setRecorder(user_id);
        expenseRecord.setStatus("pending");

        //快遞單相關
        expenseRecord.setShippingNumber(shipping_number);
        expenseRecord.setShipCompany(ship_company);

        ExpenseRecord savedExpenseRecord = expenseRecordRepository.save(expenseRecord);
        Map<String, Object> result = new HashMap<>();
        result.put("reimbursement_id",savedExpenseRecord.getId());
        return result;
    }

    @Override
    public Workbook exportExcel(Instant start_time, Instant end_time, String status, String company, Long user_id) {
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if (authorities.stream().anyMatch(grantedAuthority
                -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))){
            user_id = null;
        }

        List<UserTab> allUSer = userRepository.findAll();
        Map<Long, UserTab> idMapUserTab = new HashMap<>();
        for(UserTab userTab : allUSer){
            idMapUserTab.put(userTab.getId(),userTab);
        }

        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("pending","待審核");
        statusMap.put("approved","已通過");
        statusMap.put("rejected","已拒絕");
        statusMap.put("processing","處理中");
        statusMap.put("completed","已完成");
        List<ExpenseRecord> allExpense = expenseRecordRepository.findExpenseRecordByTimeAndCompany
                (status, start_time, end_time, company, user_id);
        // 创建Excel工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("報銷單");
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("公司名稱");
        headerRow.createCell(2).setCellValue("費用類型");
        headerRow.createCell(3).setCellValue("速遞單號");
        headerRow.createCell(4).setCellValue("速遞公司");
        headerRow.createCell(5).setCellValue("金額");
        headerRow.createCell(6).setCellValue("幣種");
        headerRow.createCell(7).setCellValue("報銷人");
        headerRow.createCell(8).setCellValue("記錄人");
        headerRow.createCell(9).setCellValue("審核狀態");
        headerRow.createCell(10).setCellValue("費用日期");
        headerRow.createCell(11).setCellValue("備注");
        headerRow.createCell(12).setCellValue("更新時間");
        headerRow.createCell(13).setCellValue("銷售訂單編號");
        headerRow.createCell(14).setCellValue("審查評語");
        //headerRow.createCell(12).setCellValue("附件");

        // 定義圖片列的起始索引 (第10列，索引為9)
        final int ATTACHMENT_COL_INDEX = 12;
        int rowNum = 1;
        // 創建 CreationHelper (用於後續創建 Anchor)
        CreationHelper helper = workbook.getCreationHelper();
        // 一個 Sheet 只能有一個 DrawingPatriarch，先創建出來
        Drawing<?> drawing = sheet.createDrawingPatriarch();

        for(ExpenseRecord expenseRecord : allExpense){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(expenseRecord.getId());
            row.createCell(1).setCellValue(expenseRecord.getCompanyName());
            row.createCell(2).setCellValue(expenseRecord.getExpenseType());
            row.createCell(3).setCellValue(expenseRecord.getShippingNumber());
            row.createCell(4).setCellValue(expenseRecord.getShipCompany());
            row.createCell(5).setCellValue(expenseRecord.getExpenseAmount().toString());
            row.createCell(6).setCellValue(expenseRecord.getCurrency());
            row.createCell(7).setCellValue(idMapUserTab.get(expenseRecord.getHandler()).getName());
            row.createCell(8).setCellValue(idMapUserTab.get(expenseRecord.getRecorder()).getName());
            row.createCell(9).setCellValue(statusMap.get(expenseRecord.getStatus()));
            String expenseDate = String.valueOf(expenseRecord.getExpenseDate().atZone(ZoneId.of("Asia/Shanghai")));
            row.createCell(10).setCellValue(expenseDate);
            row.createCell(11).setCellValue(expenseRecord.getRemarks());
            row.createCell(12).setCellValue(expenseRecord.getUpdatedDate().toString());
            row.createCell(13).setCellValue(""+expenseRecord.getSalesOrderId());
            row.createCell(14).setCellValue(expenseRecord.getReviewComment());

            /*List<Map<String, String>> imageData = imageUtil.getBase64Image(expenseRecord.getAttachmentPath(), cosConfig, cosClient);
            int i = 0;
            for(Map<String, String> image : imageData){
                try {
                    insertImageToExcel(workbook, sheet, drawing, helper, image, row.getRowNum(), ATTACHMENT_COL_INDEX + i);

                    // 設置列寬，讓圖片能顯示出來 (設置為20個字符寬)
                    sheet.setColumnWidth(ATTACHMENT_COL_INDEX + i, 30 * 256);
                    i++;
                }catch (Exception e){
                    //todo: 完善報錯
                }
            }*/
        }
        return workbook;
    }

    @Override
    public Object getImage(String file_path) throws IOException{
        return imageUtil.getBase64Image(file_path, cosConfig, cosClient);
    }

    @Override
    public Object appendPDF(Long reimbursement_id, MultipartFile[] file, String pdf_path) {
        ExpenseRecord expenseRecord = expenseRecordRepository.findById(reimbursement_id).orElse(null);
        if(expenseRecord == null){
            return "Expense record not exist!";
        }
        String allFilePath ="";
        if (file != null) {
            for(MultipartFile multipartFile : file){
                if(multipartFile.isEmpty()){continue;}
                String filePath =
                        cosConfig.getPdfPath() +
                                System.currentTimeMillis() + "/" +
                                UUID.randomUUID() +
                                Objects.requireNonNull(multipartFile.getOriginalFilename()).
                                        substring(multipartFile.getOriginalFilename().lastIndexOf("."));
                try{
                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(multipartFile.getSize()); // 必须设置长度，否则部分实现会回退到临时文件
                    metadata.setContentType(multipartFile.getContentType());
                    InputStream inputStream = multipartFile.getInputStream();
                    cosClient.putObject(cosConfig.getBucketName(), cosConfig.getReimbursementPath() +filePath, inputStream, metadata);
                    allFilePath+=filePath+";";
                }catch (IOException e){
                    throw new RuntimeException("读取上传文件失败", e);
                }catch (NullPointerException e) {
                    throw new RuntimeException("图片名不能为空", e);
                }
            }
            allFilePath = allFilePath.substring(0,allFilePath.length()-1);
        }
        String total = "";

        if(!allFilePath.isEmpty()&&!pdf_path.isEmpty()){
            total = pdf_path+";"+allFilePath;
        } else if (!allFilePath.isEmpty()) {
            total=allFilePath;
        }else if (!pdf_path.isEmpty()){
            total = pdf_path;
        }
        expenseRecord.setPdfPath(total);
        expenseRecord.setUpdatedDate(Instant.now());
        return expenseRecordRepository.save(expenseRecord).getId();
    }

    @Override
    public Object getPDF(String file_path) throws IOException{
        return pdfUtil.getBase64Pdf(file_path, cosConfig, cosClient);
    }

    @Override
    public Object searchReimbursement(String search_text, Integer page, Integer page_size, Long user_id) {
        int offset = (page-1) * page_size;
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if (authorities.stream().anyMatch(grantedAuthority
                -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))){
            user_id = null;
        }

        List<UserTab> allUSer = userRepository.findAll();
        Map<Long, UserTab> idMapUserTab = new HashMap<>();
        for(UserTab userTab : allUSer){
            idMapUserTab.put(userTab.getId(),userTab);
        }
        List<UserTab> userTabList = userRepository.findAll();
        Map<Long, UserTab> idMapUser = new HashMap<>();
        for(UserTab userTab : userTabList){
            idMapUser.put(userTab.getId(), userTab);
        }

        Long total = expenseRecordRepository.countSearchRecord(user_id, search_text);

        List<ExpenseRecord> expenseRecordList = expenseRecordRepository.searchExpenseRecordByShippingNumberAndSalesOrderId(user_id,page_size,offset,search_text);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        List<Object> reimbursements =  new ArrayList<>();
        for (ExpenseRecord expenseRecord : expenseRecordList){
            Map<String, Object> temp = new HashMap<>();
            temp.put("attachmentPath", expenseRecord.getAttachmentPath());
            temp.put("companyName", expenseRecord.getCompanyName());
            temp.put("currency", expenseRecord.getCurrency());
            temp.put("expenseAmount", expenseRecord.getExpenseAmount());
            temp.put("expenseDate", expenseRecord.getExpenseDate());
            temp.put("expenseType", expenseRecord.getExpenseType());
            temp.put("handler", idMapUser.get(expenseRecord.getHandler()).getName());
            temp.put("id", expenseRecord.getId());
            temp.put("remarks", expenseRecord.getRemarks());
            temp.put("salesOrderId", expenseRecord.getSalesOrderId());
            temp.put("status", expenseRecord.getStatus());
            temp.put("updatedDate", expenseRecord.getUpdatedDate());
            temp.put("recorder", idMapUser.get(expenseRecord.getRecorder()).getName());
            temp.put("pdfPath", expenseRecord.getPdfPath());
            temp.put("reviewComment", expenseRecord.getReviewComment());
            //temp.put("imageURLs", expenseRecord.getAttachmentPath());
            if(expenseRecord.getExpenseType().contains("速遞費")){
                temp.put("shippingNumber", expenseRecord.getShippingNumber());
                temp.put("shippingCompany", expenseRecord.getShipCompany());
            }
            reimbursements.add(temp);
        }
        result.put("reimbursements",reimbursements);
        return result;

    }

    private void insertImageToExcel(Workbook workbook, Sheet sheet, Drawing<?> drawing, CreationHelper helper, Map<String, String> imageData, int row, int col) throws IOException {
        int formate = Workbook.PICTURE_TYPE_PNG;
        if(imageData.get("contentType").equals("image/jpeg")){
            formate = Workbook.PICTURE_TYPE_JPEG;
        }

        // 獲取 Base64 字串並解碼回原始 byte[]
        String base64String = imageData.get("data");
        byte[] imageBytes = Base64.getDecoder().decode(base64String);

        double imgWidth = 0;
        double imgHeight = 0;
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        BufferedImage bufferedImage = ImageIO.read(bis);
        if (bufferedImage != null) {
            imgWidth = bufferedImage.getWidth();
            imgHeight = bufferedImage.getHeight();
        }

        // 縮小為原尺寸的 1/20
        double scaledWidth = imgWidth;
        double scaledHeight = imgHeight;

        // 添加圖片到 Workbook
        int pictureIdx = workbook.addPicture(imageBytes, formate);

        // 創建錨點 (設置圖片位置)
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(col);
        anchor.setRow1(row);
        anchor.setCol2(col + 1);
        anchor.setRow2(row + 1);

        // 設置錨點類型為不隨 cell 改變大小
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);

        // 繪製圖片
        Picture pict = drawing.createPicture(anchor, pictureIdx);

        // 調整 cell 寬度以適應圖片（考慮同一列可能有多張圖片）
        double requiredColumnWidth = (scaledWidth * 37) + 256; // 加一些 padding

        if (sheet.getColumnWidth(col) < requiredColumnWidth) {
            sheet.setColumnWidth(col, (int) requiredColumnWidth);
        }

        // 調整 row 高度以適應圖片（考慮同一行可能有多張圖片）
        Row currentRow = sheet.getRow(row);
        if (currentRow == null) {
            currentRow = sheet.createRow(row);
        }

        short requiredRowHeight = (short)((scaledHeight * 0.75 * 20) + 20); // 加一些 padding

        if (currentRow.getHeight() < requiredRowHeight) {
            currentRow.setHeight(requiredRowHeight);
        }
    }
}
