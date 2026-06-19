package com.weple.cloud.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CommonController {

    @GetMapping("/")
    public String home() {
        return "weple/common";
    }

    @GetMapping("/weple/admin")
    public String adminPage() {
        return "weple/admin/group/list"; 
    }
    
    @GetMapping("/{page}.html")
    public String page(@PathVariable String page) {
        return "dashboard/" + page;
    }

}

