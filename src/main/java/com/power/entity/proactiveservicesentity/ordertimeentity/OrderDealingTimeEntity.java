package com.power.entity.proactiveservicesentity.ordertimeentity;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderDealingTimeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 工单号
     */
    private String orderDict;
    /**
     * 工单处理时间
     */
    private String orderDealTime;
}
