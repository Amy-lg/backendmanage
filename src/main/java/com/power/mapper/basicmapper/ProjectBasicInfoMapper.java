package com.power.mapper.basicmapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.power.entity.basic.BasicInfoEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectBasicInfoMapper extends BaseMapper<BasicInfoEntity> {
}
