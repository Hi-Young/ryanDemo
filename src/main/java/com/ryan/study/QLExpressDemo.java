package com.ryan.study;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;

public class QLExpressDemo {
    public static void main(String[] args) throws Exception {
        ExpressRunner expressRunner = new ExpressRunner();
        DefaultContext<String, Object> context = new DefaultContext<>();
        context.put("a", 10);
        context.put("b", 20);
        String express = "a+b*10";
        Object execute = expressRunner.execute(express, context, null, false, false);
        System.out.println("执行结果：" + execute);
    }
}
