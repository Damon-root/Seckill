package com.example.scekill.vo;

import com.example.scekill.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * project name: seckill
 *
 * Date: 2021/5/25 10:14 上午
 * @Description: 详情返回对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailVo {

    private User user;

    private GoodsVo goodsVo;

    private int seckillStatus;

    private int remainSeconds;
}
