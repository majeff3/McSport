package org.example.mcsport.repository.mariadb;

import org.example.mcsport.entity.mariadb.ReturnRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReturnRecordRepository extends JpaRepository<ReturnRecord, Long> {

    @Query(value = " SELECT * FROM return_record as rr " +
            "WHERE MATCH(rr.sale_number) AGAINST (:sale_number) " +
            " LIMIT :page_size OFFSET :offset; ", nativeQuery = true)
    List<ReturnRecord> findReturnRecordBySaleNumber(@Param("sale_number") String sale_number,
                                                    @Param("page_size") int page_size,
                                                    @Param("offset") int offset);

    @Query(value = " SELECT * FROM return_record " +
            " LIMIT :page_size OFFSET :offset; ", nativeQuery = true)
    List<ReturnRecord> findAllByLimit(@Param("page_size") int page_size,
                                      @Param("offset") int offset);
}