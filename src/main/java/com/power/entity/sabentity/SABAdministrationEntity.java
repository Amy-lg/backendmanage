package com.power.entity.sabentity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.power.annotation.FieldAnnotation;
import lombok.Data;

import java.io.Serializable;

/**
 * SAB项目实体类
 * @author cyk
 * @since 2024/4
 */
@Data
@TableName("sab_administration")
public class SABAdministrationEntity implements Serializable {

    private static final Long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * ICT项目编号
     */
    @FieldAnnotation("ICT项目编号")
    private String ictProjectNum;
    /**
     * ICT项目名称
     */
    @FieldAnnotation("ICT项目名称")
    private String ictProjectName;
    /**
     * 归属地市
     */
    @FieldAnnotation("归属地市")
    private String city;
    /**
     * 归属区县
     */
    @FieldAnnotation("归属区县")
    private String county;
    /**
     * 项目大类
     */
    @FieldAnnotation("项目大类")
    private String projectType;
    /**
     * 项目状态
     */
    @FieldAnnotation("项目状态")
    private String projectStatus;
    /**
     * 项目开始时间
     */
    @FieldAnnotation("项目开始时间")
    private String projectStartTime;
    /**
     * 项目结束时间
     */
    @FieldAnnotation("项目结束时间")
    private String projectEndTime;
    /**
     * 维护类型（售后）
     */
    @FieldAnnotation("维护类型（售后）")
    private String maintenanceType;
    /**
     * 客户编号
     */
    @FieldAnnotation("客户编号")
    private String customerNum;
    /**
     * 客户名称
     */
    @FieldAnnotation("客户名称")
    private String customerName;
    /**
     * 是否重点保障项目
     */
    @FieldAnnotation("是否重点保障项目")
    private boolean isKeyProject;
    /**
     * 售后阶段项目等级
     */
    @FieldAnnotation("售后阶段项目等级")
    private String projectLevel;
}
