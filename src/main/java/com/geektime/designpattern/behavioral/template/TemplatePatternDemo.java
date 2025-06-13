package com.geektime.designpattern.behavioral.template;

// 抽象模板类，定义了游戏流程的骨架
abstract class Game {
    // 模板方法，定义了游戏进行的固定步骤，不允许子类修改
    public final void play() {
        // 初始化游戏
        initialize();
        // 开始游戏
        startPlay();
        // 结束游戏
        endPlay();
    }
    
    // 抽象方法，由子类实现具体的初始化过程
    abstract void initialize();
    
    // 抽象方法，由子类实现具体的开始游戏过程
    abstract void startPlay();
    
    // 抽象方法，由子类实现具体的结束游戏过程
    abstract void endPlay();
}

// 子类实现具体的 Cricket 游戏


// 子类实现具体的 Football 游戏


// 测试模板模式的主类
public class TemplatePatternDemo {
    public static void main(String[] args) {
        // 使用 Cricket 实现
        Game game = new Cricket();
        game.play();
        Cricket cricket = new Cricket();
//        cricket.test();

        System.out.println();

        // 使用 Football 实现
        game = new Football();
        game.play();
    }
}

