package com.example.delayqueue.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.util.Arrays;


@TableName("task_log")
public class TaskLog {

    @TableId
    private Long id;

    @TableField("execute_time")
    private Long executeTime;

    @TableField("task_name")
    private String taskName;

    @TableField("parameters")
    private byte[] parameters;

    @TableField("priority")
    private Integer priority;

    @TableField("task_type")
    private Integer taskType;

    @TableField("task_owner")
    private String taskOwner;

    @Version
    private Integer version;

    @TableField("status")
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Long executeTime) {
        this.executeTime = executeTime;
    }

    public byte[] getParameters() {
        return parameters;
    }

    public void setParameters(byte[] parameters) {
        this.parameters = parameters;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    public String getTaskOwner() {
        return taskOwner;
    }

    public void setTaskOwner(String taskOwner) {
        this.taskOwner = taskOwner;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @Override
    public String toString() {
        return "TaskLog{" +
                "id=" + id +
                ", executeTime=" + executeTime +
                ", taskName='" + taskName + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                ", priority=" + priority +
                ", taskType=" + taskType +
                ", taskOwner='" + taskOwner + '\'' +
                ", version=" + version +
                ", status=" + status +
                '}';
    }
}
