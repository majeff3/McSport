package org.example.mcsport.repository.mariadb;

import org.example.mcsport.entity.mariadb.PreOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PreOrderRepository extends JpaRepository<PreOrder, String> {

    @Query(nativeQuery = true, value =
            " SELECT * FROM pre_order AS po " +
            " WHERE po.review_status = :status OR :status IS NULL" +
            " LIMIT :page_size OFFSET :offset; "
    )
    List<PreOrder> findPreOrdersByReviewStatusInPage(@Param("status") String status,
                                                             @Param("page_size") int page_size,
                                                             @Param("offset") int offset);

    @Query(nativeQuery = true, value =
            " SELECT COUNT(*) FROM pre_order AS po " +
            "WHERE po.review_status = :status OR :status IS NULL; "
    )
    int countPreOrderByReviewStatus(String status);
}