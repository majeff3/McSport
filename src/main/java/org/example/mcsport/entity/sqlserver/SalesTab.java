package org.example.mcsport.entity.sqlserver;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "\"銷售主表\"")
public class SalesTab {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Nationalized
    @Column(name = "\"銷售單號\"", length = 500)
    private String salesOrderNumber;

    @Nationalized
    @Column(name = "\"銷售項目名稱\"", length = 500)
    private String salesItemName;

    @Nationalized
    @Column(name = "\"審覈人\"", length = 500)
    private String reviewer;

    @Nationalized
    @Column(name = "\"流程實例ID\"", length = 500)
    private String processInstanceID;

    @Nationalized
    @Column(name = "\"当前审核人\"", length = 500)
    private String currentReviewer;

    @Nationalized
    @Column(name = "\"流程審覈進度\"", length = 500)
    private String processReviewProgress;

    @Nationalized
    @Column(name = "\"流程狀態\"", length = 500)
    private String processStatus;

    @Nationalized
    @Column(name = "\"客戶編號\"", length = 500)
    private String customerNumber;

    @Nationalized
    @Column(name = "\"確認圖\"", length = 500)
    private String confirmedDrawing;

    @Nationalized
    @Column(name = "\"客戶要求說明\"", length = 500)
    private String customerRequirementsDescription;

    @Nationalized
    @Column(name = "\"門店編號\"", length = 500)
    private String storeNumber;

    @Nationalized
    @Column(name = "\"當前流程節點\"", length = 500)
    private String currentProcessNode;

    @Column(name = "\"訂單數量\"")
    private Long orderQuantity;

    @Nationalized
    @Column(name = "\"備註\"", length = 500)
    private String remarks;

    @Nationalized
    @Column(name = "\"款式編號\"", length = 500)
    private String styleNumber;

    @Column(name = "\"下單日期\"")
    private Instant orderDate;

    @Column(name = "\"交貨日期\"")
    private Instant deliveryDate;

    @Nationalized
    @Column(name = "\"完成流程節點\"", length = 500)
    private String completedProcessNode;

    @Nationalized
    @Column(name = "\"銷售代表\"", length = 500)
    private String salesRepresentative;

    @Nationalized
    @Column(name = "\"地址及郵箱\"", length = 500)
    private String addressAndEmail;

    @Nationalized
    @Column(name = "\"負責人\"", length = 500)
    private String personInCharge;

    @Nationalized
    @Column(name = "\"負責人電話\"", length = 500)
    private String contactPhone;

    @Column(name = "\"聯絡人ID\"")
    private Long contactPersonID;

    @ColumnDefault("0")
    @Column(name = "\"附加費用\"")
    private Double additionalFee;

    @ColumnDefault("0")
    @Column(name = "\"單價\"")
    private Double unitPrice;

    @ColumnDefault("0")
    @Column(name = "\"其它附加費用\"")
    private Double otherAdditionalFees;

    @Nationalized
    @Column(name = "\"其它附加備註\"", length = 500)
    private String otherAdditionalRemarks;

    @Nationalized
    @Column(name = "\"收貨地址\"", length = 500)
    private String shippingAddress;

    @Nationalized
    @Column(name = "\"物流公司\"", length = 500)
    private String logisticsCompany;

    @Nationalized
    @Column(name = "\"物流單號\"", length = 500)
    private String trackingNumber;

    @Nationalized
    @Column(name = "\"收貨人\"", length = 500)
    private String consignee;

    @Nationalized
    @Column(name = "\"收貨人電話號碼\"", length = 500)
    private String consigneePhoneNumber;

    @Column(name = "\"寄送日期\"")
    private Instant shippingDate;

    @ColumnDefault("0")
    @Column(name = "\"已收款\"")
    private Double amountReceived;

    @Nationalized
    @Column(name = "\"币别\"", length = 500)
    private String currency;

