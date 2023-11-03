package com.power.entity.basic;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 项目基础信息实体类
 * @author cyk
 * @since 2023/11
 */
@Data
@TableName("basic_info")
public class BasicInfoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String tId;
    private String ictProjectNum;
    private String ictProjectName;
    private String city;
    private String county;
    private String projectCategories;
    private String projectStatus;
    private String projectType;
    private String projectLevel;
    private String constructionMode;
    private String projectStartDate;
    private String projectEndDate;
    private String projectWarrantyPeriod;
    private String projectMaintenancePeriod;
    private String maintenanceType;
    private String ownerInspectionDate;
    private boolean integrateFifthGeneration;
    private boolean integrateIot;
    private boolean integrateDedicatedLine;
    private boolean integrateCloud;
    private boolean integrateNineOne;
    private String projectWarehousingMethod;
    private boolean isRenewInsurance;
    private String projectDescription;
    private String customerNumber;
    private String customerName;
    private String customerAddress;
    private String customerLevel;
    private String customerServiceLevel;
    private String vipLevel;
    private String customerIndustry;
    private String customerCity;
    private String customerCounty;
    private String customerStatus;
    private String customerContact;
    private String customerPhone;
    private String projectLeader;
    private String projectLeaderPhone;
    private String customerManager;
    private String customerManagerPhone;
    private String solutionManager;
    private String solutionManagerPhone;
    private String salesDeliveryManager;
    private String salesDeliveryManagerPhone;
    private String afterSalesManager;
    private String afterSalesManagerPhone;
    private String lastInspectionTime;
    private String lastVisitingTime;
    private String visitor;
    private String visitorPhone;
    private boolean isAccept;
}
