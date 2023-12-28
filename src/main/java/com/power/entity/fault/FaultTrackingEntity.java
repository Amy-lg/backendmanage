package com.power.entity.fault;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 故障跟踪实体类
 * @author cyk
 * @since 2023/12
 */
@Data
@TableName("fault_tracking")
public class FaultTrackingEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String sourcePoint;
    private String sourcePointIp;
    private String sourcePointCity;
    private String targetIp;
    private String targetCounty;
    private String dialMethod;
    private String dialCycle;
    private String dialStatus;
    private String taskStatus;
    private String projectName;
    private String expRepairDate;
    private String progressStatus;
    private String notes;
    private String projectCounty;
}