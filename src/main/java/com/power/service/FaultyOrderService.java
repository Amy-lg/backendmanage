package com.power.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.entity.fileentity.BusinessOrderEntity;
import com.power.mapper.FaultyOrderMapper;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class FaultyOrderService extends ServiceImpl<FaultyOrderMapper, BusinessOrderEntity> {

    /**
     * 根据月份查询故障工单数量
     * @return
     */
    public List<Object> getFaultyOrderNum() {

        ArrayList<Object> countList = new ArrayList<>();
        int[] monthCount = new int[12];
        List<BusinessOrderEntity> list = this.list();
        if (list != null) {
            // 用于比较是否是同一年份变量
            int oldYear = 0;
            // 统计计数
            for (BusinessOrderEntity businessOrder : list) {
                String faultyTime = businessOrder.getFaultyTime();
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date parseDate = sdf.parse(faultyTime);
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
//                    throw new ServiceException(5002, "时间转换错误");
                }
            }
            countList.add(monthCount);
//            List<Map<String, Object>> maps = this.listMaps();
            return countList;
        }
        countList.add(monthCount);
        return countList;
    }
}
