package com.example.scekill.rabbitmq;

import com.example.scekill.pojo.SeckillMessage;
import com.example.scekill.pojo.SeckillOrder;
import com.example.scekill.pojo.User;
import com.example.scekill.service.IGoodsService;
import com.example.scekill.service.IOrderService;
import com.example.scekill.vo.GoodsVo;
import com.example.scekill.vo.RespBean;
import com.example.scekill.vo.RespBeanEnum;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQReceiver {
    @Autowired
    private Gson gson;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IOrderService orderService;

    @RabbitListener(queues = "seckillQueue")
    public void receive(String message){
        log.info("接收消息：",message);
        SeckillMessage seckillMessage = gson.fromJson(message, SeckillMessage.class);
        Long goodId = seckillMessage.getGoodId();
        User user = seckillMessage.getUser();
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodId);
        if(goodsVo.getStockCount()<1){
            return;
        }
        //判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().
                get("order:" + user.getId() + ":" + goodId);
        if (seckillOrder != null) {
            return ;
        }
        //下单操作
        orderService.secKill(user,goodsVo);

    }

}
