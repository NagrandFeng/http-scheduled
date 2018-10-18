package com.ysf.delayschedule.core;

import com.google.common.collect.Lists;
import com.ysf.delayschedule.commons.constants.ScheduleTaskState;
import com.ysf.delayschedule.core.biz.JobExecutorBiz;
import com.ysf.delayschedule.core.threadpool.ScheduleThreadFactory;
import com.ysf.delayschedule.core.threadpool.TripScheduledThreadPoolExecutor;
import com.ysf.delayschedule.entity.DelaySchedule;
import com.ysf.delayschedule.repository.DelayScheduleRepository;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author Yeshufeng
 * @title
 * @date 2017/9/6
 */
@Service
public class ScheduleExecutor extends Observable implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(ScheduleExecutor.class);

    // 默认的延迟队列调度线程池的线程threadName前缀
    private final String threadNamePrefix = "delay-schedule";

    // 从数据库中往调度队列中补给的线程池的线程threadName前缀
    private final String supplyThreadNamePrefix = "delay-schedule-queue-supply";

    // 检查延迟队列中已经超时且未执行完成的任务
    private final String fixDataThreadNamePrefix = "delay-schedule-queue-fix";

    // 从数据库中往调度队列中补给扫描的初始默认延迟 10秒
    private final int scan_default_deley = 10 * 1000;

    // 在数据库中检测超时未完成的任务初始默认延迟
    private final int fix_default_deley = 10 * 1000;

    // 数据库扫出的数据提前入队的时间 - 10分钟
    private final int DATA_IN_QUEUE = 10 * 60 * 1000;

    // 从数据库中往调度队列中补给扫描的周期-10秒
    public final int SCAN_PERIOD = 10 * 1000;

    // 从数据库中往调度队列中补给扫描的周期-30秒
    public final int FIX_PERIOD = 30 * 1000;

    // 每一个任务执行完成最长需要的时间：20秒
    public static final Integer SUPPOSED_RUN_TIME = 20 * 1000;

    private static final int COREPOOLSIZE_DEFAULT = 32;
    private static final int COREPOOLSIZE_FIX_DATA = 8;
    private static final int COREPOOLSIZE_SUPPLY = 2;

    // 默认的延迟队列调度线程池
    private TripScheduledThreadPoolExecutor executorService;

    // 从数据库中往调度队列中补给的线程池
    private ScheduledExecutorService supplyExecutorService;

    private ScheduledExecutorService fixDataExecutorService;


    @Autowired
    private JobExecutorBiz jobExecutorBiz;

    @Autowired
    private DelayScheduleRepository delayScheduleRepository;

    public ScheduleExecutor() {

        executorService = new TripScheduledThreadPoolExecutor(COREPOOLSIZE_DEFAULT,
                new ScheduleThreadFactory(threadNamePrefix));

        supplyExecutorService = Executors
                .newScheduledThreadPool(COREPOOLSIZE_SUPPLY, new ScheduleThreadFactory(supplyThreadNamePrefix));

        supplyExecutorService.scheduleAtFixedRate(new QueueSupplementRunnable(), scan_default_deley, SCAN_PERIOD,
                TimeUnit.MILLISECONDS);

        fixDataExecutorService = Executors.newScheduledThreadPool(COREPOOLSIZE_FIX_DATA, new ScheduleThreadFactory(fixDataThreadNamePrefix));

        fixDataExecutorService.scheduleAtFixedRate(new QueueFixOvertimeUndoneTaskRunnable(), fix_default_deley, FIX_PERIOD,
                TimeUnit.MILLISECONDS);

    }

    @Override
    public void destroy() throws Exception {

    }

    public class ScheduleRunnable implements Runnable {

        private Integer scheduleId;


        public ScheduleRunnable(Integer scheduleId) {
            this.scheduleId = scheduleId;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            try {
                log.info("任务被调度，现在开始处理执行, schedule_id = " + scheduleId + "current:" + start);
                jobExecutorBiz.execute(scheduleId);
                updateScheduleState(scheduleId, ScheduleTaskState.COMPLETE);
            } catch (Exception e) {
                log.error("任务调度处理失败, schedule_id = " + scheduleId + ", 错误信息：" + e.getMessage(), e);
                updateScheduleState(scheduleId, ScheduleTaskState.FAIL);
            }

        }

        public Integer getScheduleId() {
            return scheduleId;
        }

    }

    public class QueueSupplementRunnable implements Runnable {

        @Override
        public void run() {
            //1.从数据库扫数据
            Long current = System.currentTimeMillis();
            log.info("scan task into queue");
            List<DelaySchedule> taskWaitForHandleList = delayScheduleRepository.findAllByCodeAndExpectExeTimeLessThanEqual(ScheduleTaskState.DEFAULT.getCode(),current+DATA_IN_QUEUE);

            //进队列
            taskWaitForHandleList.forEach(item -> {
                try {
                    //state更新成功(0->3) 才将任务入队等待，防止多台机器同时入队调用
                    if(updateScheduleState(item.getScheduleId(),ScheduleTaskState.DEFAULT,ScheduleTaskState.SCAN_LOCK)){
                        submit(item.getScheduleId(),item.getExpectExeTime());
                    }
                } catch (Exception e) {
                    log.error("补给线程池失败,id:{}",item.getScheduleId(),e);
                }
            });

        }
    }

    public class QueueFixOvertimeUndoneTaskRunnable implements Runnable {
        @Override
        public void run() {
            Long current = System.currentTimeMillis();

            Long maxExpectDoneTime = current + SUPPOSED_RUN_TIME;

            List<Integer> unDealTaskState = Lists.newArrayList();
            unDealTaskState.add(ScheduleTaskState.SCAN_LOCK.getCode());
            unDealTaskState.add(ScheduleTaskState.WAIT_IN_QUEUE.getCode());
            List<DelaySchedule> taskOvertimeUndoneList = delayScheduleRepository.findAllByCodeInAndExpectExeTimeLessThan(unDealTaskState,maxExpectDoneTime);
            for (DelaySchedule delayTask : taskOvertimeUndoneList) {
                log.warn("scheduleId:{} 在期望执行时间:{} 以后的20000 ms内并未完成执行,在{}更新为未处理状态,下一次计划任务执行时被扫进队列",delayTask.getScheduleId(),delayTask.getExpectExeTime(),current);
                updateScheduleState(delayTask.getScheduleId(),ScheduleTaskState.DEFAULT);
            }
        }
    }

    public void submit(Integer scheduleId, Long expectTime) throws Exception {
        log.info("submit to job pool, scheduleId = " + scheduleId + ", expectTime = " + expectTime);

        long currentTime = new Date().getTime();

        try{
            executorService.schedule(new ScheduleRunnable(scheduleId),expectTime-currentTime, TimeUnit.MILLISECONDS);
            updateScheduleState(scheduleId,ScheduleTaskState.WAIT_IN_QUEUE);
        }catch (Exception e){
            log.warn("job scheduleID"+scheduleId+"入队失败，需重置任务状态为初始态",e);
            updateScheduleState(scheduleId,ScheduleTaskState.DEFAULT);
        }

    }


    /**
     * 更新指定记录的状态
     *
     * @param scheduleId        任务ID
     * @param scheduleTaskState 目标状态
     * @return
     */
    private void updateScheduleState(Integer scheduleId, ScheduleTaskState scheduleTaskState) {
        delayScheduleRepository.updateStatusById(scheduleId, scheduleTaskState.getCode(),scheduleTaskState.getDesc());
    }


    /**
     * 更新指定记录的状态
     *
     * @param scheduleId 任务ID
     * @param from       更新前状态
     * @param to         目标状态
     * @return
     */
    private boolean updateScheduleState(Integer scheduleId, ScheduleTaskState from, ScheduleTaskState to) {
        int result = delayScheduleRepository.updateStatusByIdAndStatus(scheduleId, from.getCode(), to.getCode());
        return result > 0;
    }
}

