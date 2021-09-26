package com.example.scekill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.scekill.pojo.Goods;
import com.example.scekill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author mzc
 * @since 2021-07-25
 */
public interface IGoodsService extends IService<Goods> {

    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
