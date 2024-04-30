package com.power.controller.SABcontroller.serverequipment;

import com.power.common.Result;
import com.power.service.SABservice.serverequipservice.ServerEquipmentService;
import com.power.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 机房设备信息控制层
 * @author cyk
 * @since 2024/4
 */
@RestController
@RequestMapping("/api/serverEquip")
public class ServerEquipmentCtl {

    @Autowired
    private ServerEquipmentService serverEquipmentService;

    /**
     * Excel机房信息数据导入
     * @param file 源文件
     * @return
     */
    @PostMapping("/import")
    public Result importServerEquipFile(@RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            String resultStr = serverEquipmentService.importServerEquipExcel(file);
            return ResultUtils.success(resultStr);
        }
        return ResultUtils.success();
    }
}
