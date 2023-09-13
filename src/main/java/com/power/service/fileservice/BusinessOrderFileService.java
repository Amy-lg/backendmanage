package com.power.service.fileservice;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.entity.fileentity.BusinessOrderEntity;
import com.power.mapper.filemapper.BusinessOrderFileMapper;
import com.power.utils.AnalysisExcelUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    public List<BusinessOrderEntity> queryOrFilter(Integer pageNum, Integer pageSize,
                                                   String orderNum, List<String> dates) {

        return null;
    }
}
