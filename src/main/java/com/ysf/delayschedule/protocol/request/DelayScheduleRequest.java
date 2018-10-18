package com.ysf.delayschedule.protocol.request;


import lombok.Data;

/**
 * @author Yeshufeng
 * @title
 * @date 2018/9/19
 */
@Data
public class DelayScheduleRequest {

    /**
     * 接口方法名称
     */
    private String method;

    /**
     * 接口名
     */
    private String uri;

    /**
     * 请求入参，json字符串
     */
    private String requestBody;

    /**
     * 期望执行时间：毫秒
     */
    private Long expectExeTime;

}
