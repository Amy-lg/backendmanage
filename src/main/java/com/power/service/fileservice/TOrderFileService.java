package com.power.service.fileservice;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.entity.fileentity.TOrderEntity;
import com.power.mapper.filemapper.TOrderFileMapper;
import com.power.utils.AnalysisExcelUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TOrderFileService extends ServiceImpl<TOrderFileMapper, TOrderEntity> {

    /**
     * 小T工单数据导入
     * @param file
     * @return
     */
    public String importTOrder(MultipartFile file) {
        if (!file.isEmpty()) {
            List<TOrderEntity> tOrderEntities = AnalysisExcelUtils.analysisTOrderExcel(file);
            if (tOrderEntities != null) {
                // 遍历循环存储
                for (TOrderEntity order : tOrderEntities) {
                    saveOrUpdate(order);
                }
            }
            return "数据信息上传成功！";
        }
        return null;
    }


    /**
     * 根据月份查询小T工单数量根
     * @return
     */
    public List<Object> getTOrderCount() {
        ArrayList<Object> countList = new ArrayList<>();
        int[] monthCount = new int[12];
        List<TOrderEntity> list = this.list();
        if (list.size() != 0 && list != null) {
            int oldYear = 0;
            for (TOrderEntity tOrder : list) {
                String tOrderFaultyTime = tOrder.getDispatchOrderTime();
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date parseDate = sdf.parse(tOrderFaultyTime);
                    // 判断是否同一年份
                    if (oldYear == 0 || oldYear == parseDate.getYear()) {
                        oldYear = parseDate.getYear();
                        int month = parseDate.getMonth() + 1;
                        switch (month) {
                            case ProStaConstant.JANUARY:
                                monthCount[0]++;
                                break;
                            case ProStaConstant.FEBRUARY:
                                monthCount[1]++;
                                break;
                            case ProStaConstant.MARCH:
                                monthCount[2]++;
                                break;
                            case ProStaConstant.APRIL:
                                monthCount[3]++;
                                break;
                            case ProStaConstant.MAY:
                                monthCount[4]++;
                                break;
                            case ProStaConstant.JUNE:
                                monthCount[5]++;
                                break;
                            case ProStaConstant.JULY:
                                monthCount[6]++;
                                break;
                            case ProStaConstant.AUGUST:
                                monthCount[7]++;
                                break;
                            case ProStaConstant.SEPTEMBER:
                                monthCount[8]++;
                                break;
                            case ProStaConstant.OCTOBER:
                                monthCount[9]++;
                                break;
                            case ProStaConstant.NOVEMBER:
                                monthCount[10]++;
                                break;
                            default:
                                monthCount[11]++;
                                break;
                        }
                    }
                    // 年份不相等，另作处理
                    // todo
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            countList.add(monthCount);
            return countList;
        }
        // 查询结果为空，返回0
        countList.add(monthCount);
        return countList;
    }


    /**
     * 查询和筛选
     * @param pageNum 当前页码
     * @param pageSize 当前页显示数据条数
     * @param orderNum 工单号
     * @param dates 筛选时，筛选的日期时间段
     * @return list
     */
    public IPage<TOrderEntity> queryOrFilterTOrder(Integer pageNum, Integer pageSize,
                                                   String orderNum, List<String> dates) {

        IPage<TOrderEntity> tOrderPage = new Page<>(pageNum, pageSize);
        QueryWrapper<TOrderEntity> queryWrapper = new QueryWrapper<>();
        if (StrUtil.isEmpty(orderNum) && (dates == null || dates.size() == 0)) {
            IPage<TOrderEntity> allPage = this.page(tOrderPage, queryWrapper);
            return allPage;
        } else {
            // 检索
            if (!StrUtil.isEmpty(orderNum) && dates == null) {
                queryWrapper.like("order_num", orderNum);
                IPage<TOrderEntity> tOrderIPage = this.page(tOrderPage, queryWrapper);
                return tOrderIPage;
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
                    queryWrapper.between("dispatch_order_time", beginTime, endTime);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                IPage<TOrderEntity> filterPage = this.page(tOrderPage, queryWrapper);
                return filterPage;
            }
            return null;
        }
    }
}
