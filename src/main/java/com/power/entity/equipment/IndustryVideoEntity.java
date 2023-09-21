package com.power.entity.equipment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("industry_video")
public class IndustryVideoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 摄像头状态
     */
    private String cameraStatus;
    /**
     * 资源编码
     */
    private String resourceEncoding;
    /**
     * 域编码
     */
    private String domainEncoding;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 项目编码
     */
    private String projectNum;
    /**
     * 设备名称
     */
    private String equipmentName;
    /**
     * 设备IP地址
     */
    private String equipmentIp;
    /**
     * 地市
     */
    private String city;
    /**
     * 区县
     */
    private String county;
    /**
     * 行业
     */
    private String industry;
    /**
     * 维护主体
     */
    private String maintenanceSubject;
    /**
     * e55计费号
     */
    private String e55Charging;
    /**
     * 第三方镜头ID
     */
    private String lensId;
    /**
     * 镜头名称
     */
    private String lensName;
    /**
     * 项目状态
     */
    private boolean projectStatus;
}
