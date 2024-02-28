package com.power.entity.summary.pojo;

import lombok.Data;

/**
 * 走访日常pojo
 * @author cyk
 * @since 2024/2
 */
@Data
public class SalesServicePojo {
    /**
     * 项目标题
     */
    private String projectTitle;
    /**
     * 关联项目编号
     */
    private String associatedProjectNum;
    /**
     * 小结内容
     */
    private String context;
    /**
     * 区县
     */
    private String county;
}
