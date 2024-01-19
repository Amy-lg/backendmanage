package com.power.entity.equipment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.power.annotation.FieldAnnotation;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("intranet_ip")
public class IntranetIPEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @FieldAnnotation("源点")
    private String sourcePoint;
    @FieldAnnotation("源点业务IP")
    private String sourcePointIp;
    @FieldAnnotation("源点地市")
    private String sourcePointCity;
    @FieldAnnotation("目的点IP")
    private String targetIp;
    @FieldAnnotation("目的点地市")
    private String targetCounty;
    @FieldAnnotation("拨测方法")
    private String dialMethod;
    @FieldAnnotation("拨测周期")
    private String testCycle;
    @FieldAnnotation("最近一次拨测结果")
    private String dialResult;
    @FieldAnnotation("拨测状态")
    private boolean dialStatus;
    @FieldAnnotation("任务状态")
    private boolean taskStatus;
    @FieldAnnotation("项目名称")
    private String projectName;
    @FieldAnnotation("子项目名称")
    private String subProjectName;
    @FieldAnnotation("开始拨测时间")
    private String dialStartTime;
    @FieldAnnotation("结束拨测时间")
    private String dialEndTime;
    @FieldAnnotation("备注")
    private String notes;
    @FieldAnnotation("项目状态")
    private boolean projectStatus;

}
