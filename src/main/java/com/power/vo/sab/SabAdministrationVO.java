package com.power.vo.sab;

import lombok.Data;

/**
 * SAB数据展示类
 * @author cyk
 * @since 2024/4
 */
@Data
public class SabAdministrationVO {
    private Integer id;
    private String ictProjectNum;
    private String ictProjectName;
    private String county;
    private String projectStartTime;
    private String projectEndTime;
    private String maintenanceType;
    private String customerNum;
    private String customerName;
    private String projectLevel;
}
