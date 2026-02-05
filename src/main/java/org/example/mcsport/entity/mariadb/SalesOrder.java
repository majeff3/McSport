package org.example.mcsport.entity.mariadb;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "sales_order")
public class SalesOrder {
    @Id
    @Column(name = "order_id", nullable = false, length = 20)
    private String orderId;

    @Column(name = "project_name", nullable = false, length = 500)
    private String projectName;

    @Column(name = "reviewer", length = 500)
    private String reviewer;

    @ColumnDefault("'pending'")
    @Lob
    @Column(name = "review_status", nullable = false)
    private String reviewStatus;

    @Column(name = "customer_requirements", length = 500)
    private String customerRequirements;

    @Column(name = "order_quantity", nullable = false)
    private Integer orderQuantity;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "customer_id", nullable = false, length = 20)
    private String customerId;

    @Column(name = "custom_id_list", nullable = false, length = 500)
    private String customIdList;

    @Column(name = "process_id_list", nullable = false, length = 500)
    private String processIdList;

    @Column(name = "accessory_product_id_list", length = 500)
    private String accessoryProductIdList;

    @ColumnDefault("0")
    @Column(name = "additional_fee")
    private Float additionalFee;

    @Column(name = "additional_fee_remarks", length = 500)
    private String additionalFeeRemarks;

    @Column(name = "logistics_id", length = 50)
    private String logisticsId;

    @Column(name = "shipping_date")
    private Instant shippingDate;

    @Column(name = "expected_delivery_date", nullable = false)
    private Instant expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private Instant actualDeliveryDate;

    @ColumnDefault("'MOP'")
    @Column(name = "currency", length = 10)
    private String currency;

    @ColumnDefault("-1")
    @Column(name = "cost_price")
    private Float costPrice;

    @Column(name = "responsible_person", nullable = false, length = 500)
    private String responsiblePerson;

    @Column(name = "production_instructions", length = 500)
    private String productionInstructions;

    @Column(name = "store_region", nullable = false, length = 500)
    private String storeRegion;

    @Column(name = "order_type", length = 500)
    private String orderType;

    @Column(name = "total_order_amount", nullable = false)
    private Float totalOrderAmount;

    @ColumnDefault("0")
    @Column(name = "received_amount")
    private Float receivedAmount;

    @ColumnDefault("0")
    @Column(name = "actual_received_amount")
    private Float actualReceivedAmount;

    @ColumnDefault("0")
    @Column(name = "discount_amount")
    private Float discountAmount;

    @Column(name = "shipping_method", length = 500)
    private String shippingMethod;

    @ColumnDefault("0")
    @Column(name = "is_factory_direct")
    private Boolean isFactoryDirect;

    @Column(name = "reference_image", length = 500)
    private String referenceImage;

    @Column(name = "confirm_image_list", length = 500)
    private String confirmImageList;

    @ColumnDefault("0")
    @Column(name = "color_fee")
    private Float colorFee;

    @ColumnDefault("0")
    @Column(name = "provided_sample_quantity")
    private Integer providedSampleQuantity;

    @Column(name = "source_order_number", length = 20)
    private String sourceOrderNumber;

    @Column(name = "unit_of_quantity", length = 200)
    private String unitOfQuantity;

    @Column(name = "quantity")
    private Integer quantity;

    @ColumnDefault("current_timestamp()")
    @Column(name = "order_date")
    private Instant orderDate;

    @ColumnDefault("current_timestamp()")
    @Column(name = "update_date")
    private Instant updateDate;


}