package com.zuel.syzc.spring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zuel.syzc.spring.dao.UserDao;
import com.zuel.syzc.spring.model.entity.User;
import com.zuel.syzc.spring.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemServiceImpl implements SystemService {
    @Autowired
    private UserDao userDao;

    @Override
    public User login(User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name",user.getName());
        queryWrapper.eq("password",user.getPassword());
        return userDao.selectOne(queryWrapper);
    }

    @Override
    public boolean register(User user) {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("name",user.getName());
        List<User> users = userDao.selectList(userQueryWrapper);
        if (users.size()>0) return false;
        int insert = userDao.insert(user);
        return insert>0;
    }

    @Override
    public boolean modifyInfo(User user) {
        int i = userDao.updateById(user);
        return i > 0;
    }
}
