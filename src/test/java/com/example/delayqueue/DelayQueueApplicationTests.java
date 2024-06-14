package com.example.delayqueue;

import com.example.delayqueue.bean.Task;
import com.example.delayqueue.mapper.TaskMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DelayQueueApplicationTests {

    @Autowired
    TaskMapper taskMapper;

    @Test
    void testInsertTask() {
        Task task = new Task();
        task.setTaskName("task name");
        task.setTaskType(1);
        task.setTaskContent("task content");
        task.setOwner("test");
        System.out.println("插入前id:"+task.getId());
        taskMapper.insertTask(task);
        System.out.println("插入后id:"+task.getId());
    }

}
