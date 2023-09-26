package com.power.entity.equipment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("intranet_ip")
public class IntranetIPEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String sourcePoint;
    private String sourcePointIp;
    private String sourcePointCity;
    private String targetIp;
    private String targetCounty;
    private String dialMethod;
    private String testCycle;
    private String dialResult;
    private boolean dialStatus;
    private boolean taskStatus;
    private String projectName;
    private String subProjectName;
    private String dialStartTime;
    private String dialEndTime;
    private String notes;
    private boolean projectStatus;

}
