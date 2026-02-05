package org.example.mcsport.entity.mariadb;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "process")
public class Process {
    @Id
    @Column(name = "id", nullable = false, length = 20)
    private String id;

    @Column(name = "process_name", length = 50)
    private String processName;

    @Column(name = "remark", length = 500)
    private String remark;

    @ColumnDefault("current_timestamp()")
    @Column(name = "update_date")
    private Instant updateDate;

    @ColumnDefault("current_timestamp()")
    @Column(name = "create_date")
    private Instant createDate;


}