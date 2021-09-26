package com.example.scekill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.scekill.pojo.Goods;
import com.example.scekill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author mzc
 * @since 2021-07-25
 */
public interface GoodsMapper extends BaseMapper<Goods> {

    List<GoodsVo> findGoodsVo();


    //获取商品详情
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
