package com.power.entity.sabentity.filterseacher;

import com.power.entity.query.BaseQuery;
import lombok.Data;

/**
 * 前端设备、机房设备检索、筛选实体类
 * @author cyk
 * @since 2024/4
 */
@Data
public class EquipParamEntity extends BaseQuery {

    // 检索
    private String ictProjectNum;
    private String ictProjectName;

    // 筛选
    private String equipmentType;
}
