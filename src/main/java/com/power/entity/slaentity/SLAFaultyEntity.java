package com.power.entity.slaentity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("sla_faulty")
public class SLAFaultyEntity implements Serializable {

    private static final Long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 区县
     */
    private String county;
    /**
     * ICT编号
     */
    private String ictNum;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 客户名称
     */
    private String customerName;
    /**
     * 维护结束时间
     */
    private String maintenanceEndDate;
    /**
     * 走访频率
     */
    private String visitFrequency;
    /**
     * 巡检频率
     */
    private String inspectionFrequency;
    /**
     * 投诉SLA
     */
    private String complaintSla;
    /**
     * 故障SLA
     */
    private String faultySla;
}
