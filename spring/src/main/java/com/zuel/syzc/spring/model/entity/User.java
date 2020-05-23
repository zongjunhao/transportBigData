package com.zuel.syzc.spring.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class User {
    @TableId(type = IdType.INPUT)
    private Integer id;
    private String name;
    private String password;
}
