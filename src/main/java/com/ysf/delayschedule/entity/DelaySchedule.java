package com.ysf.delayschedule.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.ToString;

/**
 * @author Yeshufeng
 * @title
 * @date 2018/9/19
 */
@Entity
@Table(name = "delay_schedule")
@Data
@ToString
public class DelaySchedule implements Serializable {

    private static final long serialVersionUID = 1905846763042639184L;

    /**
     * 任务调度唯一标识
     */
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Integer scheduleId;

    /**
     * 接口方法名称
     */
    @Column(name = "method")
    private String method;

    /**
     * 接口名
     */
    @Column(name = "uri")
    private String uri;

    /**
     * 请求入参，json字符串
     */
    @Column(name = "request_body")
    private String requestBody;

    /**
     * 请求返回出参，json字符串
     */
    @Column(name = "response_body")
    private String responseBody;

    /**
     * 期望执行时间：毫秒
     */
    @Column(name = "expect_exe_time")
    private Long expectExeTime;

    /**
     * 实际执行时间；毫秒
     */
    @Column(name = "real_exe_time")
    private Long realExeTime;

    /**
     * 执行状态
     */
    @Column(name = "code")
    private Integer code;

    /**
     * 执行结果描述
     */
    @Column(name = "msg")
    private String msg;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;



}
