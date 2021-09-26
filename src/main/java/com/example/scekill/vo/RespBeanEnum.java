package com.example.scekill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum RespBeanEnum {


    SUCCESS(200, "SUCCESS"),
    ERROR(500, "服务端异常"),

    LOGIN_ERROR(5001, "用户名或密码错误！"),
    MOBILE_ERROR(5002,"手机号码格式错误"),
    BIND_ERROR(5003,"参数校验异常"),
    NOT_LOGIN_ERROR(5004,"请先登录！"),
    EMPTY_STOCK(5005, "库存不足"),
    LIMIT_ERROR(5006, "该商品每人限购一件"),
    PASSWORD_UPDATE_FAIL(5007, "更新密码失败"),
    SESSION_ERROR(5008, "用户不存在"),
    ORDER_NOT_EXIST(5009, "订单不存在！"),
    CAPTCHA_ERROR(5011, "验证码错误，请重新输入！"),
    REQUEST_ILLEGAL(5010,"请求非法，请重新尝试"),
    ACCESS_LIMIT_REACHED(5012,"请求过快，稍等一下吧！"),
    MOBILE_NOT_EXIST(5011,"手机号不存在");
    private  final  Integer code;
    private  final  String message;
}
