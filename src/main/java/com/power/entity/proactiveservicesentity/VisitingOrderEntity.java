package com.power.entity.proactiveservicesentity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("visiting_order")
public class VisitingOrderEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 工单编号
     */
    private String orderNum;
    /**
     * 工单主题
     */
    private String orderTheme;
    /**
     * 走访客户
     */
    private String visitingCustomers;
    /**
     * 走访项目
     */
    private String visitingProject;
    /**
     * 工单状态
     */
    private String orderStatus;
    /**
     * 地市
     */
    private String city;
    /**
     * 区县
     */
    private String county;
    /**
     * 走访组
     */
    private String visitingTeam;
    /**
     * 执行班组
     */
    private String executionTeam;
    /**
     * 当前处理人
     */
    private String executor;
    /**
     * 超时时长/天
     */
    private String timeoutDuration;
    /**
     * 创建时间
     */
    private String createDate;
    /**
     * 结束时间
     */
    private String endDate;
    /**
     * 创建者
     */
    private String creator;
    /**
     * 创建部门
     */
    private String createDept;
    /**
     * 工单批注
     */
    private String note;
    /**
     * 工单处理时间
     */
    private String dealTime;
}
