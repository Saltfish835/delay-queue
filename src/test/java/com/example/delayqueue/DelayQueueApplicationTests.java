package com.example.delayqueue;

import com.example.delayqueue.bean.Task;
import com.example.delayqueue.job.TransferJob;
import com.example.delayqueue.service.DelayTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Set;


@SpringBootTest
class DelayQueueApplicationTests {

    @Autowired
    DelayTaskService delayTaskService;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    TransferJob transferJob;

    @Test
    public void testAddTask() {
        for(int i=0; i<10; i++) {
            final Task task = new Task();
            task.setExecuteTime(System.currentTimeMillis() + i*60*1000);
            task.setPriority(1);
            task.setTaskType(1);
            task.setTaskOwner("yuhe");
            task.setTaskName("test");
            task.setParameters(null);
            final long addTaskId = delayTaskService.addTask(task);
            System.out.println(addTaskId);
        }
    }


    @Test
    public void testCancelTask() {
        final boolean cancelTask = delayTaskService.cancelTask(2);
        System.out.println(cancelTask);
    }


    @Test
    public void testPullTask() {
        String taskOwner = "exportReport";
        final Task task = delayTaskService.pullTask(taskOwner);
        System.out.println(task);
    }

    @Test
    public void testPullTasks() {
        String taskOwner = "exportReport";
        final List<Task> taskList = delayTaskService.pullTasks(taskOwner);
        System.out.println(taskList);
    }

    @Test
    public void testRedis() {
        redisTemplate.opsForValue().set("delay:test","test value");
    }


    @Test
    public void testScan() {
        final Set<String> keys = transferJob.scan("future*");
        System.out.println(keys);
    }
}
