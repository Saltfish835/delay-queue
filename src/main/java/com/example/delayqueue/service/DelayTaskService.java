package com.example.delayqueue.service;

import com.example.delayqueue.bean.Task;

public interface DelayTaskService {

    /**
     * 添加任务
     * @param task
     */
    void addTask(Task task);

    /**
     * 取消任务
     * @param task
     */
    void cancelTask(Task task);
}
