package com.zuel.syzc.spring.controller;

import com.zuel.syzc.spring.constant.enums.ResultCodeEnum;
import com.zuel.syzc.spring.model.entity.User;
import com.zuel.syzc.spring.model.vo.ResultData;
import com.zuel.syzc.spring.service.SystemService;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@RestController
@RequestMapping("system")
public class SystemController {

    private ResultData resultData;
    @Autowired
    private SystemService systemService;
    @RequestMapping("login")
    public ResultData login(String name, String password, HttpServletRequest request) {
//        System.out.println(name+"-"+password);
        resultData = new ResultData();
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        user = systemService.login(user);
        if (user!=null){
            HttpSession session = request.getSession();
            resultData.setResult(ResultCodeEnum.OK);
            resultData.setData(user);
            session.setAttribute("user",user);
        } else {
            resultData.setResult(ResultCodeEnum.LOGIN_ERROR);
        }
        return resultData;
    }

    @RequestMapping("logout")
    public ResultData logout(Long id, HttpSession session){

        if(id!=null){//判断是否为空字符串
            resultData = new ResultData();
            if(session.getAttribute("user")==null){//获取属性
                resultData.setResult(ResultCodeEnum.NO_LOGIN_USER);
            } else {
                session.removeAttribute("user");
                resultData.setResult(ResultCodeEnum.LOGOUT_SUCCESS);//成功退出
            }
        }else{
            resultData = new ResultData();
            resultData.setResult(ResultCodeEnum.PARA_WORNING_NULL);
        }
        return resultData;
    }

    @RequestMapping("modifyInfo")
    public ResultData modifyInfo(Integer id, String password, HttpSession session) {
        if(id != null ) {
            try{
                User user = new User();
                user.setId(id);
                user.setPassword(password);
                boolean b = systemService.modifyInfo(user);
                if (b) {
                    resultData.setResult(ResultCodeEnum.DB_UPDATE_SUCCESS);
                } else {
                    resultData.setResult(ResultCodeEnum.DB_UPDATE_ERROR);
                }
            }catch(Exception e){
                e.printStackTrace();
                resultData = new ResultData();
                resultData.setResult(ResultCodeEnum.SERVER_ERROR);
            }
        } else {
            resultData = new ResultData();
            resultData.setResult(ResultCodeEnum.PARA_WORNING_NULL);
        }
        return resultData;
    }

    @RequestMapping("register")
    public ResultData register(String name, String password){
        resultData = new ResultData();
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        boolean register = systemService.register(user);
        if (register) {
            resultData.setResult(ResultCodeEnum.REGISTER_SUCCESS);
        } else {
            resultData.setResult(ResultCodeEnum.USER_HAVE_REGISTER);
        }
        return resultData;
    }
}
