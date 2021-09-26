package com.example.scekill.interceptor;


import com.example.scekill.annotation.AccessLimit;
import com.example.scekill.exception.GlobalException;
import com.example.scekill.pojo.User;
import com.example.scekill.service.IUserService;
import com.example.scekill.utils.CookieUtil;
import com.example.scekill.vo.RespBeanEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * project name: seckill
 *
 * @Description: 限流拦截器
 */
@Component
public class AccessInterceptor implements HandlerInterceptor {
    @Autowired
    private IUserService userService;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){
            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null){
                // 拦截的方法上没有该注解， 放行
                return true;
            }
            // 有该注解， 进行限流操作
            int second = accessLimit.second();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            User user = null;
            if (needLogin){
                String ticket = CookieUtil.getCookieValue(request, "userTicket");
                if (StringUtils.isEmpty(ticket)) {
                    throw new GlobalException(RespBeanEnum.NOT_LOGIN_ERROR);
                }
                user = userService.getUserByCookie(ticket, request, response);
                if (user == null){
                    throw new GlobalException(RespBeanEnum.SESSION_ERROR);
                }
            }
            // 获取uri进行限流， 限制访问次数五秒内最多5次， 计数器算法
            String uri = request.getRequestURI();
            // 从redis判断5s内是否访问
            Integer count = (Integer) redisTemplate.opsForValue().get(uri + ":" + user.getId());
            if (count == null){
                // 第一次访问， 那么添加进redis
                redisTemplate.opsForValue().set(uri + ":" + user.getId(), 1, second, TimeUnit.SECONDS);
            } else if (count < maxCount){
                // 请求次数加一
                redisTemplate.opsForValue().increment(uri + ":" + user.getId());
            } else {
                // 不能请求成功
                throw new GlobalException(RespBeanEnum.ACCESS_LIMIT_REACHED);
            }
        }
        return true;
    }

}
