package org.example.mcsport.controller;

import jakarta.annotation.Resource;
import lombok.Getter;
import org.apache.coyote.Response;
import org.apache.poi.ss.usermodel.Workbook;
import org.example.mcsport.entity.req.AppendPdfReq;
import org.example.mcsport.entity.req.GetPdfReq;
import org.example.mcsport.entity.req.GetReimbursementByUserReq;
import org.example.mcsport.entity.req.reimbursement.*;
import org.example.mcsport.service.ReimbursementService;
import org.example.mcsport.service.impl.jwt.UserDetailsImpl;
import org.example.mcsport.util.ImageUtil;
import org.example.mcsport.util.PdfUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/reimbursement")
public class ReimbursementController {
    @Autowired
    private ImageUtil imagesUtil;

    @Autowired
    private PdfUtils pdfUtils;

    @Resource
    private ReimbursementService reimbursementService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/addReimbursement")
    public ResponseEntity<Object> addReimbursement(@RequestBody AddReimbursementReq req){

        Long user_id = ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(reimbursementService.addReimbursement(req.getSales_order_id(), req.getCompany_name(),
                req.getExpense_type(), req.getExpense_amount(), req.getCurrency(), req.getHandler(),
                req.getRemarks(), req.getExpense_date(), user_id, req.getShipping_number(), req.getShip_company()));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/appendImage")
    public ResponseEntity<Object> appendImage(@ModelAttribute AppendImageReq req){
        return ResponseEntity.ok(reimbursementService.appendImage(req.getReimbursement_id(), req.getFile(), req.getAttachment_path()));
    }

    @PostMapping("/getReimbursement")
    public ResponseEntity<Object> getReimbursement(@RequestBody GetReimbursementReq req){
        Long user_id = ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(reimbursementService.getReimbursement(req.getStart_time(), req.getEnd_time(),
                user_id , req.getHandler(), req.getStatus(), req.getPage(), req.getPage_size(), req.getCompany()));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/searchReimbursement")
    public ResponseEntity<Object> searchReimbursement(@RequestBody SearchReimbursementReq req){
        Long user_id = ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(reimbursementService.searchReimbursement(req.getSearch_text(), req.getPage(), req.getPage_size(), user_id));
    }

    @PostMapping("/getImage")
    public ResponseEntity<Object> getImage(@RequestBody GetImageReq req){
        /*
         List<Map<String, String>> imagesData = reimbursementService.getImage(req.getFile_path());
        */
        //List<Map<String, String>> imagesData = new ArrayList<>();
        try {
            Object imagesData = reimbursementService.getImage(req.getFile_path());
            //imagesData = imagesUtil.getBase64Image(req.getFile_path());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(imagesData);
        }catch (IOException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/changeReimbursementStatus")
    public ResponseEntity<Object> changeReimbursementStatus(@RequestBody ChangeReimbursementStatusReq req){
        String user_name = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(reimbursementService.changeReimbursementStatus(req.getReimbursement_id(), user_name, req.getStatus(), req.getReview_comment()));
    }

    @PostMapping("/getReimbursementByUser")
    public ResponseEntity<Object> getReimbursementByUser(@RequestBody GetReimbursementByUserReq req){
        Long user_id = ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(reimbursementService.getReimbursementByUser
                (user_id, req.getStart_date(), req.getEnd_date(), req.getPage(), req.getPage_size(), req.getStatus()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/countAllPending")
    public ResponseEntity<Object> countAllPending(){
        return ResponseEntity.ok(reimbursementService.countAllPending());
    }

    public ResponseEntity<Object> sendPdf() throws IOException {
        // 1. 准备数据
        Map<String, Object> data = new HashMap<>();
        data.put("title", "基于 Thymeleaf 的 PDF 报告");
        data.put("content", "你好 world! 这是一个由 Thymeleaf 模板生成的 PDF 文件。");
        data.put("date", "2023-10-27");
        // 2. 使用 ByteArrayOutputStream 在内存中生成 PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // 指定模板名称 (不需要后缀，也不需要 classpath: 前缀，默认在 resources/templates 下)
        String templateName = "template";

        pdfUtils.generatePdfFromThymeleaf(outputStream, templateName, data);

        // 3. 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "thymeleaf-sample.pdf"); // 下载方式
        // headers.setContentDispositionFormData("inline", "thymeleaf-sample.pdf"); // 预览方式

        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }


    @PreAuthorize("hasRole('USER')")
    @PostMapping("/changeReimbursement")
    public ResponseEntity<Object> changeReimbursement(@RequestBody ChangeReimbursementReq req){

        Long user_id = ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(reimbursementService.changeReimbursement(
                req.getReimbursement_id(), user_id, req.getSales_order_id(), req.getCompany_name(),
                req.getExpense_type(), req.getExpense_amount(), req.getCurrency(),
                req.getHandler(), req.getRemarks(), req.getExpense_date(), req.getShipping_number(), req.getShip_company()));
    }

    @PostMapping("/exportExcel")
    public ResponseEntity<Object> exportExcel(@RequestBody ExportExcelReq req){
        Long user_id = ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Workbook workbook = reimbursementService.exportExcel
                    (req.getStart_time(), req.getEnd_time(), req.getStatus(), req.getCompany(), user_id);
            workbook.write(outputStream);
            workbook.close();
            byte[] excelBytes = outputStream.toByteArray();
            // 将字节数组转换为Base64字符串
            String base64String = java.util.Base64.getEncoder().encodeToString(excelBytes);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + System.currentTimeMillis() + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(base64String);
        }catch (Exception e){
            //throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            return ResponseEntity.notFound().eTag(e.getMessage()).build();
        }
    }

    @PostMapping("/appendPDF")
    public ResponseEntity<Object> appendPDF(@ModelAttribute AppendPdfReq req){
        return ResponseEntity.ok(reimbursementService.appendPDF(req.getReimbursement_id(), req.getFile(), req.getPdf_path()));
    }

    @PostMapping("/getPDF")
    public ResponseEntity<Object> getPDF(@RequestBody GetPdfReq req){
        try {
            Object pdfData = reimbursementService.getPDF(req.getFile_path());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(pdfData);
        }catch (IOException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
