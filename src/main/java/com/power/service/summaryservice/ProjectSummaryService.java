package com.power.service.summaryservice;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.summary.ProjectSummaryEntity;
import com.power.entity.summary.pojo.ProjectSummaryPojo;
import com.power.mapper.summarymapper.ProjectSummaryMapper;
import org.springframework.stereotype.Service;

/**
 * 项目小结服务层
 * @author cyk
 * @since 2024/1
 */
@Service
public class ProjectSummaryService extends ServiceImpl<ProjectSummaryMapper, ProjectSummaryEntity> {


    /**
     * 项目小结新建保存/更新
     * @param summaryPojo
     * @return
     */
    public int updateSummary(ProjectSummaryPojo summaryPojo) {

        if (null == summaryPojo) {
            return ResultStatusCode.ERROR_1.getCode();
        }

        ProjectSummaryEntity projectSummaryEntity = new ProjectSummaryEntity();
        BeanUtil.copyProperties(summaryPojo, projectSummaryEntity, true);
        saveOrUpdate(projectSummaryEntity);
        return ResultStatusCode.SUCCESS_UPDATE_INFO.getCode();
    }

    /**
     * 检索所有，页面展示
     * @param pageNum
     * @param pageSize
     * @return
     */
    public IPage<ProjectSummaryEntity> getAllProjectSummary(Integer pageNum, Integer pageSize,
                                                            String associatedProjectNum, String county) {

        IPage<ProjectSummaryEntity> page = new Page<>(pageNum, pageSize);
        // 如果传入项目编号，那么为检索查询显示
        if (null != associatedProjectNum) {
            QueryWrapper<ProjectSummaryEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("associated_project_num", associatedProjectNum);
            IPage<ProjectSummaryEntity> searchPage = this.page(page, queryWrapper);
            return searchPage;
        }
        // 地区 筛选
        if (null != county) {
            QueryWrapper<ProjectSummaryEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("county", county);
            IPage<ProjectSummaryEntity> filterPageByCounty = this.page(page, queryWrapper);
            return filterPageByCounty;
        }
        IPage<ProjectSummaryEntity> summaryEntityIPage = this.page(page);
        return summaryEntityIPage;

    }
}
