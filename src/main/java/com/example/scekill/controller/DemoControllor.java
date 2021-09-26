package com.example.scekill.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/demo")
public class DemoControllor {
    @RequestMapping("/hello")
    public String hello(Model model){
        model.addAttribute("name","xxx");
        return "hello";
    }
}
