package com.example.delayqueue.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.delayqueue.bean.Task;
import com.example.delayqueue.bean.TaskLog;
import com.example.delayqueue.common.Constance;
import com.example.delayqueue.mapper.TaskMapper;
import com.example.delayqueue.service.DelayTaskLogService;
import com.example.delayqueue.service.DelayTaskService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
@Transactional
public class DelayTaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements DelayTaskService {

    private static final Logger logger = LoggerFactory.getLogger(DelayTaskServiceImpl.class);

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    DelayTaskLogService delayTaskLogService;



    /**
     * 添加延时任务
     * @param task
     * @return
     */
    @Override
    public long addTask(Task task) {
        // 添加任务到数据库
        final boolean success = addTask2DB(task);
        // 添加任务到redis
        if(success) {
            addTask2Redis(task);
        }
        return task.getId();
    }


    /**
     * 取消任务
     * @param taskId
     * @return
     */
    @Override
    public boolean cancelTask(long taskId) {
        try {
            // 删除任务，更新任务日志
            final Task task = updateTask(taskId, Constance.CANCELED);
            // 删除redis中的数据
            if(task != null) {
                removeTaskFromRedis(task);
            }
            return true;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 判断任务是否取消
     * @param taskId
     * @return
     */
    @Override
    public boolean isCanceled(long taskId) {
        final TaskLog taskLog = delayTaskLogService.getById(taskId);
        if (taskLog == null) {
            logger.warn("taskLog not found, taskLog: {}", taskLog);
            return false;
        }
        if(taskLog.getStatus() == Constance.CANCELED) {
            return true;
        }else {
            return false;
        }
    }


    /**
     * 拉取任务
     * @param taskOwner
     * @return
     */
    @Override
    public Task pullTask(String taskOwner) {
        // 从list中拉取任务
        final String taskJsonStr = redisTemplate.opsForList().rightPop(Constance.APP_NAME + ":" + Constance.CURRENT + ":" + taskOwner);
        if(StringUtils.isEmpty(taskJsonStr)) {
            return null;
        }
        Task task = new Gson().fromJson(taskJsonStr, Task.class);
        updateTask(task.getId(), Constance.EXECUTED);
        logger.info("pull task, task:{}",task);
        return task;
    }


    /**
     * 批量拉取任务
     * @param taskOwner
     * @return
     */
    public List<Task> pullTasks(String taskOwner) {
        List<Task> taskList = new ArrayList<>();
        while(true) {
            final Task task = pullTask(taskOwner);
            if(task == null) {
                break;
            }else {
                taskList.add(task);
            }
        }
        logger.info("pull task list, task list:{}", taskList.toArray().toString());
        return taskList;
    }


    /**
     * 删除redis中的任务
     * @param task
     */
    private void removeTaskFromRedis(Task task) {
        Gson gson = new Gson();
        // 删除当前要执行的任务
        if(task.getExecuteTime() <= System.currentTimeMillis()) {
            redisTemplate.opsForList().remove(Constance.APP_NAME + ":" + Constance.CURRENT + ":" + task.getTaskOwner(), 0, gson.toJson(task));
            logger.info("remove task from redis list, task:{}", task);
        }else {
            // 删除未来将要执行的任务
            redisTemplate.opsForZSet().remove(Constance.APP_NAME + ":" + Constance.FUTURE + ":" + task.getTaskOwner(), gson.toJson(task));
            logger.info("remove task from redis zSet, task:{}", task);
        }
    }


    /**
     * 更新任务状态
     * @param taskId
     * @param status
     * @return
     */
    private Task updateTask(long taskId, int status) {
        final Task task = getById(taskId);
        // 删除任务
        removeById(taskId);
        // 更新任务日志
        final TaskLog taskLog = delayTaskLogService.getById(taskId);
        taskLog.setStatus(status);
        delayTaskLogService.updateById(taskLog);
        logger.info("update task status, taskId:{}, status:{}", taskId, status);
        return task;
    }


    /**
     * 添加任务到数据库
     * @param task
     * @return
     */
    private boolean addTask2DB(Task task) {
        try {
            // 保存到task表
            save(task);
            // 保存到task_log表
            TaskLog taskLog = new TaskLog();
            BeanUtils.copyProperties(task, taskLog);
            taskLog.setVersion(1); // 设置版本号
            taskLog.setStatus(Constance.INIT);
            delayTaskLogService.save(taskLog);
            logger.info("add task to db, task: {}", task);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 添加任务到redis
     * @param task
     */
    public void addTask2Redis(Task task) {
        Gson gson = new Gson();
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        // 如果任务执行时间小于等于当前时间，则需要将任务直接放入list中
        if(task.getExecuteTime() <= System.currentTimeMillis()) {
            redisTemplate.opsForList().leftPush(Constance.APP_NAME + ":" + Constance.CURRENT + ":" + task.getTaskOwner(), gson.toJson(task));
            logger.info("add task to redis list, task:{}", task);
        }else if(task.getExecuteTime() <= calendar.getTimeInMillis()) {
            // 如果任务执行时间大于当前时间，且小于等于未来五分钟，将任务放入zset中
            redisTemplate.opsForZSet().add(Constance.APP_NAME + ":" + Constance.FUTURE + ":" + task.getTaskOwner(), gson.toJson(task), task.getExecuteTime());
            logger.info("add task to redis zSet, task:{}", task);
        }
    }
}
