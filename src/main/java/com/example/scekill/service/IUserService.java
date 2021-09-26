package com.example.scekill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.scekill.pojo.User;
import com.example.scekill.vo.LoginVo;
import com.example.scekill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author mzc
 * @since 2021-07-17
 */
public interface IUserService extends IService<User> {
     RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    User getUserByCookie(String userTicket,HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse);

    RespBean updatePassword(String userTicket,Long id,String password,HttpServletRequest request,HttpServletResponse response);
}
