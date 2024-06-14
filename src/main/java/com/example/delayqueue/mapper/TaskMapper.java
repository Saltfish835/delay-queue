package com.example.delayqueue.mapper;

import com.example.delayqueue.bean.Task;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper {

    void insertTask(Task task);
}
