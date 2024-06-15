package com.example.delayqueue.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.delayqueue.bean.Task;

import java.util.List;

public interface DelayTaskService extends IService<Task> {

    /**
     * 添加任务
     * @param task
     * @return
     */
    long addTask(Task task);

    /**
     * 取消任务
     * @param taskId
     * @return
     */
    boolean cancelTask(long taskId);

    /**
     * 判断任务是否取消
     * @param taskId
     * @return
     */
    boolean isCanceled(long taskId);


    /**
     * 拉取任务
     * @param taskOwner
     * @return
     */
    Task pullTask(String taskOwner);


    /**
     * 批量拉取任务
     * @param taskOwner
     * @return
     */
    List<Task> pullTasks(String taskOwner);

    /**
     * 添加任务到redis
     * @param task
     */
    void addTask2Redis(Task task);

}
