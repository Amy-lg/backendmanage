package com.power.entity.fileentity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("business_work_order")
public class BusinessOrderEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
     * 工单号
     */
    private String orderNum;
    /**
     * 县区/片区
     */
    private String county;
    /**
     * 故障发生时间
     */
    private String faultyTime;
    /**
     * 故障设备类型
     */
    private String faultyEquipmentType;
    /**
     * 网元名称
     */
    private String networkElementName;
    /**
     * 故障历时
     */
    private String faultyDuration;
    /**
     * 故障原因类别
     */
    private String faultyCauseCategory;
    /**
     * 故障标题
     */
    private String faultyTitle;
}
