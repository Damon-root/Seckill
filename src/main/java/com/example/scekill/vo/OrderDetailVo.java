package com.example.scekill.vo;

import com.example.scekill.pojo.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailVo {
    private Order order;

    private GoodsVo goodsVo;
}
