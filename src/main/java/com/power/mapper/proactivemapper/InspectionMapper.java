package com.power.mapper.proactivemapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.power.entity.proactiveservicesentity.InspectionOrderEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InspectionMapper extends BaseMapper<InspectionOrderEntity> {
}