    @Nationalized
    @Column(name = "\"採購指令單號\"", length = 500)
    private String purchaseOrderNumber;

    @Column(name = "\"指令时间\"")
    private Instant instructionTime;

    @Column(name = "\"預計交貨日\"")
    private Instant estimatedDeliveryDate;

    @Nationalized
    @Column(name = "\"幣別\"", length = 500)
    private String currency2;

    @ColumnDefault("0")
    @Column(name = "\"其它折扣\"")
    private Double otherDiscounts;

    @ColumnDefault("0")
    @Column(name = "\"零售價\"")
    private Double retailPrice;

    @Column(name = "\"成本價\"")
    private Double costPrice;

    @Nationalized
    @Column(name = "\"尺碼版本\"", length = 500)
    private String sizeVersion;

    @Nationalized
    @Column(name = "\"制單人\"", length = 500)
    private String orderCreator;

    @Nationalized
    @Column(name = "\"生產說明\"", length = 500)
    private String productionDescription;

    @Nationalized
    @Column(name = "\"門店區域\"", length = 500)
    private String storeRegion;

    @Nationalized
    @Column(name = "\"訂單類型\"", length = 500)
    private String orderType;

    @Column(name = "\"訂單總金額\"")
    private Double totalOrderAmount;

    @ColumnDefault("0")
    @Column(name = "\"優惠券金額\"")
    private Double couponAmount;

    @Nationalized
    @Column(name = "\"渠道\"", length = 500)
    private String channel;

    @Column(name = "\"渠道優惠\"")
    private Double channelDiscount;

    @Nationalized
    @Column(name = "\"運輸方式\"", length = 500)
    private String shippingMethod;

    @Column(name = "\"是否工廠直發\"")
    private Long isDirectFromFactory;

    @Nationalized
    @Column(name = "\"參考圖\"", length = 500)
    private String referenceDrawing;

    @Nationalized
    @Column(name = "\"客戶PO號\"", length = 500)
    private String customerPoNumber;

    @Column(name = "\"顏色費用\"")
    private Double colorFee;

    @Nationalized
    @Column(name = "\"確認圖2\"", length = 500)
    private String confirmedDrawing2;

    @Nationalized
    @Column(name = "\"確認圖3\"", length = 500)
    private String confirmedDrawing3;

    @Column(name = "\"提供樣板\"")
    private Long provideSample;

    @Column(name = "\"登記尺碼\"")
    private Long registeredSize;

    @Nationalized
    @Column(name = "\"登記尺碼版本\"", length = 500)
    private String registeredSizeVersion;

    @Nationalized
    @Column(name = "\"來源銷售單號\"", length = 500)
    private String sourceSalesOrderNumber;

    @Nationalized
    @Column(name = "\"訂單修改人\"", length = 500)
    private String orderModifier;

    @Nationalized
    @Column(name = "\"推薦人\"", length = 500)
    private String recommender;

    @Nationalized
    @Column(name = "\"流程實例名稱\"", length = 500)
    private String processInstanceName;

    @Nationalized
    @Column(name = "\"上級审核人\"", length = 500)
    private String superiorReviewer;

    @Nationalized
    @Column(name = "\"上級備註\"", length = 500)
    private String superiorRemarks;

    @Nationalized
    @Column(name = "temp", length = 500)
    private String temp;

    @Column(name = "\"附產品訂單數\"")
    private Double attachedProductOrderQuantity;

    @Column(name = "\"附加費用訂單數\"")
    private Double additionalFeeOrderQuantity;

    @Nationalized
    @Column(name = "\"單位\"", length = 500)
    private String unit;

    @Nationalized
    @Column(name = "\"件數單位\"", length = 500)
    private String pieceUnit;

    @Nationalized
    @Column(name = "\"訂單數單位\"", length = 500)
    private String orderQuantityUnit;

    @Nationalized
    @Column(name = "\"樣版訂單\"", length = 500)
    private String sampleOrder;


}