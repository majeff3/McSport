package org.example.mcsport.repository.mariadb;

import org.example.mcsport.entity.mariadb.ExpenseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.time.Instant;
import java.util.List;

public interface ExpenseRecordRepository extends JpaRepository<ExpenseRecord, Long> {

    @Query(nativeQuery = true, value =
            " SELECT * FROM expense_records AS er " +
            " WHERE (er.status = :status OR :status IS NULL) " +
            " AND (:start_time IS NULL OR er.expense_date BETWEEN :start_time AND :end_time) " +
            " AND (:company IS NULL OR er.company_name = :company)" +
            " AND (:handler IS NULL OR er.handler = :handler) " +
            " AND (:expense_type IS NULL OR er.expense_type = :expense_type" +
            "  OR (:expense_type IS NOT NULL AND er.expense_type LIKE CONCAT(:expense_type, '-%'))) " +
            " AND (er.child_id IS NULL OR er.child_id = 0) " +
            " ORDER BY er.created_date DESC" +
            " LIMIT :page_size OFFSET :offset; ")
    List<ExpenseRecord> findExpenseRecordByTimeAndStatus(@Param("status") String status,
                                                         @Param("page_size") int page_size,
                                                         @Param("offset") int offset,
                                                         @Param("start_time") Instant start_time,
                                                         @Param("end_time") Instant end_time,
                                                         @Param("company") String company,
                                                         @Param("handler") Long handler,
                                                         @Param("expense_type") String expense_type);

    ExpenseRecord findExpenseRecordById(Long id);

    @Query(nativeQuery = true, value =
            " SELECT * FROM expense_records AS er " +
            " WHERE er.handler = :handler " +
            " AND (:start_time IS NULL OR er.expense_date BETWEEN :start_time AND :end_time) " +
            " AND (:status IS NULL OR er.status=:status) " +
            " AND (:expense_type IS NULL OR er.expense_type = :expense_type" +
            "  OR (:expense_type IS NOT NULL AND er.expense_type LIKE CONCAT(:expense_type, '-%'))) " +
            " AND (:company IS NULL OR er.company_name = :company) " +
            " AND (er.child_id IS NULL OR er.child_id = 0) " +
            " ORDER BY er.expense_date DESC " +
            " LIMIT :page_size OFFSET :offset; ")
    List<ExpenseRecord> findExpenseRecordByHandlerWithTime(@Param("handler") Long handler,
                                                           @Param("start_time")Instant start_time,
                                                           @Param("end_time") Instant end_time,
                                                           @Param("page_size") int page_size,
                                                           @Param("offset") int offset,
                                                           @Param("status") String status,
                                                           @Param("expense_type") String expense_type,
                                                           @Param("company") String company);

    @Query(nativeQuery = true, value =
            " SELECT count(*) FROM expense_records AS er " +
                    " WHERE (er.status = :status OR :status IS NULL) " +
                    " AND (:start_time IS NULL OR er.expense_date BETWEEN :start_time AND :end_time) " +
                    " AND (:company IS NULL OR er.company_name = :company)" +
                    " AND (:handler IS NULL OR er.handler = :handler)" +
                    " AND (:expense_type IS NULL OR er.expense_type = :expense_type" +
                    "  OR (:expense_type IS NOT NULL AND er.expense_type LIKE CONCAT(:expense_type, '-%')))" +
                    " AND (er.child_id IS NULL OR er.child_id = 0);")
    Long countAllByStatusAndExpenseDateBetween(@Param("status") String status,
                                               @Param("start_time") Instant start_time,
                                               @Param("end_time") Instant end_time,
                                               @Param("company") String company,
                                               @Param("handler") Long handler,
                                               @Param("expense_type") String expense_type);

    @Query(nativeQuery = true, value =
            " SELECT SUM(er.expense_amount) FROM expense_records AS er " +
            " WHERE er.status = 'pending' " +
            " AND (er.child_id IS NULL OR er.child_id = 0); ")
    Double sumAllExpenseAmountByStatus();

    @Query(nativeQuery = true, value =
            " SELECT * FROM expense_records AS er " +
            " WHERE (er.status = :status OR :status IS NULL) " +
            " AND (:start_time IS NULL OR er.expense_date BETWEEN :start_time AND :end_time) " +
            " AND (:handler IS NULL OR er.handler = :handler)" +
            " AND (:company IS NULL OR er.company_name = :company)" +
            " AND (er.child_id IS NULL OR er.child_id = 0); ")
    List<ExpenseRecord> findExpenseRecordByTimeAndCompany(@Param("status") String status,
                                                          @Param("start_time") Instant start_time,
                                                          @Param("end_time") Instant end_time,
                                                          @Param("company") String company,
                                                          @Param("handler") Long user_id);

    @Query(nativeQuery = true, value =
            " SELECT * FROM expense_records AS er " +
            " WHERE er.status = :status " +
            " AND (er.child_id IS NULL OR er.child_id = 0) ")
    List<ExpenseRecord> findAllByStatus(@Param("status") String status);

    @Query(nativeQuery = true, value =
            " SELECT * FROM expense_records AS er " +
            " WHERE MATCH(shipping_number, sales_order_id) AGAINST (:search_text) " +
            " AND (:handler IS NULL OR er.handler = :handler) " +
            " AND (er.child_id IS NULL OR er.child_id = 0) " +
            " ORDER BY er.created_date DESC" +
            " LIMIT :page_size OFFSET :offset; ")
    List<ExpenseRecord> searchExpenseRecordByShippingNumberAndSalesOrderId(@Param("handler") Long handler,
                                                                           @Param("page_size") int page_size,
                                                                           @Param("offset") int offset,
                                                                           @Param("search_text") String search_text);

    @Query(nativeQuery = true, value =
            " SELECT count(*) FROM expense_records AS er " +
            " WHERE MATCH(shipping_number, sales_order_id) AGAINST (:search_text) " +
            " AND (:handler IS NULL OR er.handler = :handler) " +
            " AND (er.child_id IS NULL OR er.child_id = 0) ")
    Long countSearchRecord(@Param("handler") Long handler,
                           @Param("search_text") String search_text);

    boolean existsExpenseRecordByShippingNumber(String shippingNumber);
}
