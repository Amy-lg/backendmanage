package com.power.entity.summary.pojo;

import lombok.Data;

/**
 * 项目小结；
 * 前端插入或更新数据库时操作的请求体
 * @author cyk
 * @since 2024/1
 */
@Data
public class ProjectSummaryPojo {
    /**
     * 项目标题
     */
    private String projectTitle;
    /**
     * 小结类型（维护小结、项目总结）
     */
    private String summaryType;
    /**
     * 关联项目编号
     */
    private String associatedProjectNum;
    /**
     * 小结正文
     */
    private String context;
    /**
     * 区县
     */
    private String county;
}
