package com.bruce.qltest;

import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QLMarketingExample {

    // 用户数据模型
    public static class User {
        private int id;
        private String name;
        private int age;
        private int purchaseCount;
        private double totalSpent;
        private String city;

        public User(int id, String name, int age, int purchaseCount, double totalSpent, String city) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.purchaseCount = purchaseCount;
            this.totalSpent = totalSpent;
            this.city = city;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public int getAge() { return age; }
        public int getPurchaseCount() { return purchaseCount; }
        public double getTotalSpent() { return totalSpent; }
        public String getCity() { return city; }

        @Override
        public String toString() {
            return "User{" +
                   "id=" + id +
                   ", name='" + name + '\'' +
                   ", age=" + age +
                   ", purchaseCount=" + purchaseCount +
                   ", totalSpent=" + totalSpent +
                   ", city='" + city + '\'' +
                   '}';
        }
    }

    public static void main(String[] args) throws Exception {
        // 1. 初始化 H2 内存数据库
        Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/study?serverTimezone=UTC", "root", "123456");
        Statement stmt = conn.createStatement();

        

        // 查询所有用户
        ResultSet rs = stmt.executeQuery("SELECT id, name, age, purchase_count, total_spent, city FROM marketing_users");
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            int age = rs.getInt("age");
            int purchaseCount = rs.getInt("purchase_count");
            double totalSpent = rs.getDouble("total_spent");
            String city = rs.getString("city");
            users.add(new User(id, name, age, purchaseCount, totalSpent, city));
        }

        // 2. 使用 QL 表达式进行条件筛选
        // 这里定义了营销条件，筛选年龄在 25~35、购买次数不少于 3、消费总额不低于 1000 且所在城市为 "北京" 的用户
        String expression = "age >= 25 && age <= 35 && purchaseCount >= 3 && totalSpent >= 1000 && city == \"北京\"";
        ExpressRunner runner = new ExpressRunner();

        System.out.println("符合条件的用户：");
        for (User user : users) {
            // 将用户数据传入 QL 表达式的上下文中
            DefaultContext<String, Object> context = new DefaultContext<>();
            context.put("age", user.getAge());
            context.put("purchaseCount", user.getPurchaseCount());
            context.put("totalSpent", user.getTotalSpent());
            context.put("city", user.getCity());

            // 执行表达式判断
            Boolean match = (Boolean) runner.execute(expression, context, null, true, false);
            if (match != null && match) {
                System.out.println(user);
            }
        }

        // 清理资源
        rs.close();
        stmt.close();
        conn.close();
    }
}
