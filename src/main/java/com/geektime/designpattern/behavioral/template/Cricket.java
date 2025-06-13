package com.geektime.designpattern.behavioral.template;

public class Cricket extends Game {
    @Override
    void initialize() {
        System.out.println("Cricket 游戏初始化！准备开始比赛。");
    }

    @Override
    void startPlay() {
        System.out.println("Cricket 比赛开始！尽情享受比赛吧。");
    }

    @Override
    void endPlay() {
        System.out.println("Cricket 比赛结束！");
    }

//    public void play() {
//
//    }

}
