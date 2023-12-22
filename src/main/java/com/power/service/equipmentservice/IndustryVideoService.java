package com.power.service.equipmentservice;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.power.common.constant.ProStaConstant;
import com.power.common.constant.ResultStatusCode;
import com.power.entity.equipment.IndustryVideoEntity;
import com.power.entity.query.DialFilterQuery;
import com.power.mapper.equipmentmapper.IndustryVideoMapper;
import com.power.utils.AnalysisExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        String originalFilename = file.getOriginalFilename();
        if (originalFilename.contains("行业视频")) {
            List<IndustryVideoEntity> industryVideoEntityList = this.importData(file);
            if (industryVideoEntityList != null) {
                for (IndustryVideoEntity industryVideo : industryVideoEntityList) {
                    QueryWrapper<IndustryVideoEntity> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("equipment_ip", industryVideo.getEquipmentIp());
                    this.saveOrUpdate(industryVideo, queryWrapper);
                }
//            boolean saveBatch = this.saveBatch(industryVideoEntityList, 1000);
                return ResultStatusCode.SUCCESS_UPLOAD.getMsg();
            }
        }
        return ResultStatusCode.ERROR_IMPORT.getMsg();
    }


    /**
     * 内网IP表 区县在线总数量
     * @return
     */
    public Map<String, Long> queryAllOnlineCount() {
        Map<String, Long> countMap = new HashMap<>();
        String[] counties = {ProStaConstant.CUSTOMER,ProStaConstant.JIA_HE,ProStaConstant.PING_HU,
                ProStaConstant.JIA_SHAN, ProStaConstant.TONG_XIANG, ProStaConstant.HAI_NING,
                ProStaConstant.HAI_YAN, ProStaConstant.XIU_ZHOU, ProStaConstant.NAN_HU};

        for (int i = 0; i < counties.length; i++) {
            QueryWrapper<IndustryVideoEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.isNotNull("project_name").ne("project_name", "");
            queryWrapper.eq("project_status", true);
            queryWrapper.like("county", counties[i]);
            long count = this.count(queryWrapper);
            countMap.put(counties[i], count);
        }
        return countMap;
    }


    /**
     * 内网IP表 区县总数量
     * @return
     */
    public Map<String, Long> queryAllCount() {
        Map<String, Long> allCountMap = new HashMap<>();
        for (String county : ProStaConstant.counties) {
            QueryWrapper<IndustryVideoEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.isNotNull("project_name").ne("project_name", "");
            queryWrapper.like("county", county);
            long count = this.count(queryWrapper);
            allCountMap.put(county, count);
        }
        return allCountMap;
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
                        for (int j = 1; j <= sheet.getLastRowNum(); j++) {
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
                                if (cell != null) {
                                    CellType cellType = cell.getCellType();
                                    if (CellType.STRING == cellType) {
                                        cellValue = cell.getStringCellValue();
                                    } else if (CellType.BLANK == cellType){
                                        cellValue = null;
                                    } else {
                                        cellValue = cell.getStringCellValue();
                                    }
                                } else {
                                    cellValue = null;
                                }
//                                cellValue = cell.getStringCellValue();
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


    /**
     * 行业视频在线率计算
     * @return
     */
    public List<Long> calculateVideoRate() {

        List<Long> videoRateList = new ArrayList<>();
        QueryWrapper<IndustryVideoEntity> queryWrapper = new QueryWrapper<>();

        // 分母（所有项目名称不为空的数据）
        queryWrapper.isNotNull("project_name").ne("project_name", "");
        long denominator = this.count(queryWrapper);
        videoRateList.add(denominator);
        // 分子（摄像头状态在线的数据）
        queryWrapper.eq("camera_status", "在线");
        long numerator = this.count(queryWrapper);
        videoRateList.add(numerator);
        return videoRateList;
    }


    /**
     * 搜索、筛选后导出使用
     * @return
     */
    public List<IndustryVideoEntity> searchOrFilterByExport() {

        QueryWrapper<IndustryVideoEntity> queryWrapper = new QueryWrapper<>();
        // 筛选指定字段 不为空或为空字符串的情况
        queryWrapper.isNotNull("project_name").ne("project_name", "");
        List<IndustryVideoEntity> list = this.list(queryWrapper);
        return list;
    }


    /**
     * 删除操作（未使用假删除，操作后数据库信息将直接删除）
     * @param ids 删除数据的id
     * @return
     */
    public List<Object> delBatchByIds(List<Integer> ids) {

        List<Object> delResultSta = new ArrayList<>();
        if (ids != null && ids.size() >= 1) {
            boolean removeStatus = removeBatchByIds(ids);
            if (removeStatus) {
                delResultSta.add(ResultStatusCode.SUCCESS_DELETE_USER.getCode());
                delResultSta.add(ResultStatusCode.SUCCESS_DELETE_USER.getMsg());
                return delResultSta;
            }
        }
        delResultSta.add(ResultStatusCode.ERROR_DEL_USER_1002.getCode());
        delResultSta.add(ResultStatusCode.ERROR_DEL_USER_1002.getMsg());
        return delResultSta;
    }
}
