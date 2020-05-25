package com.zuel.syzc.spring.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zuel.syzc.spring.model.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface TaskDao extends BaseMapper<Task> {
    // @Insert("insert into task(start_time, status, task_type, params) values(#{startTime} ,#{status} ,#{taskType}, #{params})")
    // int addOneTask(Task task);

}
