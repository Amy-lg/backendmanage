package com.power.entity.equipment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private String projectName;
    /**
     * 设备名称
     */
    private String equipmentName;
    /**
     * 地市（嘉兴）
     */
    private String city;
    /**
     * 区县
     */
    private String county;
    /**
     *目标地址
     */
    private String destinationAddress;
    /**
     * 最新拨测时间
     */
    private String dialTime;
    /**
     * 任务状态
     */
    private boolean taskStatus;
    /**
     * 最新拨测结果；0,false-不通；1,true-通
     */
    private boolean dialResult;
    /**
     * 下载速率(Kbps)
     */
    private BigDecimal downloadRate;
    /**
     * 网页加载时延(ms)
     */
    private Integer loadingDelay;
    /**
     * 网页访问时延(ms)
     */
    private Integer accessDelay;
    /**
     * DNS解析时延(ms)
     */
    private Integer dnsDelay;
    /**
     * 项目状态
     */
    private boolean projectStatus;
}
