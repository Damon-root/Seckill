package com.example.scekill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.scekill.pojo.SeckillOrder;
import com.example.scekill.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author mzc
 * @since 2021-07-25
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {

    /**
     * 获取秒杀结果
     * @param user
     * @param goodsId
     * @return
     */
    Long getResult(User user, Long goodsId);
}
