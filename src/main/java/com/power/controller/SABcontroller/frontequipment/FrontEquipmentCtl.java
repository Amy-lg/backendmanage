package com.power.controller.SABcontroller.frontequipment;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.sabentity.filterseacher.EquipParamEntity;
import com.power.entity.sabentity.headend.FrontEquipmentEntity;
import com.power.entity.sabentity.machine.ServerEquipmentEntity;
import com.power.service.SABservice.frontequipservice.FrontEquipmentService;
import com.power.service.SABservice.serverequipservice.ServerEquipmentService;
import com.power.utils.AnalysisExcelUtils;
import com.power.utils.ResultUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 前端设备信息控制层
 * @author cyk
 * @since 2024/4
 */
@RestController
@RequestMapping("/api/frontEquip")
public class FrontEquipmentCtl {

    @Autowired
    private FrontEquipmentService frontEquipmentService;


    // 用于查询机房设备表数据
    @Autowired
    private ServerEquipmentService serverEquipmentService;

    /**
     * Excel前端设备信息数据导入
     * @param file 源文件
     * @return
     */
    @PostMapping("/import")
    public Result importFrontEquipFile(@RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            String resultStr = frontEquipmentService.importFrontEquipExcel(file);
            return ResultUtils.success(resultStr);
        }
        return ResultUtils.success();
    }


    /**
     * 前端设备、机房设备信息全部数据分页显示
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/showEquipData")
    public Result getAllEquipment(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {

        // 存储前端设备和机房设备查询的list数据集
        List<Object> list = new ArrayList<>();
        // 数据查询
        List<FrontEquipmentEntity> frontEquipmentList = frontEquipmentService.selectFrontEquipment();
        List<ServerEquipmentEntity> serverEquipmentList = serverEquipmentService.selectServeEquipment();
        if (frontEquipmentList != null) {
            for (FrontEquipmentEntity frontEquipment : frontEquipmentList) {
                list.add(frontEquipment);
            }
        }
        if (serverEquipmentList != null) {
            for (ServerEquipmentEntity serverEquipment : serverEquipmentList) {
                list.add(serverEquipment);
            }
        }

        // 1.创建一个空IPage()对象
        IPage<Object> allEquipPage = new Page<>();
        // 2.设置IPage对象中总记录条数totalCount
        int size = list.size();
        allEquipPage.setTotal(size);
        // 3.设置IPage当前页码
        allEquipPage.setCurrent(pageNum);
        // 4.设置IPage对象每页记录数
        allEquipPage.setSize(pageSize);
        // 5.计算总页数;根据Math.ceil()向上取整
        int pages = (int) Math.ceil((double) allEquipPage.getTotal() / allEquipPage.getSize());
        allEquipPage.setPages(pages);
        // 6.设置IPage对象数据列表
        List<Object> records = list.subList((pageNum-1) * pageSize, Math.min(pageNum * pageSize, size));
        allEquipPage.setRecords(records);
        return ResultUtils.success(allEquipPage);
    }



    /**
     * 筛选检索功能接口
     * @param equipParam 检索条件
     * @return
     */
    @PostMapping("/selectAllEquip")
    public Result equipmentSearchFilter(@RequestBody EquipParamEntity equipParam) {

        List<Object> list = new ArrayList<>();
        if (equipParam != null) {
            Integer pageNum = equipParam.getPageNum();
            Integer pageSize = equipParam.getPageSize();

            List<FrontEquipmentEntity> frontEquipList = frontEquipmentService.getFrontEquipmentByCondition(equipParam);
            List<ServerEquipmentEntity> serverEquipList = serverEquipmentService.getServerEquipmentByCondition(equipParam);
            if (frontEquipList != null) {
                for (FrontEquipmentEntity frontEquipment : frontEquipList) {
                    list.add(frontEquipment);
                }
            }
            if (serverEquipList != null) {
                for (ServerEquipmentEntity serverEquipment : serverEquipList) {
                    list.add(serverEquipment);
                }
            }
            // 1.创建一个空IPage()对象
            IPage<Object> allEquipPage = new Page<>();
            // 2.设置IPage对象中总记录条数totalCount
            int size = list.size();
            allEquipPage.setTotal(size);
            // 3.设置IPage当前页码
            allEquipPage.setCurrent(pageNum);
            // 4.设置IPage对象每页记录数
            allEquipPage.setSize(pageSize);
            // 5.计算总页数;根据Math.ceil()向上取整
            int pages = (int) Math.ceil((double) allEquipPage.getTotal() / allEquipPage.getSize());
            allEquipPage.setPages(pages);
            // 6.设置IPage对象数据列表
            List<Object> records = list.subList((pageNum-1) * pageSize, Math.min(pageNum * pageSize, size));
            allEquipPage.setRecords(records);
            return ResultUtils.success(allEquipPage);

        }
        return ResultUtils.success();
    }



    /**
     * 新增修改
     * @param frontEquipment
     * @param
     * @return
     */
    @PostMapping("/updateEquipInfo")
    public Result updateEquipInfo(@RequestBody FrontEquipmentEntity frontEquipment) {

        // 前端设备操作
        if (frontEquipment != null) {
            String frontUpdateInfo = frontEquipmentService.updateFrontEquipInfo(frontEquipment);
            return ResultUtils.success(frontUpdateInfo);
        }
        // 服务设备操作哦
        /*if (serverEquipment != null) {
            String serverUpdateInfo = serverEquipmentService.updateServerEquipInfo(serverEquipment);
            return ResultUtils.success(serverUpdateInfo);
        }*/
        return ResultUtils.success();
    }


    /**
     * 删除
     * @param ictProjectNum
     * @return
     */
    @PostMapping("/removeSabData")
    public Result delEquipData(@RequestParam(value = "ictProjectNum", required = true) String ictProjectNum,
                               @RequestParam(value = "equipmentType", required = true) String equipmentType) {

        String delResult = null;
        if (!StrUtil.isBlank(ictProjectNum) && !StrUtil.isBlank(equipmentType)) {
            delResult = frontEquipmentService.delEquipByIctNumAndType(ictProjectNum, equipmentType);
            if (delResult.equals(ResultStatusCode.ERROR_DELETE.getMsg())) {
                String serverEquipResult = serverEquipmentService.
                        delServerEquipByIctNumAndType(ictProjectNum, equipmentType);
                return ResultUtils.success(serverEquipResult);
            }
        }
        return ResultUtils.success(delResult);
    }


    /**
     * 导出
     * @param response
     * @param equipParam
     */
    @PostMapping("/exportEquipData")
    public void exportData(HttpServletResponse response, @RequestBody EquipParamEntity equipParam) {

        List<Object> frontAndServerList = new ArrayList<>();

        List<FrontEquipmentEntity> exportFrontDataList = frontEquipmentService.
                getFrontEquipmentByCondition(equipParam);
        List<ServerEquipmentEntity> serverEquipmentList = serverEquipmentService.
                getServerEquipmentByCondition(equipParam);
        if (exportFrontDataList != null) {
            for (FrontEquipmentEntity frontEquipment : exportFrontDataList) {
                frontAndServerList.add(frontEquipment);
            }
        }
        if (serverEquipmentList != null) {
            for (ServerEquipmentEntity serverEquipment : serverEquipmentList) {
                frontAndServerList.add(serverEquipment);
            }
        }
        try {
            // 通过工具类创建writer
            ExcelWriter writer = ExcelUtil.getWriter(true);
            // 自定义标题别名
            writer.addHeaderAlias("ictProjectNum", "ICT项目编号");
            writer.addHeaderAlias("ictProjectName", "ICT项目名称");
            writer.addHeaderAlias("county", "所属区县");
            writer.addHeaderAlias("equipmentType", "设备类型");
            writer.addHeaderAlias("equipmentCount", "设备数量");

            writer.write(frontAndServerList, true);
            String fileName = "SAB项目表";
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


}
