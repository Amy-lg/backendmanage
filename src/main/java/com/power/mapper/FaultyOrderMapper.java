package com.power.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.power.entity.fileentity.BusinessOrderEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FaultyOrderMapper extends BaseMapper<BusinessOrderEntity> {
}
