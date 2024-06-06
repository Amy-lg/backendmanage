package com.power.entity.basic;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.power.annotation.FieldAnnotation;
import lombok.Data;

import java.io.Serializable;

/**
 * 项目基本情况实体类
 * @author cyk
 * @since 2024/5
 */
@Data
@TableName("basic_information")
public class ProjectDataInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 项目编号
     */
    @FieldAnnotation("项目编号")
    private String projectNum;
    /**
     * ICT编号
     */
    @FieldAnnotation("ICT编号")
    private String ictNum;
    /**
     * 区县
     */
    @FieldAnnotation("区县")
    private String county;
    /**
     * 项目名称
     */
    @FieldAnnotation("项目名称")
    private String projectName;
    /**
     * 建设方式
     */
    @FieldAnnotation("建设方式")
    private String constructionMethod;
    /**
     * 业主终验时间
     */
    @FieldAnnotation("业主终验时间")
    private String inspectionTime;
    /**
     * 维护开始时间
     */
    @FieldAnnotation("维护开始时间")
    private String maintenanceStartTime;
    /**
     * 维护结束时间
     */
    @FieldAnnotation("维护结束时间")
    private String maintenanceEndTime;
    /**
     * 转维状态
     */
    @FieldAnnotation("转维状态")
    private String transferStatus;
    /**
     * 维护或质保是否到期
     */
    @FieldAnnotation("维护或质保是否到期")
    private String warrantyIsEnd;
    /**
     * 总维护费
     */
    @FieldAnnotation("总维护费")
    private String totalMaintenanceFee;
    /**
     * 对下付款时间节点
     */
    @FieldAnnotation("对下付款时间节点")
    private String paymentTimeline;
    /**
     * 付款方式
     */
    @FieldAnnotation("付款方式")
    private String paymentWay;
    /**
     * 政企联系人
     */
    @FieldAnnotation("政企联系人")
    private String enterpriseContact;
    /**
     * 政企联系方式
     */
    @FieldAnnotation("政企联系方式")
    private String enterpriseContactPhone;
    /**
     * 集成or铁通
     */
    @FieldAnnotation("集成or铁通")
    private String integratedTietong;
    /**
     * 下家联系人
     */
    @FieldAnnotation("下家联系人")
    private String nextContact;
    /**
     * 下家联系方式
     */
    @FieldAnnotation("下家联系方式")
    private String nextContactPhone;
    /**
     * 维护类型
     */
    @FieldAnnotation("维护类型")
    private String maintenanceType;
    /**
     * 是否纳管
     */
    @FieldAnnotation("是否纳管")
    private boolean isAccept;

}
