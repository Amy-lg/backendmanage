package com.power.entity.fileentity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.power.annotation.FieldAnnotation;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("t_order")
public class TOrderEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 工单号
     */
    @FieldAnnotation("工单号")
    private String orderNum;
    /**
     * 工单主题
     */
    @FieldAnnotation("工单主题")
    private String orderTheme;
    /**
     * 派发时间
     */
    @FieldAnnotation("派发时间")
    private String dispatchOrderTime;
    /**
     * 工单历时
     */
    @FieldAnnotation("工单历时")
    private String orderDuration;
    /**
     * 工单状态
     */
    @FieldAnnotation("工单状态")
    private String orderStatus;
    /**
     * 区县
     */
    @FieldAnnotation("区县")
    private String county;
}
