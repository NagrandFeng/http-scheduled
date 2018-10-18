package com.ysf.delayschedule.service.impl;

import com.google.common.collect.Lists;
import com.ysf.delayschedule.commons.constants.ScheduleTaskState;
import com.ysf.delayschedule.entity.DelaySchedule;
import com.ysf.delayschedule.protocol.request.DelayScheduleRequest;
import com.ysf.delayschedule.protocol.response.DelayScheduleResponse;
import com.ysf.delayschedule.repository.DelayScheduleRepository;
import com.ysf.delayschedule.service.DelayScheduleService;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Yeshufeng
 * @title
 * @date 2018/9/19
 */
@Slf4j
@Service
public class DelayScheduleServiceImpl implements DelayScheduleService {

    @Autowired
    private DelayScheduleRepository delayScheduleRepository;

    @Override
    public DelaySchedule findById(Integer id) {
         return delayScheduleRepository.findByScheduleId(id);
    }

    @Override
    public DelayScheduleResponse register(DelayScheduleRequest request) {

        DelayScheduleResponse response = new DelayScheduleResponse();
        DelaySchedule saveEntity = new DelaySchedule();
        BeanUtils.copyProperties(request,saveEntity);
        saveEntity.setCode(ScheduleTaskState.DEFAULT.getCode());
        saveEntity.setMsg(ScheduleTaskState.DEFAULT.getDesc());
        Date now = new Date();
        saveEntity.setCreateTime(now);
        saveEntity.setUpdateTime(now);
        saveEntity = delayScheduleRepository.save(saveEntity);
        if(saveEntity.getScheduleId()==null){
            response.setStatus(-1);
            response.setMessage("register failed");

        } else {
            response.setScheduleId(saveEntity.getScheduleId());
            response.setStatus(1);
            response.setMessage("success");
        }
        return  response;
    }

    @Override
    public List<DelaySchedule> findUndone(Long current) {

        List<Integer> unDealTaskState = Lists.newArrayList();
        unDealTaskState.add(ScheduleTaskState.SCAN_LOCK.getCode());
        unDealTaskState.add(ScheduleTaskState.WAIT_IN_QUEUE.getCode());
        List<DelaySchedule> taskOvertimeUndoneList = delayScheduleRepository.findAllByCodeInAndExpectExeTimeLessThan(unDealTaskState,current);

        return taskOvertimeUndoneList;
    }
}
