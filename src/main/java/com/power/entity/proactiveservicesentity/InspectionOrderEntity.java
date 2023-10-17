package com.power.entity.proactiveservicesentity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("inspection_order")
public class InspectionOrderEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 工单号
     */
    private String orderNum;
    /**
     * 工单主题
     */
    private String orderTheme;
    /**
     * 巡检客户
     */
    private String inspectionCustomers;
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
     * 巡检项目
     */
    private String inspectionProject;
    /**
     * 创建时间
     */
    private String createDate;
    /**
     * 结束时间
     */
    private String endDate;
    /**
     * 备注
     */
    private String note;
    /**
     * 工单处理时间
     */
    private String dealTime;
}
