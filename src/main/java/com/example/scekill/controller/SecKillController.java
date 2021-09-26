package com.example.scekill.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.scekill.annotation.AccessLimit;
import com.example.scekill.exception.GlobalException;
import com.example.scekill.pojo.Order;
import com.example.scekill.pojo.SeckillMessage;
import com.example.scekill.pojo.SeckillOrder;
import com.example.scekill.pojo.User;
import com.example.scekill.rabbitmq.MQSender;
import com.example.scekill.service.IGoodsService;
import com.example.scekill.service.IOrderService;
import com.example.scekill.service.ISeckillOrderService;
import com.example.scekill.service.impl.GoodsServiceImpl;
import com.example.scekill.utils.GsonUtils;
import com.example.scekill.vo.GoodsVo;
import com.example.scekill.vo.RespBean;
import com.example.scekill.vo.RespBeanEnum;
import com.google.gson.Gson;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
@RequestMapping("/seckill")
public class SecKillController implements InitializingBean{
    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQSender mqSender;

    private Map<Long,Boolean> EmptyStockMap = new HashMap<>();




    //秒杀静态化前
//    @RequestMapping("/doSeckill")
//    public  String doSeckill(Model model, User user, Long goodsId){
//        if(user == null){
//            return "login";
//        }
//        model.addAttribute("user",user);
//        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
//        if(goods.getStockCount()<1){
//            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
//            return "secKillFail";
//        }
//        //判断是否重复抢购
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
//                .eq("user_id", user.getId()).eq("goods_id", goodsId));
//
//        if(seckillOrder!=null){
//            model.addAttribute("errmsg",RespBeanEnum.REPEATE_ERROR.getMessage());
//        }
//        Order order =  orderService.secKill(user,goods);
//        model.addAttribute("order",order);
//        model.addAttribute("goods",goods);
//        return "orderDetail";
//    }
    /**
     * 获取秒杀结果
     * @param user
     * @param goodsId
     * @return orderId:成功， -1：秒杀失败， 0：秒杀中
     */
    @GetMapping("/result")
    @ResponseBody
    public RespBean getResult(User user, Long goodsId){
        if (user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }


    /**
     *缓存前QPS： 785
     * 缓存QPS：1356
     * 异步下单优化QPS：2454
     */

    @PostMapping(value = "/{path}/doSeckill")
    @ResponseBody
    public RespBean doSeckill(@PathVariable("path") String path,User user, Long goodsId) {
        if(user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();
        boolean check = orderService.checkPath(user, goodsId,path);
        if(!check){
            return  RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().
                get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.LIMIT_ERROR);
        }
        //内存标记，减少redis访问
        if(EmptyStockMap.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //预减少库存
        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        if(stock<0){
            EmptyStockMap.put(goodsId,true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 进行下单
        SeckillMessage seckillMessage = new SeckillMessage(user,goodsId);
        mqSender.sendSeckillMessage(GsonUtils.toJson(seckillMessage));
        return RespBean.success(0);
        /*
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goods.getStockCount() < 1) {
            // 秒杀库存小于1
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 判断订单， 用户只能下一次单(直接操作数据库判断是否重复下单)
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().
//                eq("user_id", user.getId()).
//                eq("goods_id", goodsId));
        // 改为从redis获取数据，更快（通过redis判断是否重复下单）
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().
                get("order:" + user.getId() + ":" + goods.getId());
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.LIMIT_ERROR);
        }
        // 进行下单
        Order order = orderService.secKill(user, goods);
        return RespBean.success(order);
         */
    }

    /**
     * 获取真正的秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    @AccessLimit(second = 5, maxCount = 5,needLogin = true)
    @GetMapping("/path")
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request){
        if (user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
//        // 限流， 限制访问次数五秒内最多5次， 计数器算法
//        String uri = request.getRequestURI();
//        System.out.println(uri);
//        // 从redis判断5s内是否访问
//        Integer count = (Integer) redisTemplate.opsForValue().get(uri + ":" + user.getId());
//        if (count == null){
//            // 第一次访问， 那么添加进redis
//            redisTemplate.opsForValue().set(uri + ":" + user.getId(), 1, 5, TimeUnit.SECONDS);
//        } else if (count < 5){
//            // 请求次数加一
//            redisTemplate.opsForValue().increment(uri + ":" + user.getId());
//        } else {
//            // 不能请 求成功
//            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REACHED);
//        }

        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if (!check){
            return RespBean.error(RespBeanEnum.CAPTCHA_ERROR);
        }
        String str = orderService.creatPath(user, goodsId);
        return RespBean.success(str);
    }

    @GetMapping("/captcha")
    public void captcha(User user, Long goodsId, HttpServletResponse response){
        if (user == null){
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        // 设置请求头为输出图片类型
        response.setContentType("image/jpg");
        response.setHeader("Pargam", "No-cache");
        response.setHeader("Cache-Control", "No-cache");
        response.setDateHeader("Expires", 0);
        // 生成验证码, 并存入redis
        ArithmeticCaptcha arithmeticCaptcha = new ArithmeticCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, arithmeticCaptcha.text(), 300, TimeUnit.SECONDS);
        try {
            arithmeticCaptcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败：", e.getMessage());
        }
    }


    //初始化  加载商品数量到redis中
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        list.forEach(goodsVo -> {
                redisTemplate.opsForValue().set("seckillGoods:"+ goodsVo.getId(),goodsVo.getStockCount());
                EmptyStockMap.put(goodsVo.getId(),false);
            }
        );

    }
}
