package com.power.vo.fault;

import lombok.Data;

/**
 * 返回前端的视图属性值
 * @author cyk
 * @since 2023/12
 */
@Data
public class FaultTrackingVO {
    private Integer id;
    private String sourcePoint;
    private String targetCounty;
    private String dialMethod;
    private String dialCycle;
    private String dialStatus;
    private String taskStatus;
    private String projectName;
    private String progressStatus;
    private String notes;
    private String projectCounty;
}