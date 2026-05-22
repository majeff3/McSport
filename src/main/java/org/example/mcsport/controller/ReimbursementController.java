package org.example.mcsport.controller;

import jakarta.annotation.Resource;
import org.example.mcsport.entity.req.AppendPdfReq;
import org.example.mcsport.entity.req.GetPdfReq;
import org.example.mcsport.entity.req.GetReimbursementByUserReq;
import org.example.mcsport.entity.req.reimbursement.*;
import org.example.mcsport.service.ReimbursementService;
import org.example.mcsport.service.impl.jwt.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/reimbursement")
public class ReimbursementController {

    @Resource
    private ReimbursementService reimbursementService;

    // ==================== 新流程：先上傳附件，再創建報銷單 ====================

    /**
     * 上傳單張圖片到 COS，返回文件路徑
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/uploadImage")
    public ResponseEntity<Object> uploadImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(reimbursementService.uploadAttachment(file, "image"));
    }

    /**
     * 上傳單個 PDF 到 COS，返回文件路徑
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/uploadPDF")
    public ResponseEntity<Object> uploadPDF(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(reimbursementService.uploadAttachment(file, "pdf"));
    }

    /**
     * 刪除 COS 上的附件（用戶在前端刪除已上傳的圖片/PDF時調用）
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/deleteAttachment")
    public ResponseEntity<Object> deleteAttachment(@RequestBody Map<String, String> req) {
        return ResponseEntity.ok(reimbursementService.deleteAttachment(req.get("file_path")));
    }

    // ==================== 報銷單 CRUD ====================

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/addReimbursement")
    public ResponseEntity<Object> addReimbursement(@RequestBody AddReimbursementReq req) {
        Long user_id = ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(reimbursementService.addReimbursement(
                req.getSales_order_id(), req.getCompany_name(),
                req.getExpense_type(), req.getExpense_amount(), req.getCurrency(), req.getHandler(),
                req.getRemarks(), req.getExpense_date(), user_id,
                req.getShipping_number(), req.getShip_company(),
                req.getAttachment_path(), req.getPdf_path()));
    }

    @PostMapping("/getReimbursement")
    public ResponseEntity<Object> getReimbursement(@RequestBody GetReimbursementReq req) {
        Long user_id = ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(reimbursementService.getReimbursement(req.getStart_time(), req.getEnd_time(),
                user_id, req.getHandler(), req.getStatus(), req.getPage(), req.getPage_size(), req.getCompany()));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/searchReimbursement")
    public ResponseEntity<Object> searchReimbursement(@RequestBody SearchReimbursementReq req) {
        Long user_id = ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(reimbursementService.searchReimbursement(req.getSearch_text(), req.getPage(), req.getPage_size(), user_id));
    }

    /**
     * 獲取圖片附件（返回原始 byte，非 base64）
     */
    @PostMapping("/getImage")
    public ResponseEntity<Object> getImage(@RequestBody GetImageReq req) {
        try {
            byte[] imageBytes = reimbursementService.getImageBytes(req.getFile_path());
            String contentType = getContentType(req.getFile_path());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 獲取 PDF 附件（返回原始 byte，非 base64）
     */
    @PostMapping("/getPDF")
    public ResponseEntity<Object> getPDF(@RequestBody GetPdfReq req) {
        try {
            byte[] pdfBytes = reimbursementService.getPdfBytes(req.getFile_path());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/changeReimbursementStatus")
    public ResponseEntity<Object> changeReimbursementStatus(@RequestBody ChangeReimbursementStatusReq req) {
        String user_name = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(reimbursementService.changeReimbursementStatus(
                req.getReimbursement_id(), user_name, req.getStatus(), req.getReview_comment()));
    }

    @PostMapping("/getReimbursementByUser")
    public ResponseEntity<Object> getReimbursementByUser(@RequestBody GetReimbursementByUserReq req) {
        Long user_id = ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(reimbursementService.getReimbursementByUser(
                user_id, req.getStart_date(), req.getEnd_date(), req.getPage(), req.getPage_size(), req.getStatus()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/countAllPending")
    public ResponseEntity<Object> countAllPending() {
        return ResponseEntity.ok(reimbursementService.countAllPending());
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/changeReimbursement")
    public ResponseEntity<Object> changeReimbursement(@RequestBody ChangeReimbursementReq req) {
        Long user_id = ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(reimbursementService.changeReimbursement(
                req.getReimbursement_id(), user_id, req.getSales_order_id(), req.getCompany_name(),
                req.getExpense_type(), req.getExpense_amount(), req.getCurrency(),
                req.getHandler(), req.getRemarks(), req.getExpense_date(),
                req.getShipping_number(), req.getShip_company(),
                req.getAttachment_path(), req.getPdf_path()));
    }

    @PostMapping("/exportExcel")
    public ResponseEntity<Object> exportExcel(@RequestBody ExportExcelReq req) {
        Long user_id = ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        try {
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            org.apache.poi.ss.usermodel.Workbook workbook = reimbursementService.exportExcel(
                    req.getStart_time(), req.getEnd_time(), req.getStatus(), req.getCompany(), user_id);
            workbook.write(outputStream);
            workbook.close();
            byte[] excelBytes = outputStream.toByteArray();
            String base64String = java.util.Base64.getEncoder().encodeToString(excelBytes);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + System.currentTimeMillis() + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(base64String);
        } catch (Exception e) {
            return ResponseEntity.notFound().eTag(e.getMessage()).build();
        }
    }

    // ==================== 舊接口保留（向後兼容） ====================

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/appendImage")
    public ResponseEntity<Object> appendImage(@ModelAttribute AppendImageReq req) {
        return ResponseEntity.ok(reimbursementService.appendImage(req.getReimbursement_id(), req.getFile(), req.getAttachment_path()));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/appendPDF")
    public ResponseEntity<Object> appendPDF(@ModelAttribute AppendPdfReq req) {
        return ResponseEntity.ok(reimbursementService.appendPDF(req.getReimbursement_id(), req.getFile(), req.getPdf_path()));
    }

    // ==================== 工具方法 ====================

    private String getContentType(String filePath) {
        String lower = filePath.toLowerCase();
        if (lower.contains(".jpg") || lower.contains(".jpeg")) return "image/jpeg";
        if (lower.contains(".png")) return "image/png";
        if (lower.contains(".gif")) return "image/gif";
        if (lower.contains(".webp")) return "image/webp";
        return "application/octet-stream";
    }
}
