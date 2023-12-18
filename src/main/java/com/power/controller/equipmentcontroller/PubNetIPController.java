package com.power.controller.equipmentcontroller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.equipment.PubNetIPEntity;
import com.power.entity.query.DialFilterQuery;
import com.power.service.equipmentservice.PubNetIPService;
import com.power.utils.AnalysisExcelUtils;
import com.power.utils.ResultUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 公网IP拨测控制层
 * @since 2023/9
 * @author cyk
 */
@RestController
@RequestMapping("/api/pubNetIp")
public class PubNetIPController {

    @Autowired
    private PubNetIPService pubNetIPService;

    /**
     * 数据导入
     * @param file 公网ip拨测excel文件
     * @return
     */
    @PostMapping("/import")
    public Result importPubNetFile(@RequestParam MultipartFile file) {
        String importResult = pubNetIPService.importPubNetIPExcel(file);
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
        List<PubNetIPEntity> pubNetIPList = pubNetIPService.searchOrFilterByExport(dialFilterQuery);
        try {
            // 通过工具类创建writer
            ExcelWriter writer = ExcelUtil.getWriter(true);
            // 自定义标题别名
            writer.addHeaderAlias("projectName", "拨测对象所属项目");
            writer.addHeaderAlias("equipmentName", "设备名称");
            writer.addHeaderAlias("city", "地市");
            writer.addHeaderAlias("county", "区县");
            writer.addHeaderAlias("destinationIp", "目标IP");
            writer.addHeaderAlias("servePort", "端口号");
            writer.addHeaderAlias("dialTime", "最新拨测时间");
            writer.addHeaderAlias("dialType", "拨测类型");
            writer.addHeaderAlias("taskStatus", "任务状态");
            writer.addHeaderAlias("dialResult", "最新拨测结果");
            writer.addHeaderAlias("lossRate", "最新丢包(%)");
            writer.addHeaderAlias("loadingDelay", "最新时延(ms)");
            writer.addHeaderAlias("shake", "最新抖动(ms)");
            writer.addHeaderAlias("projectStatus", "项目状态");
            writer.write(pubNetIPList, true);
            String fileName = "公网IP拨测";
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
    public Result queryPubNetIPInfo(@RequestParam Integer pageNum,
                                  @RequestParam Integer pageSize) {

        IPage<PubNetIPEntity> pubNetIpPages = pubNetIPService.queryPubNetworkIPInfo(pageNum, pageSize);
        if (pubNetIpPages != null) {
            return ResultUtils.success(pubNetIpPages);
        }
        return ResultUtils.success();
    }


    /**
     * 搜索、筛选
     * @param dialFilterQuery
     * @return
     */
    @PostMapping("/searchOrFilterInfo")
    public Result searchPubNetIPInfo(@RequestBody DialFilterQuery dialFilterQuery) {

        IPage<PubNetIPEntity> pubNetIPEntityIPage = pubNetIPService.searchOrFilter(dialFilterQuery);
        if (pubNetIPEntityIPage != null) {
            return ResultUtils.success(pubNetIPEntityIPage);
        }
        return ResultUtils.success(ResultStatusCode.CONDITION_ERROR.getMsg());
    }


    /**
     * 数据删除（没有使用假删除方式）
     * @param ids
     * @return
     */
    @PostMapping("/delDataInfo")
    public Result removePubNetIPInfo(@RequestBody List<Integer> ids) {

        List<Object> delResult = pubNetIPService.delBatchByIds(ids);
        if (delResult != null && delResult.size() != 0) {
            return ResultUtils.success(delResult);
        }
        return ResultUtils.success();
    }
}
