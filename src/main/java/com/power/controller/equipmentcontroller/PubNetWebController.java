package com.power.controller.equipmentcontroller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.equipment.PubNetWebEntity;
import com.power.entity.query.DialFilterQuery;
import com.power.service.equipmentservice.PubNetWebService;
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
 * 公网WebIP拨测控制层
 * @since 2023/9
 * @author cyk
 */
@RestController
@RequestMapping("/api/pubNet")
public class PubNetWebController {

    @Autowired
    private PubNetWebService pubNetWebService;

    /**
     * 数据导入接口
     * @param file 公网web拨测excel文件
     * @return
     */
    @PostMapping("/import")
    public Result importPubNetFile(@RequestParam MultipartFile file) {
        if (!file.isEmpty()) {
            String importResult = pubNetWebService.importPubNetExcel(file);
            if (importResult != null && !importResult.equals(ResultStatusCode.ERROR_IMPORT.getMsg())) {
                return ResultUtils.success(importResult);
            }
            return ResultUtils.error(5003, ResultStatusCode.ERROR_IMPORT_001.getMsg());
        }
        return ResultUtils.success();
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
        List<PubNetWebEntity> pubNetWebList = pubNetWebService.searchOrFilterByExport(dialFilterQuery);
        try {
            // 通过工具类创建writer
            ExcelWriter writer = ExcelUtil.getWriter(true);
            // 自定义标题别名
//        writer.addHeaderAlias("projectName", "拨测对象所属项目");
            writer.write(pubNetWebList, true);
            String fileName = "公网Web拨测";
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
    public Result queryPubNetInfo(@RequestParam Integer pageNum,
                                  @RequestParam Integer pageSize) {

        IPage<PubNetWebEntity> pubNetWebPages = pubNetWebService.queryPubNetworkInfo(pageNum, pageSize);
        if (pubNetWebPages != null) {
            return ResultUtils.success(pubNetWebPages);
        }
        return ResultUtils.success();
    }


    /**
     * 搜索、筛选
     * @param dialFilterQuery
     * @return
     */
    @PostMapping("/searchOrFilterInfo")
    public Result searchPubNetInfo(@RequestBody DialFilterQuery dialFilterQuery) {

        IPage<PubNetWebEntity> pubNetWebEntityIPage = pubNetWebService.searchOrFilter(dialFilterQuery);
        if (pubNetWebEntityIPage != null) {
            return ResultUtils.success(pubNetWebEntityIPage);
        }
        return ResultUtils.success(ResultStatusCode.CONDITION_ERROR.getMsg());
    }


    /**
     * 数据删除（没有使用假删除方式）
     * @param ids
     * @return
     */
    @PostMapping("/delDataInfo")
    public Result removePubNetInfo(@RequestBody List<Integer> ids) {

        List<Object> delResult = pubNetWebService.delBatchByIds(ids);
        if (delResult != null && delResult.size() != 0) {
            return ResultUtils.success(delResult);
        }
        return ResultUtils.success();
    }
}
