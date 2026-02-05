package org.example.mcsport.service.impl;

import jakarta.annotation.Resource;
import org.example.mcsport.entity.mariadb.ReturnRecord;
import org.example.mcsport.repository.mariadb.ReturnRecordRepository;
import org.example.mcsport.service.ReturnRecordService;
import org.example.mcsport.service.impl.jwt.UserDetailsImpl;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class ReturnRecordServiceImpl implements ReturnRecordService {

    @Resource
    private ReturnRecordRepository returnRecordRepository;

    @Override
    public Object addReturnRecord(String sale_number, String customer_name, String salesperson,
                                  Instant return_date, Instant warehousing_date, String sku_id, String product_name,
                                  String color, String size, Integer quantity, Double unit_price, String return_reason,
                                  String quality_status, String shelves, String final_result, String remark,Long user_id) {
        ReturnRecord returnRecord = new ReturnRecord();
        returnRecord.setSaleNumber(sale_number);
        returnRecord.setCustomerName(customer_name);
        returnRecord.setSalesperson(salesperson);
        returnRecord.setReturnDate(return_date);
        returnRecord.setWarehousingDate(warehousing_date);
        returnRecord.setSkuId(sku_id);
        returnRecord.setProductName(product_name);
        returnRecord.setColor(color);
        returnRecord.setSize(size);
        returnRecord.setQuantity(quantity);
        returnRecord.setUnitPrice(BigDecimal.valueOf(unit_price));
        returnRecord.setReturnReason(return_reason);
        returnRecord.setQualityStatus(quality_status);
        returnRecord.setShelves(shelves);
        returnRecord.setFinalResult(final_result);
        returnRecord.setRemark(remark);
        returnRecord.setCreatedDate(Instant.now());
        returnRecord.setUpdatedDate(Instant.now());
        returnRecord.setHandler(user_id);
        return returnRecordRepository.save(returnRecord).getId();
    }

    @Override
    public Object findReturnRecord(String sale_number,Integer page, Integer page_size) {
        int offset = (page-1) * page_size;
        List<ReturnRecord> returnRecordsList = new ArrayList<>();
        if(sale_number == null){
            returnRecordsList = returnRecordRepository.findAllByLimit(page_size, offset);
        }else{
            returnRecordsList = returnRecordRepository.findReturnRecordBySaleNumber(sale_number, page_size, offset);
        }
        Long total = returnRecordRepository.count();
        Map<String, Object> result = new HashMap<>();
        result.put("returnRecords", returnRecordsList);
        result.put("total", total);

        return returnRecordsList;
    }

    @Override
    public Object changeReturnRecord(String sale_number, String customer_name, String salesperson,
                                     Instant return_date, Instant warehousing_date, String sku_id,
                                     String product_name, String color, String size, Integer quantity,
                                     Double unit_price, String return_reason, String quality_status,
                                     String shelves, String final_result, String remark, Long return_id) {
        ReturnRecord returnRecord = returnRecordRepository.findById(return_id).orElse(null);
        if (returnRecord == null){
            return "Return record not exist";
        }

        Authentication Authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = Authentication.getAuthorities();
        Long user_id = ((UserDetailsImpl)Authentication.getPrincipal()).getId();
        if(!returnRecord.getHandler().equals(user_id)&& authorities.stream().noneMatch(grantedAuthority
                -> Objects.equals(grantedAuthority.getAuthority(), "ROLE_ADMIN"))){
            return "User roll don't match";
        }
        returnRecord.setSaleNumber(sale_number);
        returnRecord.setCustomerName(customer_name);
        returnRecord.setSalesperson(salesperson);
        returnRecord.setReturnDate(return_date);
        returnRecord.setWarehousingDate(warehousing_date);
        returnRecord.setSkuId(sku_id);
        returnRecord.setProductName(product_name);
        returnRecord.setColor(color);
        returnRecord.setSize(size);
        returnRecord.setQuantity(quantity);
        returnRecord.setUnitPrice(BigDecimal.valueOf(unit_price));
        returnRecord.setReturnReason(return_reason);
        returnRecord.setQualityStatus(quality_status);
        returnRecord.setShelves(shelves);
        returnRecord.setFinalResult(final_result);
        returnRecord.setRemark(remark);
        returnRecord.setUpdatedDate(Instant.now());
        return returnRecordRepository.save(returnRecord).getId();
    }
}
