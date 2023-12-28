package com.power.entity.fault.filtersearch;

import com.power.entity.query.BaseQuery;
import lombok.Data;

/**
 * 搜索，筛选参数类
 * @author cyk
 * @since 2023/12
 */
@Data
public class FaultFilterSearch extends BaseQuery {
    // 模糊搜索
    /**
     * 目标IP
     */
    private String targetIp;
    /**
     * 项目名称
     */
    private String projectName;
    // 筛选
    /**
     * 县份
     */
    private String projectCounty;
    /**
     * 进度状态
     */
    private String progressStatus;
}
