package com.aipaint.test;

/**
 * @author cj.lu
 * @date 2026年06⽉10⽇ 1:19
 */
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 京东物流回调通知接收实体类
 */
@Data
@NoArgsConstructor
public class JdLogisticsCallback {

    /** 京东运单号 */
    @JsonProperty("orderNo")
    private String orderNo;

    /** 客户订单号 */
    @JsonProperty("customerOrderNo")
    private String customerOrderNo;

    /** 运单号 */
    @JsonProperty("waybillNo")
    private String waybillNo;

    /** 轨迹备注信息 */
    @JsonProperty("remark")
    private String remark;

    /** 操作人姓名 */
    @JsonProperty("operatorName")
    private String operatorName;

    /** 操作时间 (格式示例: 2023-01-01 11:01:01) */
    @JsonProperty("operationTime")
    private String operationTime;

    /** 操作类型 */
    @JsonProperty("operationType")
    private String operationType;

    /** 操作码 */
    @JsonProperty("operationCode")
    private String operationCode;

    /** 操作网点名称 */
    @JsonProperty("operateSite")
    private String operateSite;

    /** 操作网点ID */
    @JsonProperty("operateSiteId")
    private String operateSiteId;

    /** 操作位置信息 */
    @JsonProperty("operateLocation")
    private OperateLocation operateLocation;

    /** 扩展信息 */
    @JsonProperty("extend")
    private ExtendInfo extend;

    /**
     * 操作位置信息
     */
    @Data
    @NoArgsConstructor
    public static class OperateLocation {
        @JsonProperty("routeProvinceName")
        private String routeProvinceName;

        @JsonProperty("routeCityName")
        private String routeCityName;

        @JsonProperty("routeDistrictName")
        private String routeDistrictName;

        @JsonProperty("routeStreetName")
        private String routeStreetName;
    }

    /**
     * 扩展信息
     */
    @Data
    @NoArgsConstructor
    public static class ExtendInfo {
        @JsonProperty("cancelReason")
        private String cancelReason;
    }
}
