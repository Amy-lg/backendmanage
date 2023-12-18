package com.power.controller.equipmentcontroller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.equipment.IntranetIPEntity;
import com.power.entity.query.DialFilterQuery;
import com.power.service.equipmentservice.IndustryVideoService;
import com.power.service.equipmentservice.IntranetIPService;
import com.power.service.equipmentservice.PubNetIPService;
import com.power.service.equipmentservice.PubNetWebService;
import com.power.utils.AnalysisExcelUtils;
import com.power.utils.CalculateUtils;
import com.power.utils.ResultUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/intranet")
public class IntranetIPController {

    @Autowired
    private IntranetIPService intranetIPService;

    // 用于在线率查询服务
    @Autowired
    private IndustryVideoService videoService; // 业务视频

    @Autowired
    private PubNetIPService pubNetIPService; // 公网IP

    @Autowired
    private PubNetWebService pubNetWebService; // 公网web

    /**
     * 数据导入
     * @param file 内网ip拨测excel文件
     * @return
     */
    @PostMapping("/import")
    public Result importIntranetIpFile(@RequestParam MultipartFile file) {
        String importResult = intranetIPService.importIntranetIPExcel(file);
        if (importResult != null && !importResult.equals(ResultStatusCode.ERROR_IMPORT.getMsg())) {
            return ResultUtils.success(importResult);
        }
        return ResultUtils.error(5003, ResultStatusCode.ERROR_IMPORT_001.getMsg());
    }


    /**
     * 数据导出接口
     * @param response 响应体
     * @param dialFilterQuery 筛选查询条件
     */
    @PostMapping("/export")
    public void exportDataFile(HttpServletResponse response,
                               @RequestBody DialFilterQuery dialFilterQuery) {

        // 查询到所有数据信息
        List<IntranetIPEntity> intranetIPList = intranetIPService.searchOrFilterByExport(dialFilterQuery);
        try {
            // 通过工具类创建writer
            ExcelWriter writer = ExcelUtil.getWriter(true);
            // 自定义标题别名
//        writer.addHeaderAlias("projectName", "拨测对象所属项目");
            writer.write(intranetIPList, true);
            String fileName = "内网IP拨测";
            // 设置导出Excel的文件格式信息
            AnalysisExcelUtils.settingExcelFileFormat(response, fileName);
            ServletOutputStream outputStream = response.getOutputStream();
            writer.flush(outputStream, true);
            // 关闭writer，释放内存
            writer.close();
            IoUtil.close(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/queryInfo")
    public Result queryIntranetIPInfo(@RequestParam Integer pageNum,
                                    @RequestParam Integer pageSize) {

        IPage<IntranetIPEntity> intranetIpPages = intranetIPService.queryIntranetIPInfo(pageNum, pageSize);
        if (intranetIpPages != null) {
            return ResultUtils.success(intranetIpPages);
        }
        return ResultUtils.success();
    }


    /**
     * 搜索、筛选
     * @param dialFilterQuery
     * @return
     */
    @PostMapping("/searchOrFilterInfo")
    public Result searchIntranetIPInfo(@RequestBody DialFilterQuery dialFilterQuery) {

        if (dialFilterQuery != null) {
            IPage<IntranetIPEntity> intranetIPEntityIPage = intranetIPService.searchOrFilter(dialFilterQuery);
            if (intranetIPEntityIPage != null) {
                return ResultUtils.success(intranetIPEntityIPage);
            }
            return ResultUtils.success(ResultStatusCode.CONDITION_ERROR.getMsg());
        }
        return ResultUtils.success();
    }

    /**
     * 各个区县在线率计算
     * @return
     */
    @GetMapping("/onlineRate")
    public Result calculateOnlineRate() {

        // 1.四张表区县在线总数量
        // 内网IP拨测表 区县在线总数量
        Map<String, Long> intranetIPCountyCount = intranetIPService.queryAllOnlineCount();
        // 公网IP拨测表 区县在线总数量
        Map<String, Long> pubNetIPCountyCount = pubNetIPService.queryAllOnlineCount();
        // 公网web拨测表 区县在线总数量
        Map<String, Long> pubNetWebCountyCount = pubNetWebService.queryAllOnlineCount();
        // 行业视频表 区县在线总数量
        Map<String, Long> industryVideoCountyCount = videoService.queryAllOnlineCount();

        // 2.查询各个区县所有数量
        Map<String, Long> intranetIPAllCountMap = intranetIPService.queryAllCount();
        Map<String, Long> pubNetIPAllCountMap = pubNetIPService.queryAllCount();
        Map<String, Long> pubNetWebAllCountMap = pubNetWebService.queryAllCount();
        Map<String, Long> videoAllCountMap = videoService.queryAllCount();

        // 调用工具类计算方法
        Map<String, Object> countyOnlineRateResultMap = CalculateUtils.calculateCountyOnlineRate(intranetIPCountyCount, pubNetIPCountyCount,
                pubNetWebCountyCount, industryVideoCountyCount,
                intranetIPAllCountMap, pubNetIPAllCountMap,
                pubNetWebAllCountMap, videoAllCountMap);
        if (countyOnlineRateResultMap != null) {
            return ResultUtils.success(countyOnlineRateResultMap);
        }
        return ResultUtils.success();
    }


    /**
     * 数据删除（没有使用假删除方式）
     * @param ids
     * @return
     */
    @PostMapping("/delDataInfo")
    public Result removeIntranetIPInfo(@RequestBody List<Integer> ids) {

        List<Object> delResult = intranetIPService.delBatchByIds(ids);
        if (delResult != null && delResult.size() != 0) {
            return ResultUtils.success(delResult);
        }
        return ResultUtils.success();
    }
}
