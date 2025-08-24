package com.ryan.deadlock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户表实体类 - 用于模拟转账死锁场景
 */
@Data
@TableName("account")
public class Account {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    @TableField("account_no")
    private String accountNo;
    
    @TableField("account_name")
    private String accountName;
    
    @TableField("balance")
    private BigDecimal balance;
    
    @Version
    @TableField("version")
    private Integer version;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    public Account() {}
    
    public Account(String accountNo, String accountName, BigDecimal balance) {
        this.accountNo = accountNo;
        this.accountName = accountName;
        this.balance = balance;
    }
}