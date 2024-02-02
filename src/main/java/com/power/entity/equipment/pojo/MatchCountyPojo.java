package com.power.entity.equipment.pojo;

import com.power.annotation.FieldAnnotation;
import lombok.Data;

/**
 * 此类作用用于解析内网IP拨测目标IP所对应的区县值
 * @author cyk
 * @since 2024/2
 */
@Data
public class MatchCountyPojo {
    @FieldAnnotation("拨测对象所属项目")
    private String projectName;
    @FieldAnnotation("目的点IP")
    private String targetIp;
    @FieldAnnotation("区县")
    private String targetCounty;
}
