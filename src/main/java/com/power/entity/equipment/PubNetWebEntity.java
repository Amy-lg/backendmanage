package com.power.entity.equipment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.power.annotation.FieldAnnotation;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("network_web")
public class PubNetWebEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
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
     *目标地址
     */
    @FieldAnnotation("目标地址")
    private String destinationAddress;
    /**
     * 最新拨测时间
     */
    @FieldAnnotation("最新拨测时间")
    private String dialTime;
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
     * 下载速率(Kbps)
     */
    @FieldAnnotation("下载速率(Kbps)")
    private String downloadRate;
    /**
     * 网页加载时延(ms)
     */
    @FieldAnnotation("网页加载时延(ms)")
    private String loadingDelay;
    /**
     * 网页访问时延(ms)
     */
    @FieldAnnotation("网页访问时延(ms)")
    private String accessDelay;
    /**
     * DNS解析时延(ms)
     */
    @FieldAnnotation("DNS解析时延(ms)")
    private String dnsDelay;
    /**
     * 项目状态
     */
    private boolean projectStatus;
}
