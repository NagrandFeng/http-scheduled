package com.ysf.delayschedule.service;

import com.ysf.delayschedule.entity.DelaySchedule;
import com.ysf.delayschedule.protocol.request.DelayScheduleRequest;
import com.ysf.delayschedule.protocol.response.DelayScheduleResponse;
import java.util.List;

/**
 * @author Yeshufeng
 * @title
 * @date 2018/9/19
 */
public interface DelayScheduleService {
    DelaySchedule findById(Integer id);

    DelayScheduleResponse register(DelayScheduleRequest request);

    List<DelaySchedule> findUndone(Long current);
}
