package com.lizxing.seckilldemo.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author lizxing
 * @since 2020-11-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("`order`") //表名与关键字冲突，加上``
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String userId;

    private String goodsId;

    private Integer status;


}
