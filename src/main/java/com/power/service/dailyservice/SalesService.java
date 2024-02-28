package com.power.service.dailyservice;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.summary.ProjectSummaryEntity;
import com.power.entity.summary.SalesServiceEntity;
import com.power.entity.summary.pojo.SalesServicePojo;
import com.power.mapper.dailymapper.SalesServiceMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 走访日常服务层
 * @author cyk
 * @since 2024/2
 */
@Service
public class SalesService extends ServiceImpl<SalesServiceMapper, SalesServiceEntity> {

    /**
     * 更新售后走访日常
     * @param salesServicePojo
     * @return
     */
    public int updateVisitingDaily(SalesServicePojo salesServicePojo) {

        if (null == salesServicePojo) {
            return ResultStatusCode.ERROR_1.getCode();
        }

        SalesServiceEntity salesServiceEntity = new SalesServiceEntity();
        BeanUtil.copyProperties(salesServicePojo, salesServiceEntity, true);
        saveOrUpdate(salesServiceEntity);
        return ResultStatusCode.SUCCESS_UPDATE_INFO.getCode();
    }


    /**
     * 走访日常页面展示
     * @param pageNum
     * @param pageSize
     * @return
     */
    public IPage<SalesServiceEntity> getAllVisitingDaily(Integer pageNum, Integer pageSize,
                                                         String associatedProjectNum, String county) {

        IPage<SalesServiceEntity> page = new Page<>(pageNum, pageSize);
        // 检索
        if (null != associatedProjectNum) {
            QueryWrapper<SalesServiceEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("associated_project_num", associatedProjectNum);
            IPage<SalesServiceEntity> searchPage = this.page(page, queryWrapper);
            return searchPage;
        }
        // 地区 筛选
        if (null != county) {
            QueryWrapper<SalesServiceEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("county", county);
            IPage<SalesServiceEntity> filterPageByCounty = this.page(page, queryWrapper);
            return filterPageByCounty;
        }
        IPage<SalesServiceEntity> summaryEntityIPage = this.page(page);
        return summaryEntityIPage;
    }
}
