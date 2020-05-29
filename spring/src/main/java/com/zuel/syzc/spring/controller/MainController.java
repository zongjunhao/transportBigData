package com.zuel.syzc.spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("main")
public class MainController {

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String test()
    {
        String index = "11/index";
        return index;
    }

    @RequestMapping(value = "/trackWayAll", method = RequestMethod.GET)
    public String trackWayAll() {
        return "trackWayAll";
    }
}
