package org.example.mcsport.repository.mariadb;

import org.example.mcsport.entity.mariadb.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    boolean existsByContactPhone(String contactPhone);

    @Query(nativeQuery = true, value =
            " SELECT * FROM customer AS c "+
            " WHERE (:phone IS NULL OR c.contact_phone = :phone) " +
            " AND (:name IS NULL OR c.customer_name = :name) " +
            " AND (:start_time IS NULL OR c.update_date BETWEEN :start_time AND :end_time)" +
            " ORDER BY c.update_date DESC" +
            " LIMIT :page_size OFFSET :offset; ")
    List<Customer> findCustomerByFilter(@Param("phone") String phone,
                                        @Param("name") String name,
                                        @Param("start_time") Instant start_time,
                                        @Param("end_time") Instant end_time,
                                        @Param("page_size") int page_size,
                                        @Param("offset") int offset);

    @Query(nativeQuery = true, value =
            " SELECT COUNT(*) FROM customer AS c "+
            " WHERE (:phone IS NULL OR c.contact_phone = :phone) " +
            " AND (:name IS NULL OR c.customer_name = :name) " +
            " AND (:start_time IS NULL OR c.update_date BETWEEN :start_time AND :end_time)")
    long countCustomerByFilter(@Param("phone") String phone,
                               @Param("name") String name,
                               @Param("start_time") Instant start_time,
                               @Param("end_time") Instant end_time);
}