package com.example.scekill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.scekill.exception.GlobalException;
import com.example.scekill.mapper.UserMapper;
import com.example.scekill.pojo.User;
import com.example.scekill.service.IUserService;
import com.example.scekill.utils.CookieUtil;
import com.example.scekill.utils.MD5Util;
import com.example.scekill.utils.UUIDUtil;
import com.example.scekill.vo.LoginVo;
import com.example.scekill.vo.RespBean;
import com.example.scekill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author mzc
 * @since 2021-07-17
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private  UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile  = loginVo.getMobile();
        String password = loginVo.getPassword();
//        //参数校验
//        if(StringUtils.isEmpty(mobile)||StringUtils.isEmpty(password)){
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
//        }
//        if(!ValidatorUtil.isMobile(mobile)){
//            return  RespBean.error(RespBeanEnum.MOBILE_ERROR);
//        }
        //根据手机号获得用户
        User user = userMapper.selectById(mobile);
        if(user == null){
           throw  new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        //判断密码是否真确
        if(!MD5Util.fromPassToDBPass(password,user.getSlat()).equals(user.getPassword())){
           // return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw  new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        //生成cookie
        String ticket = UUIDUtil.uuid();
        //request.getSession().setAttribute(ticket,user);

        redisTemplate.opsForValue().set("user:"+ticket,user);
        CookieUtil.setCookie(request,response,"userTicket",ticket);
        return  RespBean.success(ticket);
    }

    @Override
    public User getUserByCookie(String userTicket,HttpServletRequest request,HttpServletResponse response) {
        if(StringUtils.isEmpty(userTicket)){
            return  null;
        }
        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
        if(user!=null){
            CookieUtil.setCookie(request,response,"userTicket",userTicket);
        }
        return user;
    }



    //更新密码
    @Override
    public RespBean updatePassword(String userTicket, Long id, String password,HttpServletRequest request,HttpServletResponse response) {
        User user = getUserByCookie(userTicket,request,response);
        if(user == null){
            throw  new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
        }
        user.setPassword(MD5Util.fromPassToDBPass(password,user.getSlat()));
        int result = userMapper.updateById(user);
        if(result == 1){
            //删除redis 失效user信息
            redisTemplate.delete("user:"+userTicket);
            return  RespBean.success();
        }
        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }
}
