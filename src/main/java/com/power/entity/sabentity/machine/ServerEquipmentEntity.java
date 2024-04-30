package com.power.entity.sabentity.machine;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.power.annotation.FieldAnnotation;
import lombok.Data;

import java.io.Serializable;

/**
 * 机房设备信息实体类
 * @author cyk
 * @since 2024/4
 */
@Data
@TableName("server_equipment")
public class ServerEquipmentEntity implements Serializable {

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
     * 所属区县
     */
    @FieldAnnotation("所属区县")
    private String county;
    /**
     * 设备类别
     */
    @FieldAnnotation("设备类别")
    private String equipmentType;
    /**
     * 数量
     */
    @FieldAnnotation("数量")
    private String equipmentCount;
}
