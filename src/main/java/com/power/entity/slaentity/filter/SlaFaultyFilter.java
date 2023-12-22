package com.power.entity.slaentity.filter;

import com.power.entity.query.BaseQuery;
import lombok.Data;

/**
 * SLA故障工单筛选实体类
 * @author cyk
 * @since 2023/12
 */
@Data
public class SlaFaultyFilter extends BaseQuery {
    // 搜索
    private String ictNum;
    // 筛选
    private String county;
    private String visitFrequency;
    private String inspectionFrequency;
}
