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




    //??????????????????
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
//        //????????????????????????
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
     * ??????????????????
     * @param user
     * @param goodsId
     * @return orderId:????????? -1?????????????????? 0????????????
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
     *?????????QPS??? 785
     * ??????QPS???1356
     * ??????????????????QPS???2454
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
        //????????????????????????
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().
                get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.LIMIT_ERROR);
        }
        //?????????????????????redis??????
        if(EmptyStockMap.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //???????????????
        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        if(stock<0){
            EmptyStockMap.put(goodsId,true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // ????????????
        SeckillMessage seckillMessage = new SeckillMessage(user,goodsId);
        mqSender.sendSeckillMessage(GsonUtils.toJson(seckillMessage));
        return RespBean.success(0);
        /*
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goods.getStockCount() < 1) {
            // ??????????????????1
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // ??????????????? ????????????????????????(?????????????????????????????????????????????)
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().
//                eq("user_id", user.getId()).
//                eq("goods_id", goodsId));
        // ?????????redis??????????????????????????????redis???????????????????????????
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().
                get("order:" + user.getId() + ":" + goods.getId());
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.LIMIT_ERROR);
        }
        // ????????????
        Order order = orderService.secKill(user, goods);
        return RespBean.success(order);
         */
    }

    /**
     * ???????????????????????????
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
//        // ????????? ?????????????????????????????????5?????? ???????????????
//        String uri = request.getRequestURI();
//        System.out.println(uri);
//        // ???redis??????5s???????????????
//        Integer count = (Integer) redisTemplate.opsForValue().get(uri + ":" + user.getId());
//        if (count == null){
//            // ?????????????????? ???????????????redis
//            redisTemplate.opsForValue().set(uri + ":" + user.getId(), 1, 5, TimeUnit.SECONDS);
//        } else if (count < 5){
//            // ??????????????????
//            redisTemplate.opsForValue().increment(uri + ":" + user.getId());
//        } else {
//            // ????????? ?????????
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
        // ????????????????????????????????????
        response.setContentType("image/jpg");
        response.setHeader("Pargam", "No-cache");
        response.setHeader("Cache-Control", "No-cache");
        response.setDateHeader("Expires", 0);
        // ???????????????, ?????????redis
        ArithmeticCaptcha arithmeticCaptcha = new ArithmeticCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, arithmeticCaptcha.text(), 300, TimeUnit.SECONDS);
        try {
            arithmeticCaptcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("????????????????????????", e.getMessage());
        }
    }


    //?????????  ?????????????????????redis???
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
