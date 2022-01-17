package com.common.tools.http;

import com.common.tools.http.utils.HttpLog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 作者：heshuiguang
 * 日期：2021/12/27 2:28 PM
 * 类说明：调度线程池
 */
public class PollingStateMachine {

    private static volatile PollingStateMachine instance = null;
    private ExecutorService pool;
    public static int corePoolSize = Runtime.getRuntime().availableProcessors() + 1;//线程数为Java虚拟机可用的处理器数+1

    public PollingStateMachine() {
        HttpLog.log().i("corePoolSize = "+corePoolSize);
        pool = Executors.newFixedThreadPool(corePoolSize);
    }

    /**
     * 方法说明：单例对象
     * 作者：heshuiguang
     * 日期：2019-11-19 17:38
     */
    public static PollingStateMachine getInstance(){
        if(instance == null){
            synchronized (PollingStateMachine.class){
                if(instance == null){
                    instance = new PollingStateMachine();
                }
            }
        }
        return instance;
    }

    /**
     * 方法说明：加入线程池执行任务
     */
    public void execute(Runnable runnable){
        if(pool == null) return;
        pool.execute(runnable);
    }

    /**
     * 方法说明：停止线程池
     */
    public void clearPool(){
        if(pool == null) return;
        pool.shutdown();
        instance = null;
    }

}
