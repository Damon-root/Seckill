package com.example.scekill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.scekill.pojo.Order;
import com.example.scekill.pojo.User;
import com.example.scekill.vo.GoodsVo;
import com.example.scekill.vo.OrderDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author mzc
 * @since 2021-07-25
 */
public interface IOrderService extends IService<Order> {

    Order secKill(User user, GoodsVo goods);


    //订单详情
    OrderDetailVo detail(Long orderId);

    String creatPath(User user, Long goodsId);

    boolean checkPath(User user, Long goodsId, String path);

    boolean checkCaptcha(User user, Long goodsId, String captcha);
}
