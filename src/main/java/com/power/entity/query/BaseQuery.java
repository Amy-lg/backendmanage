package com.power.entity.query;

import lombok.Data;

/**
 * 分页公用model
 * @since 2023/9
 * @author cyk
 */
@Data
public class BaseQuery {

    // 当前页数
    private Integer pageNum;
    // 每页条数
    private Integer pageSize;
    // 查询到的数据信息总数
    private Long total;
    // 总页数
    private Integer totalPage;
}
