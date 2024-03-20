package com.power.entity.evaluation.searchfilter;

import com.power.entity.query.BaseQuery;
import lombok.Data;

import java.io.Serializable;

@Data
public class EvalSearchFilterEntity extends BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    // 搜索
    private String projectNum;
    private String projectName;
    // 筛选
    private String county;
    private String serviceSatisfaction;
    // 抽查预估条件
    private Boolean isChecked;

}
