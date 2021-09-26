package com.example.scekill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.scekill.exception.GlobalException;
import com.example.scekill.mapper.OrderMapper;
import com.example.scekill.mapper.SeckillGoodsMapper;
import com.example.scekill.mapper.SeckillOrderMapper;
import com.example.scekill.pojo.Order;
import com.example.scekill.pojo.SeckillGoods;
import com.example.scekill.pojo.SeckillOrder;
import com.example.scekill.pojo.User;
import com.example.scekill.service.IGoodsService;
import com.example.scekill.service.IOrderService;
import com.example.scekill.service.ISeckillGoodsService;
import com.example.scekill.utils.MD5Util;
import com.example.scekill.utils.UUIDUtil;
import com.example.scekill.vo.GoodsVo;
import com.example.scekill.vo.OrderDetailVo;
import com.example.scekill.vo.RespBeanEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author mzc
 * @since 2021-07-25
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    @Autowired
    private ISeckillGoodsService seckillGoodsService;


    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;
    //秒杀


    @Override
    @Transactional
    public Order secKill(User user, GoodsVo goods) {
        // 秒杀商品表减库存
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().
                eq("goods_id", goods.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
//        seckillGoodsService.updateById(seckillGoods);
        // 防止库存超卖
        seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().setSql("stock_count = stock_count-1").
                eq("id", seckillGoods.getId()).gt("stock_count", 0));
        if (seckillGoods.getStockCount() < 1){
            // 判断是否还有库存，没库存向redis添加库存为空的消息
            redisTemplate.opsForValue().set("isStockEmpty:" + seckillGoods.getGoodsId(), "0");
            return null;
        }
        // 生成订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(goods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);

        // 生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(goods.getId());
        seckillOrderMapper.insert(seckillOrder);
        // 存入redis
        redisTemplate.opsForValue().set("order:"+user.getId()+":"+goods.getId(),seckillOrder);

        return order;
    }

    @Override
    public OrderDetailVo detail(Long orderId) {
        if(orderId == null){
            throw  new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo detail = new OrderDetailVo();
        detail.setOrder(order);
        detail.setGoodsVo(goodsVo);
        return detail;
    }

    @Override
    public String creatPath(User user, Long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisTemplate.opsForValue().set("seckillPath:" + user.getId() + ":" +goodsId, str, 60, TimeUnit.SECONDS);
        return str;
    }

    /**
     * 校验秒杀地址
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    @Override
    public boolean checkPath(User user, Long goodsId, String path) {
        if (user == null || goodsId < 0 || StringUtils.isEmpty(path)){
            return false;
        }
        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);
        return path.equals(redisPath);
    }

    /**
     * 校验验证码
     * @param user
     * @param goodsId
     * @param captcha
     * @return
     */
    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captcha) {
        if (user == null || goodsId < 0|| StringUtils.isEmpty(captcha)){
            return false;
        }
        String redisCaptcha = (String) redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);
        return captcha.equals(redisCaptcha);
    }
}
