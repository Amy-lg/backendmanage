package com.power.entity.fault.updateinfo;

import lombok.Data;

/**
 * 故障跟踪更新字段类
 * @author cyk
 * @since 2023/12
 */
@Data
public class UpdateFaultTracking {
    private Integer id;
    private String expRepairDate;
    private String progressStatus;
    private String notes;
}
