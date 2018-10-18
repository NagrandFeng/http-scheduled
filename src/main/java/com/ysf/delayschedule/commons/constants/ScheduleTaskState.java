package com.ysf.delayschedule.commons.constants;

/**
 * @author Yeshufeng
 * @title
 * @date 2018/9/19
 */
public enum ScheduleTaskState {

    DEFAULT(0,"待处理"),
    SCAN_LOCK(3,"被扫描"),
    WAIT_IN_QUEUE(4,"入队成功"),
    COMPLETE(1,"调用接口执行成功"),
    FAIL(2,"调用接口执行失败")
    ;

    private int code;

    private String desc;

    ScheduleTaskState(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
