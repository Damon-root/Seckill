package com.example.scekill.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author mzc
 * @since 2021-07-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String nickname;

    /**
     * MD5(MD5(PASS 明文+固定salt)+salt)
     */
    private String password;

    private String slat;

    /**
     * 头像
     */
    private String head;

    private Date registerDate;

    private Date lastLoginDate;

    private Integer loginCount;


}
