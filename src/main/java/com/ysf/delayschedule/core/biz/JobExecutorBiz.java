package com.ysf.delayschedule.core.biz;

import com.ysf.delayschedule.core.http.HttpSendProcessor;
import com.ysf.delayschedule.entity.DelaySchedule;
import com.ysf.delayschedule.repository.DelayScheduleRepository;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Yeshufeng
 * @title
 * @date 2018/9/19
 */
@Service
@Slf4j
public class JobExecutorBiz {

    @Autowired
    private HttpSendProcessor httpSendProcessor;

    @Autowired
    private DelayScheduleRepository delayScheduleRepository;

    @Value("${http.basic.auth.username}")
    private String userName;

    @Value("${http.basic.auth.password}")
    private String password;


    public void execute(Integer scheduleId){
        DelaySchedule delaySchedule = delayScheduleRepository.findByScheduleId(scheduleId);
        String response = httpSendProcessor.execute(delaySchedule.getUri(),delaySchedule.getRequestBody(),delaySchedule.getMethod(),getHeader());
        Long current = System.currentTimeMillis();
        log.info("execute uri:{},current:{},response:{}",delaySchedule.getUri(),current,response);
        delaySchedule.setResponseBody(response);
        //update
        delayScheduleRepository.updateResponseById(scheduleId,response);
    }


    private Header getHeader() {
        byte[] bd = (userName + ":" + password).getBytes(StandardCharsets.UTF_8);
        return  new BasicHeader("Authorization", "Basic " + Base64.encodeBase64String(bd));
    }

}
