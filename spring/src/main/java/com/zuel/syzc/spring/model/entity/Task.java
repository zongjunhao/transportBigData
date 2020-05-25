package com.zuel.syzc.spring.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
@TableName(value = "task")
public class Task {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer taskid;
    private Date startTime;
    private Date endTime;
    private String status;
    private String params;
    private String taskType;
}
