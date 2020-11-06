package com.lizxing.seckilldemo.mapper;

import com.lizxing.seckilldemo.entity.Order;
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
public interface OrderMapper extends BaseMapper<Order> {

    @Update("update `order` set status = 1 where id =#{orderId}")
    int updateOrderStatus(String orderId);
}
