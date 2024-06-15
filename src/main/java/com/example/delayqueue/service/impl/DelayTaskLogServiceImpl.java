package com.example.delayqueue.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.delayqueue.bean.TaskLog;
import com.example.delayqueue.mapper.TaskLogMapper;
import com.example.delayqueue.service.DelayTaskLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DelayTaskLogServiceImpl extends ServiceImpl<TaskLogMapper, TaskLog> implements DelayTaskLogService {
}
