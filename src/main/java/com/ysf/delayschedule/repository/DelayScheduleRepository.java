package com.ysf.delayschedule.repository;

import com.ysf.delayschedule.entity.DelaySchedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Yeshufeng
 * @title
 * @date 2018/9/19
 */
public interface DelayScheduleRepository extends JpaRepository<DelaySchedule,Integer> {

    DelaySchedule findByScheduleId(Integer scheduleId);

    List<DelaySchedule>  findAllByCodeAndExpectExeTimeLessThanEqual(Integer code,Long inQueueTime);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("update DelaySchedule set code = :code,msg = :msg where scheduleId = :scheduleId ")
    int updateStatusById(@Param("scheduleId") Integer scheduleId,@Param("code") Integer code,@Param("msg") String msg);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("update DelaySchedule set code = :to where scheduleId = :scheduleId and code = :from ")
    int updateStatusByIdAndStatus(@Param("scheduleId")Integer scheduleId,@Param("from")Integer from,@Param("to")Integer to);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query("update DelaySchedule set responseBody = :responseBody where scheduleId = :scheduleId")
    int updateResponseById(@Param("scheduleId")Integer scheduleId,@Param("responseBody")String responseBody);


    List<DelaySchedule> findAllByCodeInAndExpectExeTimeLessThan(List<Integer> code,Long unDoneTime);

}
