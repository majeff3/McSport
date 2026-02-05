package org.example.mcsport.controller;

import jakarta.annotation.Resource;
import org.example.mcsport.entity.req.ReturnRecord.AddReturnRecordReq;
import org.example.mcsport.entity.req.ReturnRecord.ChangeReturnRecordReq;
import org.example.mcsport.entity.req.ReturnRecord.FindReturnRecordReq;
import org.example.mcsport.service.ReturnRecordService;
import org.example.mcsport.service.impl.jwt.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/return")
public class ReturnRecordController {
    @Resource
    private ReturnRecordService returnRecordService;

    @PostMapping("/addReturnRecord")
    public ResponseEntity<Object> addReturnRecord(@RequestBody AddReturnRecordReq req){
        Long user_id = ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(returnRecordService.addReturnRecord(req.getSale_number(), req.getCustomer_name(), req.getSalesperson(), req.getReturn_date(),
                req.getWarehousing_date(), req.getSku_id(), req.getProduct_name(), req.getColor(), req.getSize(), req.getQuantity(), req.getUnit_price(),
                req.getReturn_reason(), req.getQuality_status(), req.getShelves(), req.getFinal_result(), req.getRemark(), user_id));
    }

    @PostMapping("/findReturnRecord")
    public ResponseEntity<Object> findReturnRecord(@RequestBody FindReturnRecordReq req){
        return ResponseEntity.ok(returnRecordService.findReturnRecord(req.getSale_number(), req.getPage(), req.getPage_size()));
    }

    @PostMapping("/changeReturnRecord")
    public ResponseEntity<Object> changeReturnRecord(@RequestBody ChangeReturnRecordReq req){
        return ResponseEntity.ok(returnRecordService.changeReturnRecord(req.getSale_number(), req.getCustomer_name(), req.getSalesperson(), req.getReturn_date(),
                req.getWarehousing_date(), req.getSku_id(), req.getProduct_name(), req.getColor(), req.getSize(), req.getQuantity(), req.getUnit_price(),
                req.getReturn_reason(), req.getQuality_status(), req.getShelves(), req.getFinal_result(), req.getRemark(), req.getReturn_id()));
    }
}
