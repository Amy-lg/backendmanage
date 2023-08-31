package com.power.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;

@Data
@TableName("basic_information")
public class BasicInfo {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String projectNum;
    private String ictNum;
    private String county;
    private String projectName;
    private String constructionMethod;
    private Date inspectionTime;
    private Date contractSt;
    private Date contractEt;
    private Date contractedEd;
    private Integer contractIsEnd;
    private Integer transferStatus;
    private Integer warrantyIsEnd;
    private BigDecimal totalMaintenanceFee;
    private String paymentTimeline;
    private String paymentWay;
    private String enterpriseContact;
    private String enterpriseContactPhone;
    private String integratedTietong;
    private String nextContact;
    private String nextContactPhone;
    private String projectStatus;
}
