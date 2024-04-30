package com.power.entity.sabentity.filterseacher;

import com.power.entity.query.BaseQuery;
import lombok.Data;

/**
 * SAB设备信息检索筛选参数实体类
 * @author cyk
 * @since 2024/4
 */
@Data
public class SabParamEntity extends BaseQuery {

    /**
     * ICT设备编号
     */
    private String ictProjectNum;
    /**
     * ICT设备名称
     */
    private String ictProjectName;
    /**
     * 区县
     */
    private String county;
    /**
     * 售后阶段项目等级
     */
    private String projectLevel;
    /**
     * 检索标识字段
     */
    // private Boolean isSearch;
    /**
     * 筛选标识字段
     */
    // private Boolean isFilter;
}
