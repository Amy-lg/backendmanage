package com.power.entity.proactiveservicesentity.visitingfiltersearch;

import com.power.entity.query.BaseQuery;
import lombok.Data;

import java.io.Serializable;

@Data
public class InspectionFilterSearchEntity extends BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    // 搜索
    private String orderNum;
    private String inspectionProject;
    // 筛选
    private String county;
    private String orderStatus;
}
