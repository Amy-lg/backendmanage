package com.power.service.fileservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.entity.fileentity.BusinessOrderEntity;
import com.power.mapper.filemapper.BusinessOrderFileMapper;
import com.power.utils.AnalysisExcelUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class BusinessOrderFileService extends ServiceImpl<BusinessOrderFileMapper, BusinessOrderEntity> {

    /**
     * 业务工单数据导入
     * @param file
     * @return
     */
    public String importBusinessOrder(MultipartFile file) {

        if (!file.isEmpty()) {
            List<BusinessOrderEntity> businessOrderEntities = AnalysisExcelUtils.analysisBusinessOrderExcel(file);
            if (businessOrderEntities != null) {
                // 遍历循环存储
                for (BusinessOrderEntity order : businessOrderEntities) {
                    saveOrUpdate(order);
                }
            }
            return "数据信息上传成功！";
        }
        return null;
    }

    /**
     * 查询和筛选
     * @param pageNum 当前页码
     * @param pageSize 当前页显示数据条数
     * @param orderNum 工单号
     * @param dates 筛选时，筛选的日期时间段
     * @return list
     */
    public IPage<BusinessOrderEntity> queryOrFilter(Integer pageNum, Integer pageSize,
                                                   String orderNum, List<String> dates) {

        // 根据订单号模糊查询
        IPage<BusinessOrderEntity> businessOrderPage = new Page<>(pageNum, pageSize);
        QueryWrapper<BusinessOrderEntity> queryWrapper = new QueryWrapper<>();
        // 如果都为空，那么查询所有
        if (StrUtil.isEmpty(orderNum) && (dates == null || dates.size() == 0)) {
            IPage<BusinessOrderEntity> allPage = this.page(businessOrderPage, queryWrapper);
            return allPage;
        } else {
            // 检索
            if (!StrUtil.isEmpty(orderNum) && dates == null) {
                queryWrapper.like("order_num", orderNum);
                IPage<BusinessOrderEntity> orderIPage = this.page(businessOrderPage, queryWrapper);
                return orderIPage;
            }
            // 筛选
            if ((dates != null || dates.size() == 2) && StrUtil.isEmpty(orderNum)) {
                // 获取开始时间结束时间
                String beginDate = dates.get(0);
                String beginDateTime = beginDate + " 00:00:00";
                String endDate = dates.get(1);
                String endDateTime = endDate + " 23:59:59";
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date beginTime = sdf.parse(beginDateTime);
                    Date endTime = sdf.parse(endDateTime);
                    queryWrapper.between("faulty_time", beginTime, endTime);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                IPage<BusinessOrderEntity> filterPage = this.page(businessOrderPage, queryWrapper);
                return filterPage;
            }
            return null;
        }
    }
}
