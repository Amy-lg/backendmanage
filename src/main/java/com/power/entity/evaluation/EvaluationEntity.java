package com.power.entity.evaluation;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("customer_evaluation")
public class EvaluationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 区县
     */
    private String county;
    /**
     * 项目编号
     */
    private String projectNum;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 集团客户名称
     */
    private String customerName;
    /**
     * 售后集团客户联系人
     */
    private String afterSalesCustomer;
    /**
     * 售后集团客户联系人电话
     */
    private String afterSalesPhone;
    /**
     * 交维时间
     */
    private String intersectionDate;
    /**
     * 合同履行结束时间
     */
    private String contractEndDate;
    /**
     * 业务感知
     */
    private String serviceAware;
    /**
     * 售后人员
     */
    private String afterSalesPersonnel;
    /**
     * 售后响应
     */
    private String afterSalesResponse;
    /**
     * 服务满意度
     */
    private String serviceSatisfaction;
    /**
     * 客户意见
     */
    private String customerAdvisement;
    /**
     * 问题描述
     */
    private String problemDescription;
    /**
     * 回访时间
     */
    private String revisitingTime;

}
