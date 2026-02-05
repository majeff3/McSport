package org.example.mcsport.controller;

import jakarta.annotation.Resource;
import org.example.mcsport.entity.req.finance.AddCustomerReq;
import org.example.mcsport.entity.req.finance.CreatePreOrderReq;
import org.example.mcsport.entity.req.GenerateOrderReq;
import org.example.mcsport.entity.req.finance.GetCustomerReq;
import org.example.mcsport.entity.req.finance.GetPreOrderReq;
import org.example.mcsport.service.FinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/finance")
public class FinanceController {

    @Resource
    private FinanceService financeService;

    @PostMapping("/generateOrder")
    public ResponseEntity<Object> generateOrder(@RequestBody GenerateOrderReq req){
        return ResponseEntity.ok(financeService.generateOrder(req.getOrderNumber()));
    }

    @PostMapping("/addCustomer")
    public ResponseEntity<Object> addCustomer(@RequestBody AddCustomerReq req){
        return ResponseEntity.ok(financeService.addCustomer(req.getCustomer_name(), req.getCustomer_po_number(), req.getContact_phone(),
                req.getDelivery_address(), req.getRemarks(), req.getCustomer_type(), req.getIndustry()));
    }

    @PostMapping("/createPreOrder")
    public ResponseEntity<Object> createPreOrder(@RequestBody CreatePreOrderReq req){
        return ResponseEntity.ok(financeService.createPreOrder(req.getStore_short_from(), req.getPre_order_product(),
                req.getCustomer_name(), req.getAge_range(), req.getPo_number(), req.getContact_phone(), req.getRemark()));
    }

    @PostMapping("/getPreOrder")
    public ResponseEntity<Object> getPreOrder(@RequestBody GetPreOrderReq req){
        return ResponseEntity.ok(financeService.getPreOrder(req.getPage(), req.getPage_size(), req.getStatus()));
    }

    @PostMapping("/getCustomers")
    public ResponseEntity<Object> getCustomers(@RequestBody GetCustomerReq req){
        return ResponseEntity.ok(financeService.getCustomers(req.getStart_time(),
                req.getEnd_time(), req.getContact_number(), req.getCustomer_name(), req.getPage(), req.getPage_size()));
    }
}
