package com.example.scekill.controller;


import com.example.scekill.rabbitmq.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author mzc
 * @since 2021-07-17
 */
@Controller
@RequestMapping("/user")
public class UserController {
//    @Autowired
//    private MQSender mqSender;
//
//    //测试rabbitmq
//    @RequestMapping("/mq")
//    @ResponseBody
//    public  void mq(){
//        mqSender.send01("Hello");
//    }
//
//
//    //direct模式
//    @RequestMapping("/mq/direct01")
//    @ResponseBody
//    public  void mq01(){
//        mqSender.send02("Hello,Red");
//    }
//
//
//    @RequestMapping("/mq/direct02")
//    @ResponseBody
//    public  void mq02(){
//        mqSender.send03("Hello,Green");
//    }
}


