package com.power.common.constant;

/**
 * 项目状态常量
 */
public class ProStaConstant {

    // 项目状态常量
    public static final String PRO_MAINTENANCE = "项目在维";
    public static final String PRO_IMPLEMENT = "项目实施";
    public static final String THEME_LAUNCH = "主体上线";
    public static final String PRO_PRELIMINARY_INSPECTION = "项目初验";
    public static final String PRO_END_INSPECTION = "项目终验";
    public static final String PRO_FINAL = "项目结束";
    public static final String PRO_DISCONTINUE = "项目中止";
    public static final String PRE_FINISH = "提前结束";

    // 区县常量
    public static final String CUSTOMER = "要客";
    public static final String JIA_HE = "嘉禾";
    public static final String PING_HU = "平湖";
    public static final String JIA_SHAN = "嘉善";
    public static final String TONG_XIANG = "桐乡";
    public static final String HAI_NING = "海宁";
    public static final String HAI_YAN = "海盐";
    public static final String XIU_ZHOU = "秀洲";
    public static final String NAN_HU = "南湖";
    public static final String JIA_XING = "嘉兴";

    // 月份常量
    public static final int JANUARY = 1;
    public static final int FEBRUARY = 2;
    public static final int MARCH = 3;
    public static final int APRIL = 4;
    public static final int MAY = 5;
    public static final int JUNE = 6;
    public static final int JULY = 7;
    public static final int AUGUST = 8;
    public static final int SEPTEMBER = 9;
    public static final int OCTOBER = 10;
    public static final int NOVEMBER = 11;
    public static final int DECEMBER = 12;

    // 状态信息
    public static final String NORMAL = "正常";
    public static final String STOP = "停止";
    public static final String OPEN = "通";
    public static final String CLOSE = "不通";

    // 客户评价判断信息
    public static final String SATISFIED = "满意";
    public static final String UNSATISFIED = "非满";

    // 基本信息表
    public static final String IS_YES = "是";
    public static final String IS_NO = "否";
    public static final String DIAL_STATUS = "拨测状态";
    public static final String DIAL_RESULT = "最新拨测结果";
    public static final String TASK_STATUS = "任务状态";

    // 故障跟踪表
    public static final String EXP_REMINDER_SUB = "DICT售后服务智能中枢故障提醒";
    public static final String FIXED = "已修复";
    // 推送邮件接收人常量设置
    public static final String TO_EMAIL = "18867119065@139.com";

    public static final String[] counties = {CUSTOMER, JIA_HE, PING_HU, JIA_SHAN, TONG_XIANG, HAI_NING,
            HAI_YAN, XIU_ZHOU, NAN_HU};

    // 主页面项目在维嘉兴数据转给要客 常量数组
    public static final String[] counties_jx = {CUSTOMER, JIA_HE, PING_HU, JIA_SHAN, TONG_XIANG, HAI_NING,
            HAI_YAN, XIU_ZHOU, NAN_HU, JIA_XING};

    // 计算纳管率
    public static final String[] counties_rate = {ProStaConstant.CUSTOMER,ProStaConstant.PING_HU,
            ProStaConstant.JIA_SHAN, ProStaConstant.TONG_XIANG, ProStaConstant.HAI_NING,
            ProStaConstant.HAI_YAN, ProStaConstant.XIU_ZHOU, ProStaConstant.NAN_HU,ProStaConstant.JIA_HE};

}
