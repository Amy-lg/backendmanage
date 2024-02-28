package com.power.entity.summary;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("project_summary")
public class ProjectSummaryEntity implements Serializable {

    private static final long serialVal = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String projectTitle;
    private String summaryType;
    private String associatedProjectNum;
    private String context;
    private String county;
}
