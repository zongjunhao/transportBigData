package com.zuel.syzc.spring.service;

import com.zuel.syzc.spring.model.entity.User;

public interface SystemService {
    public User login(User user);
//    public void logout();
    public boolean register(User user);
    public boolean modifyInfo(User user);
}
