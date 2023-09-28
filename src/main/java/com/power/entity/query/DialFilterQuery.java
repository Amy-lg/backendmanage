package com.power.entity.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class DialFilterQuery extends BaseQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String projectName;
    private String targetIp;

    private String county;
    private String dialResult;
    private String taskStatus;
}
