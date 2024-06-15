package com.example.delayqueue.controller;

import com.example.delayqueue.bean.Task;
import com.example.delayqueue.service.DelayTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController
@RequestMapping("/delayQueue")
public class DelayQueueController {

    @Autowired
    DelayTaskService delayTaskService;

    @PostMapping("/addTask")
    public Long addTask(Task task) {
        final long taskId = delayTaskService.addTask(task);
        return taskId;
    }

}
