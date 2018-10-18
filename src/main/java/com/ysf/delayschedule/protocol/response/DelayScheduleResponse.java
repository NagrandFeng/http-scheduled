package com.ysf.delayschedule.protocol.response;

import lombok.Data;
import lombok.ToString;

/**
 * @author Yeshufeng
 * @title
 * @date 2018/9/19
 */
@Data
@ToString
public class DelayScheduleResponse {

    private Integer status;

    private String message;

    private Integer scheduleId;
}
