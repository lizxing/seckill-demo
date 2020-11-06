package com.lizxing.seckilldemo.mapper;

import com.lizxing.seckilldemo.entity.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lizxing
 * @since 2020-11-04
 */
@Repository
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
    @Update("update goods set goods_stock = goods_stock - 1 where id =#{goodsId}")
    int reduceStock(String goodsId);

    @Update("update goods set goods_stock = goods_stock + 1 where id =#{goodsId}")
    int addStock(String goodsId);
}
