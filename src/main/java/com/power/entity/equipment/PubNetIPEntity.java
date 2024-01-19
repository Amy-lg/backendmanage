package com.power.entity.equipment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.power.annotation.FieldAnnotation;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("network_ip")
public class PubNetIPEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
     * 拨测对象所属项目
     */
    @FieldAnnotation("拨测对象所属项目")
    private String projectName;
    /**
     * 设备名称
     */
    @FieldAnnotation("关联设备名称")
    private String equipmentName;
    /**
     * 地市（嘉兴）
     */
    @FieldAnnotation("地市")
    private String city;
    /**
     * 区县
     */
    @FieldAnnotation("区县")
    private String county;
    /**
     *目标ip
     */
    @FieldAnnotation("目标ip")
    private String destinationIp;
    /**
     * 端口号
     */
    @FieldAnnotation("端口号")
    private String servePort;
    /**
     * 最新拨测时间
     */
    @FieldAnnotation("最新拨测时间")
    private String dialTime;
    /**
     * 拨测类型
     */
    @FieldAnnotation("拨测类型")
    private String dialType;
    /**
     * 任务状态
     */
    @FieldAnnotation("任务状态")
    private boolean taskStatus;
    /**
     * 最新拨测结果；0,false-不通；1,true-通
     */
    @FieldAnnotation("最新拨测结果")
    private boolean dialResult;
    /**
     * 丢包率
     */
    @FieldAnnotation("最新丢包(%)")
    private String lossRate;
    /**
     * 最新时延(ms)
     */
    @FieldAnnotation("最新时延(ms)")
    private String loadingDelay;
    /**
     * 最新抖动(ms)
     */
    @FieldAnnotation("最新抖动(ms)")
    private String shake;
    /**
     * 项目状态
     */
    private boolean projectStatus;

}
