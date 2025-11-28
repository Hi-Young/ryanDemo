package com.ryan.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * MyBatis 拦截器
 * 功能：
 * 1. 自动填充 name、createTime、updateTime 字段
 * 2. 检查查询语句必须有 WHERE 或 LIMIT 子句
 */
@Slf4j
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
        org.apache.ibatis.session.RowBounds.class, org.apache.ibatis.session.ResultHandler.class})
})
public class AutoFillAndQueryCheckInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();

        // 处理 INSERT 和 UPDATE 操作
        if (sqlCommandType == SqlCommandType.INSERT || sqlCommandType == SqlCommandType.UPDATE) {
            Object parameter = invocation.getArgs()[1];
            autoFillFields(parameter, sqlCommandType);
        }

        // 处理 SELECT 操作
        if (sqlCommandType == SqlCommandType.SELECT) {
            checkQuerySafety(mappedStatement, invocation.getArgs()[1]);
        }

        return invocation.proceed();
    }

    /**
     * 自动填充字段
     */
    private void autoFillFields(Object parameter, SqlCommandType sqlCommandType) {
        if (parameter == null) {
            return;
        }

        try {
            Class<?> parameterClass = parameter.getClass();

            // 跳过基本类型和字符串
            if (parameterClass.isPrimitive() || parameter instanceof String) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();

            // INSERT 操作：填充 name（如果为空）、createTime、updateTime
            if (sqlCommandType == SqlCommandType.INSERT) {
                setFieldIfExists(parameter, "userName", getDefaultName());
                setFieldIfExists(parameter, "createTime", now);
                setFieldIfExists(parameter, "updateTime", now);
                log.info("自动填充插入字段: name, createTime, updateTime");
            }

            // UPDATE 操作：填充 updateTime
            if (sqlCommandType == SqlCommandType.UPDATE) {
                setFieldIfExists(parameter, "updateTime", now);
                log.info("自动填充更新字段: updateTime");
            }

        } catch (Exception e) {
            log.warn("自动填充字段失败", e);
        }
    }

    /**
     * 设置字段值（如果字段存在且当前为空）
     */
    private void setFieldIfExists(Object target, String fieldName, Object value) {
        try {
            Field field = findField(target.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                Object currentValue = field.get(target);

                // 只有当前值为 null 时才设置
                if (currentValue == null) {
                    field.set(target, value);
                    log.debug("字段 {} 自动填充为: {}", fieldName, value);
                }
            }
        } catch (Exception e) {
            log.debug("字段 {} 设置失败: {}", fieldName, e.getMessage());
        }
    }

    /**
     * 查找字段（包括父类）
     */
    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 获取默认名称
     */
    private String getDefaultName() {
        // 可以根据业务需求自定义默认值
        return "system_auto";
    }

    /**
     * 检查查询语句的安全性
     */
    private void checkQuerySafety(MappedStatement mappedStatement, Object parameter) {
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String sql = boundSql.getSql().toLowerCase().replaceAll("\\s+", " ").trim();

        log.debug("检查SQL安全性: {}", sql);

        // 检查是否包含 WHERE 或 LIMIT
        boolean hasWhere = sql.contains(" where ");
        boolean hasLimit = sql.contains(" limit ");

        if (!hasWhere && !hasLimit) {
            String errorMsg = String.format(
                "不安全的查询语句！必须包含 WHERE 或 LIMIT 子句。SQL: %s",
                mappedStatement.getId()
            );
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        log.debug("SQL安全检查通过: hasWhere={}, hasLimit={}", hasWhere, hasLimit);
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以在这里读取配置参数
    }
}