package org.example.mcsport.service.impl;

import jakarta.annotation.Resource;
import org.example.mcsport.entity.mariadb.Customer;
import org.example.mcsport.entity.mariadb.PreOrder;
import org.example.mcsport.repository.mariadb.CustomerRepository;
import org.example.mcsport.repository.mariadb.PreOrderRepository;
import org.example.mcsport.repository.sqlserver.SalesTabRepository;
import org.example.mcsport.service.FinanceService;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.time.Instant;
import java.util.*;

@Service
public class FinanceServiceImpl implements FinanceService {

    @Resource
    private SalesTabRepository salesTabRepository;
    @Resource
    private CustomerRepository customerRepository;
    @Resource
    private PreOrderRepository preOrderRepository;

    @Override
    public Object generateOrder(String orderNumber) {
        boolean notExistNumber = true;
        return !salesTabRepository.existsBySalesOrderNumber(orderNumber);
    }

    @Override
    public Object getCustomers(Instant start_time, Instant end_time, String contact_number,
                               String customer_name, Integer page, Integer page_size){
        int offset = (page-1) * page_size;
        List<Customer> allCustomer = customerRepository.findCustomerByFilter(contact_number, customer_name,
                start_time, end_time, page_size, offset);
        return allCustomer;
    }

    @Override
    public Object addCustomer(String customer_name, String customer_po_number, String contact_phone,
                              String delivery_address, String remarks, String customer_type, String industry) {
        if(customerRepository.existsByContactPhone(contact_phone)){
            return "customer exists";
        }
        Customer customer = new Customer();
        customer.setCustomerName(customer_name);
        customer.setCustomerPoNumber(customer_po_number);
        customer.setContactPhone(contact_phone);
        customer.setDeliveryAddress(delivery_address);
        customer.setRemarks(remarks);
        customer.setCustomerType(customer_type);
        customer.setIndustry(industry);
        customer.setCreateDate(Instant.now());
        customer.setUpdateDate(Instant.now());

        Customer savedCustomer = customerRepository.save(customer);
        return savedCustomer.getId();
    }

    @Override
    public Object createPreOrder(String store_short_from, String pre_order_product,
                                 String customer_name, String age_range, String po_number,
                                 String contact_phone, String remark) {

        String id = addCustomer(customer_name, po_number,contact_phone,null,remark,null,null).toString();

        PreOrder preOrder = new PreOrder();
        preOrder.setStoreShortFrom(store_short_from);
        preOrder.setPreOrderProduct(pre_order_product);
        preOrder.setCustomerId(id);
        preOrder.setAgeRange(age_range);
        preOrder.setReviewStatus("pending");
        preOrder.setCreateDate(Instant.now());
        preOrder.setUpdateDate(Instant.now());
        preOrder.setUserId(0);

        PreOrder savedPreOrder = preOrderRepository.save(preOrder);
        return savedPreOrder.getOrderId();
    }

    @Override
    public Object getPreOrder(Integer page, Integer page_size, String status) {
        int offset = (page-1) * page_size;
        List<PreOrder> filterPreOrder = preOrderRepository.findPreOrdersByReviewStatusInPage(status, page_size, offset);
        int total = preOrderRepository.countPreOrderByReviewStatus(status);
        List<String> customerIds = new ArrayList<>();
        HashMap<String, String> orderMapCustomer = new HashMap<>();
        for (PreOrder preOrder : filterPreOrder){
            orderMapCustomer.put(preOrder.getOrderId(), preOrder.getCustomerId());
            customerIds.add(preOrder.getCustomerId());
        }

        List<Customer> customers = customerRepository.findAllById(customerIds);
        HashMap<String, Customer> idMapCustomer = new HashMap<>();
        for(Customer customer : customers){
            idMapCustomer.put(customer.getId(),customer);
        }

        HashMap<String, Object> result = new HashMap<>();
        List<Object> pre_orders = new ArrayList<>();
        for(PreOrder preOrder : filterPreOrder){
            HashMap<String,Object> temp = new HashMap<>();
            temp.put("ageRange",preOrder.getAgeRange());
            temp.put("updateDate",preOrder.getUpdateDate());
            temp.put("orderId",preOrder.getOrderId());
            temp.put("preOrderProduct",preOrder.getPreOrderProduct());
            temp.put("reviewStatus",preOrder.getReviewStatus());
            temp.put("storeShortFrom",preOrder.getStoreShortFrom());
            temp.put("userId",preOrder.getUserId());
            temp.put("customerName",idMapCustomer.get(orderMapCustomer.get(preOrder.getOrderId())).getCustomerName());
            pre_orders.add(temp);
        }
        result.put("pre-orders", pre_orders);
        result.put("total", total);
        return result;
    }
}
