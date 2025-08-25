package com.ryan.deadlock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ryan.deadlock.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 账户Mapper - 用于模拟转账死锁场景
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {
    
    /**
     * 根据账户号查询账户信息（不加锁）
     */
    @Select("SELECT * FROM account WHERE account_no = #{accountNo}")
    Account selectByAccountNo(@Param("accountNo") String accountNo);
    
    /**
     * 根据账户号查询账户(加行锁)
     */
    @Select("SELECT * FROM account WHERE account_no = #{accountNo} FOR UPDATE")
    Account selectByAccountNoForUpdate(@Param("accountNo") String accountNo);
    
    /**
     * 更新账户余额(悲观锁)
     */
    @Update("UPDATE account SET balance = balance + #{amount}, update_time = NOW() WHERE account_no = #{accountNo}")
    int updateBalanceByAccountNo(@Param("accountNo") String accountNo, @Param("amount") BigDecimal amount);
    
    /**
     * 更新账户余额(乐观锁)
     */
    @Update("UPDATE account SET balance = balance + #{amount}, version = version + 1, update_time = NOW() " +
            "WHERE account_no = #{accountNo} AND version = #{version}")
    int updateBalanceWithVersion(@Param("accountNo") String accountNo, @Param("amount") BigDecimal amount, @Param("version") Integer version);
    
    /**
     * 检查账户余额是否充足
     */
    @Select("SELECT COUNT(1) FROM account WHERE account_no = #{accountNo} AND balance >= #{amount}")
    int checkBalance(@Param("accountNo") String accountNo, @Param("amount") BigDecimal amount);
}