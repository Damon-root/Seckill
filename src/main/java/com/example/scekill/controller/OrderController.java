package com.example.scekill.controller;


import com.example.scekill.pojo.User;
import com.example.scekill.service.IOrderService;
import com.example.scekill.vo.OrderDetailVo;
import com.example.scekill.vo.RespBean;
import com.example.scekill.vo.RespBeanEnum;
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
 * @since 2021-07-25
 */
@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private IOrderService orderService;

    @RequestMapping("/detail")
    @ResponseBody
    public RespBean detail(User user, Long orderId){
        if(user == null){
            return  RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        OrderDetailVo detail = orderService.detail(orderId);
        return RespBean.success(detail);
    }
}
