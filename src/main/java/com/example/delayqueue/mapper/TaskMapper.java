package com.example.delayqueue.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.delayqueue.bean.Task;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface TaskMapper extends BaseMapper<Task> {

}
