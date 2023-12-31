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

    public static final String[] counties = {ProStaConstant.CUSTOMER,ProStaConstant.JIA_HE,ProStaConstant.PING_HU,
            ProStaConstant.JIA_SHAN, ProStaConstant.TONG_XIANG, ProStaConstant.HAI_NING,
            ProStaConstant.HAI_YAN, ProStaConstant.XIU_ZHOU, ProStaConstant.NAN_HU};

    // 计算纳管率
    public static final String[] counties_rate = {ProStaConstant.CUSTOMER,ProStaConstant.PING_HU,
            ProStaConstant.JIA_SHAN, ProStaConstant.TONG_XIANG, ProStaConstant.HAI_NING,
            ProStaConstant.HAI_YAN, ProStaConstant.XIU_ZHOU, ProStaConstant.NAN_HU,ProStaConstant.JIA_HE};

}
