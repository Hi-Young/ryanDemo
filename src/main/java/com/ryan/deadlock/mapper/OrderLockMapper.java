package com.ryan.deadlock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ryan.deadlock.entity.OrderLock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 订单锁Mapper - 用于模拟订单处理死锁场景
 */
@Mapper
public interface OrderLockMapper extends BaseMapper<OrderLock> {
    
    /**
     * 根据订单号查询订单(加行锁)
     */
    @Select("SELECT * FROM order_locks WHERE order_no = #{orderNo} FOR UPDATE")
    OrderLock selectByOrderNoForUpdate(@Param("orderNo") String orderNo);
    
    /**
     * 根据用户ID查询待处理订单(加行锁)
     */
    @Select("SELECT * FROM order_locks WHERE user_id = #{userId} AND status = 0 ORDER BY id FOR UPDATE")
    List<OrderLock> selectPendingOrdersByUserIdForUpdate(@Param("userId") Integer userId);
    
    /**
     * 批量根据订单号查询订单(加行锁) - 按订单号排序避免死锁
     */
    @Select("SELECT * FROM order_locks WHERE order_no IN " +
            "<foreach collection='orderNos' item='orderNo' open='(' close=')' separator=','> #{orderNo} </foreach> " +
            "ORDER BY order_no FOR UPDATE")
    List<OrderLock> selectByOrderNosForUpdate(@Param("orderNos") List<String> orderNos);
    
    /**
     * 更新订单状态
     */
    @Update("UPDATE order_locks SET status = #{status}, process_time = NOW(), update_time = NOW() " +
            "WHERE order_no = #{orderNo}")
    int updateOrderStatus(@Param("orderNo") String orderNo, @Param("status") Integer status);
    
    /**
     * 查询指定时间范围内的订单(可能产生Gap锁)
     */
    @Select("SELECT * FROM order_locks WHERE create_time BETWEEN #{startTime} AND #{endTime} ORDER BY create_time for update")
    List<OrderLock> selectOrdersByTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    /**
     * 插入订单并返回ID(可能产生插入意向锁)
     */
    @Select("INSERT INTO order_locks (order_no, user_id, product_ids, total_amount, status) " +
            "VALUES (#{orderNo}, #{userId}, #{productIds}, #{totalAmount}, #{status})")
    int insertOrderLock(OrderLock orderLock);
    
    /**
     * 按用户ID范围更新订单状态 - 产生Gap锁
     * 更新所有user_id > minUserId的订单状态为处理中(status=1)
     */
    @Update("UPDATE order_locks SET status = 1, process_time = NOW(), update_time = NOW() " +
            "WHERE user_id > #{minUserId}")
    int updateOrderStatusByUserIdRange(@Param("minUserId") Integer minUserId);
}