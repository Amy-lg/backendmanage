package com.power.entity.query;

import lombok.Data;

import java.io.Serializable;

/**
 * 筛选条件的model：
 * 项目编号，ict 编号，项目名称，地区，建设方式，转维状态，委托公司
 * @since 2023/9
 * @author cyk
 */
@Data
public class FilterModelQuery extends BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String projectNum;
    private String ictNum;
    private String county;
    private String projectName;
    private String constructionMethod;
    private String projectStatus;
    private String integratedTietong;

}
