## 一.概念


主要解决的场景：A操作发生之后一段时间，B操作执行

计划任务实现：A操作发生，往数据库插一条记录，由计划任务定期扫表执行
缺点：计划任务有执行周期，对时间要求很精确的操作不友好；实现这种功能每次都可能在数据库增加表，其实这些表实现的功能都是相似的，每次需要设计计划任务表，这些动作是重复的，可以抽象在一套系统里实现


## 二.功能简介

调用注册接口

### 1.待执行记录保存在数据库

本地持久化通过mysql实现,数据库储存任务执行需要的元数据

### 2.队列等待数据在内存

### 3.工作线程

负责任务排队的线程池，TripScheduledThreadPoolExecutor（改自ScheduledThreadPoolExecutor）

负责将初始状态的任务扫进排队任务的线程池 ScheduledThreadPoolExecutor

负责补偿工作的线程池  ScheduledThreadPoolExecutor（主要负责将期望执行时间已经超过当前时间的任务重新入队列）


## 三、两个核心实现类
MyThreadPoolExecutor：改造ThreadPoolExecutor

TripScheduledThreadPoolExecutor(extends MyThreadPoolExecutor) ：改造ScheduledThreadPoolExecutor ，使用schedule(task,delay,unit)方法实现延迟调用


## DelayWorkQueue介绍
核心底层实现通过堆实现，本质是一个小顶堆，元素通过delay时间比较，delay最小的时间排在堆顶，取元素的时候，队首元素就是堆顶元素

1.使用DelayedWorkQueue作为阻塞队列，并没有像ThreadPoolExecutor类一样开放给用户进行自定义设置。该队列是ScheduledThreadPoolExecutor类的核心组件。

2.这里没有向用户开放maximumPoolSize的设置，原因是DelayedWorkQueue中的元素在大于初始容量16时，会进行扩容，也就是说队列不会装满，maximumPoolSize参数即使设置了也不会生效。

3.Worker线程没有回收时间，原因跟第2点一样，因为不会触发回收操作。所以这里的线程存活时间都设置为0。