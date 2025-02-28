package com.geektime.designpattern.template;

class Football extends Game {
    @Override
    void initialize() {
        System.out.println("Football 游戏初始化！准备开始比赛。");
    }

    @Override
    void startPlay() {
        System.out.println("Football 比赛开始！尽情享受比赛吧。");
    }

    @Override
    void endPlay() {
        System.out.println("Football 比赛结束！");
    }
}