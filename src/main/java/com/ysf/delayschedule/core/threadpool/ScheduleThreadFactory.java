package com.ysf.delayschedule.core.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ScheduleThreadFactory implements ThreadFactory {

        final AtomicInteger threadNumber = new AtomicInteger(1);

        final String threadNamePrefix;

        public ScheduleThreadFactory(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            final Thread thread = new Thread(runnable, threadNamePrefix + threadNumber.getAndIncrement());
            return thread;
        }
    }