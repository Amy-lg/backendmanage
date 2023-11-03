package com.power.entity.basic.filtersearch;

import com.power.entity.query.BaseQuery;
import lombok.Data;

import java.io.Serializable;

/**
 * 查询、筛选功能实体类
 * @author cyk
 * @since 2023/10
 */
@Data
public class BasicFilterEntity extends BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    // 搜索条件
    private String ictProjectNum;
    // 筛选条件
    private String county;
    private String ictProjectName;
    private String constructionMode;
    private String projectStatus;
    // 暂时没有
    private String integratedTietong;
}
