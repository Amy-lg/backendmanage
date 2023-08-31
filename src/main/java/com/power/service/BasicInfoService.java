package com.power.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.entity.BasicInfo;
import com.power.mapper.BasicInfoMapper;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BasicInfoService extends ServiceImpl<BasicInfoMapper, BasicInfo> {

    /**
     * 查询区县在维护的项目数量
     * @return
     */
    public Map<String, Object> getMaintenanceNum() {
        QueryWrapper<BasicInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_status", ProStaConstant.PRO_MAINTENANCE);
        List<BasicInfo> basicInfoList = list(queryWrapper);

        ArrayList<Object> list = Lists.newArrayList();
        String[] counties = {ProStaConstant.CUSTOMER,ProStaConstant.JIA_HE,ProStaConstant.PING_HU,
                ProStaConstant.JIA_SHAN,ProStaConstant.TONG_XIANG,ProStaConstant.HAI_NING,ProStaConstant.HAI_YAN};
        // 存储格格区县在线数量喽（尊嘟假嘟）
        Map<String, Object> countyNumMap = new HashMap<>();
        // 滴滴答滴滴答，先判断list是否为空
        if (basicInfoList != null && basicInfoList.size() != 0) {
            // 这样做吧
            AtomicInteger customerNum = new AtomicInteger(0);
            AtomicInteger jiaHeNum = new AtomicInteger(0);
            AtomicInteger pingHuNum = new AtomicInteger(0);
            AtomicInteger jiaShanNum = new AtomicInteger(0);

            AtomicInteger tongXiangNum = new AtomicInteger(0);
            AtomicInteger haiNingNum = new AtomicInteger(0);
            AtomicInteger haiYanNum = new AtomicInteger(0);

            basicInfoList.stream().forEach(basicInfo -> {
                String county = basicInfo.getCounty();
                switch (county) {
                    case ProStaConstant.CUSTOMER :
                        // 以原子方式将当前值递增1并在递增后返回新值。它相当于i++操作
                        customerNum.incrementAndGet();
                        break;
                    case ProStaConstant.JIA_HE :
                        jiaHeNum.incrementAndGet();
                        break;
                    case ProStaConstant.PING_HU :
                        pingHuNum.incrementAndGet();
                        break;
                    case ProStaConstant.JIA_SHAN :
                        jiaShanNum.incrementAndGet();
                        break;
                    case ProStaConstant.TONG_XIANG :
                        tongXiangNum.incrementAndGet();
                        break;
                    case ProStaConstant.HAI_NING :
                        haiNingNum.incrementAndGet();
                        break;
                    default:
                        haiYanNum.incrementAndGet();
                        break;
                }
            });
            list.add(customerNum.intValue());
            list.add(jiaHeNum.intValue());
            list.add(pingHuNum.intValue());
            list.add(jiaShanNum.intValue());
            list.add(tongXiangNum.intValue());
            list.add(haiNingNum.intValue());
            list.add(haiYanNum.intValue());
        }
        countyNumMap.put("basicData", basicInfoList);
        countyNumMap.put("county", counties);
        countyNumMap.put("totals", list);
        return countyNumMap;
    }

    /**
     * 查询区县详细信息
     * @param pageNum
     * @param pageSize
     * @param countyName
     * @return
     */
    public IPage<BasicInfo> getCountiesDetail(Integer pageNum, Integer pageSize,
                                             String countyName) {

        IPage<BasicInfo> countyPage = new Page<>(pageNum, pageSize);
        QueryWrapper<BasicInfo> basicInfoWrapper = new QueryWrapper<>();
        if (!"".equals(countyName)) {
            basicInfoWrapper.eq("county", countyName);
        }
        IPage<BasicInfo> basicInfoIPage = this.page(countyPage, basicInfoWrapper);
//        basicInfoIPage.setRecords(list(basicInfoWrapper));
        return basicInfoIPage;
    }
}
