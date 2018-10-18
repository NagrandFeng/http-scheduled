package com.ysf.delayschedule.controller;

import com.ysf.delayschedule.entity.DelaySchedule;
import com.ysf.delayschedule.protocol.request.DelayScheduleRequest;
import com.ysf.delayschedule.protocol.response.DelayScheduleResponse;
import com.ysf.delayschedule.service.DelayScheduleService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Yeshufeng
 * @title
 * @date 2018/9/19
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class DelayScheduleController {

    @Autowired
    private DelayScheduleService delayScheduleService;

    @RequestMapping(value = "/register",method = RequestMethod.POST)
    public String registerTask(@RequestBody DelayScheduleRequest request){
        DelayScheduleResponse response =  delayScheduleService.register(request);
        return response.toString();
    }

    @RequestMapping(value = "/task",method = RequestMethod.GET)
    public String getJob(@RequestParam("id") Integer id){
        DelaySchedule delaySchedule = delayScheduleService.findById(id);
        return delaySchedule.toString();
    }

    @RequestMapping(value = "undone",method = RequestMethod.GET)
    public String undoneTask(@RequestParam("time") Long time){
        List<DelaySchedule> result = delayScheduleService.findUndone(time);
        return "success";
    }


}
