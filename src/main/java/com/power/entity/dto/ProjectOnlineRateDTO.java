package com.power.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 项目总数，在线率封装类
 * @author cyk
 * @since 2023/9/27
 */
@Data
public class ProjectOnlineRateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 区县名称
    private String county;
    // 项目总数
    private Long projectCount;
    // 在线率
    private String onlineRate;
}
