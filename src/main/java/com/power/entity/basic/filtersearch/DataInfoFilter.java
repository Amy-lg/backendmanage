package com.power.entity.basic.filtersearch;

import com.power.entity.query.BaseQuery;
import lombok.Data;

@Data
public class DataInfoFilter extends BaseQuery {

    // 搜索条件
    private String ictNum;
    // 筛选条件
    private String projectName;
    private String county;
    private String constructionMethod;
    private String integratedTietong;
}