package com.power.service.equipmentservice;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.equipment.IndustryVideoEntity;
import com.power.mapper.equipmentmapper.IndustryVideoMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class IndustryVideoService extends ServiceImpl<IndustryVideoMapper, IndustryVideoEntity> {

    /**
     * 查询所有信息
     * @return
     */
    public IPage<IndustryVideoEntity> queryIndustryVideoInfo(Integer pageNum, Integer pageSize) {
        IPage industryPage = new Page<IndustryVideoEntity>(pageNum, pageSize);
        QueryWrapper<IndustryVideoEntity> queryWrapper = new QueryWrapper<>();
        // 筛选指定字段 不为空或为空字符串的情况
        queryWrapper.isNotNull("project_name").ne("project_name", "");
        IPage pages = this.page(industryPage, queryWrapper);
        return pages;
    }


    /**
     * 文件上传
     * @param file
     * @return
     */
    public String importIndustryVideoExcel(MultipartFile file) {

        List<IndustryVideoEntity> industryVideoEntityList = this.importData(file);
        if (industryVideoEntityList != null) {
            boolean saveBatch = this.saveBatch(industryVideoEntityList, 1000);
            if (saveBatch) {
                return ResultStatusCode.SUCCESS_UPLOAD.toString();
            }
        }
        return ResultStatusCode.FILE_TYPE_ERROR.toString();
    }

    /**
     * 文件处理
     * @param file
     * @return
     */
    private List<IndustryVideoEntity> importData(MultipartFile file) {

        // 判断文件是否为空
        if (!file.isEmpty()) {
            // 是否符合上传文件类型 .xlsx/.xls
            Workbook workbook = AnalysisExcelUtils.isExcelFile(file);
            IndustryVideoEntity industryVideo;
            List<IndustryVideoEntity> industryVideoLists = new ArrayList<>();
            if (workbook != null) {
                int sheets = workbook.getNumberOfSheets();
                for (int i = 0; i < sheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (sheet != null) {

                        ArrayList<String> titles = new ArrayList<>();
                        // 标题行
                        Row titleRow = sheet.getRow(0);
                        // excel中数据有多少列
                        short columns = titleRow.getLastCellNum();
                        for (int j = 0; j < columns; j++) {
                            Cell column = titleRow.getCell(j);
                            String titleValue = column.getStringCellValue();
                            titles.add(titleValue);
                        }

                        // 循环遍历数据内容
                        for (int j = 1; j < sheet.getLastRowNum(); j++) {
                            Row contentRow = sheet.getRow(j);
                            short lastCellNum = contentRow.getLastCellNum();
                            // 此行没有数据，继续循环其他行
                            /*if (contentRow == null) {
                                continue;
                            }*/
                            industryVideo = new IndustryVideoEntity();
                            String cellValue = null;
                            for (int k = 0; k < lastCellNum; k++) {
                                Cell cell = contentRow.getCell(k);
                                cellValue = cell.getStringCellValue();
                                switch (k) {
                                    case 0:
                                        industryVideo.setCameraStatus(cellValue);
                                        break;
                                    case 1:
                                        industryVideo.setResourceEncoding(cellValue);
                                        break;
                                    case 2:
                                        industryVideo.setDomainEncoding(cellValue);
                                        break;
                                    case 3:
                                        industryVideo.setProjectName(cellValue);
                                        break;
                                    case 4:
                                        industryVideo.setProjectNum(cellValue);
                                        break;
                                    case 5:
                                        industryVideo.setEquipmentName(cellValue);
                                        break;
                                    case 6:
                                        industryVideo.setEquipmentIp(cellValue);
                                        break;
                                    case 7:
                                        industryVideo.setCity(cellValue);
                                        break;
                                    case 8:
                                        industryVideo.setCounty(cellValue);
                                        break;
                                    case 9:
                                        industryVideo.setIndustry(cellValue);
                                        break;
                                    case 10:
                                        industryVideo.setMaintenanceSubject(cellValue);
                                        break;
                                    case 11:
                                        industryVideo.setE55Charging(cellValue);
                                        break;
                                    case 12:
                                        industryVideo.setLensId(cellValue);
                                        break;
//                                    case 13:
//                                        industryVideo.setLensName(cellValue);
//                                        break;
                                    default:
                                        industryVideo.setLensName(cellValue);
                                        break;
                                }
                            }
                            industryVideo.setProjectStatus(true);
                            industryVideoLists.add(industryVideo);
                        }
                    }
                    continue;
                }
//                return null;
                return industryVideoLists;
            }
            // workbook=null表示文件类型错误
            return null;
        }
        return null;
    }

}
