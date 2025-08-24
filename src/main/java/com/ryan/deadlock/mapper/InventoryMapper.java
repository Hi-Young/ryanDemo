package com.ryan.deadlock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ryan.deadlock.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 库存Mapper - 用于模拟库存扣减死锁场景
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {
    
    /**
     * 根据商品ID查询库存(加行锁)
     */
    @Select("SELECT * FROM inventory WHERE product_id = #{productId} FOR UPDATE")
    Inventory selectByProductIdForUpdate(@Param("productId") Integer productId);
    
    /**
     * 批量根据商品ID查询库存(加行锁) - 按ID升序排列避免死锁
     */
    @Select("SELECT * FROM inventory WHERE product_id IN " +
            "<foreach collection='productIds' item='id' open='(' close=')' separator=','> #{id} </foreach> " +
            "ORDER BY product_id FOR UPDATE")
    List<Inventory> selectByProductIdsForUpdate(@Param("productIds") List<Integer> productIds);
    
    /**
     * 扣减库存(悲观锁)
     */
    @Update("UPDATE inventory SET stock_quantity = stock_quantity - #{quantity}, update_time = NOW() " +
            "WHERE product_id = #{productId} AND stock_quantity >= #{quantity}")
    int reduceStock(@Param("productId") Integer productId, @Param("quantity") Integer quantity);
    
    /**
     * 扣减库存(乐观锁)
     */
    @Update("UPDATE inventory SET stock_quantity = stock_quantity - #{quantity}, version = version + 1, update_time = NOW() " +
            "WHERE product_id = #{productId} AND version = #{version} AND stock_quantity >= #{quantity}")
    int reduceStockWithVersion(@Param("productId") Integer productId, @Param("quantity") Integer quantity, @Param("version") Integer version);
    
    /**
     * 预留库存
     */
    @Update("UPDATE inventory SET reserved_quantity = reserved_quantity + #{quantity}, update_time = NOW() " +
            "WHERE product_id = #{productId} AND stock_quantity - reserved_quantity >= #{quantity}")
    int reserveStock(@Param("productId") Integer productId, @Param("quantity") Integer quantity);
    
    /**
     * 释放预留库存
     */
    @Update("UPDATE inventory SET reserved_quantity = reserved_quantity - #{quantity}, update_time = NOW() " +
            "WHERE product_id = #{productId} AND reserved_quantity >= #{quantity}")
    int releaseReservedStock(@Param("productId") Integer productId, @Param("quantity") Integer quantity);
    
    /**
     * 检查库存是否充足
     */
    @Select("SELECT COUNT(1) FROM inventory WHERE product_id = #{productId} AND stock_quantity - reserved_quantity >= #{quantity}")
    int checkStock(@Param("productId") Integer productId, @Param("quantity") Integer quantity);
}