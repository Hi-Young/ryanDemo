package com.bruce.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test {
    public static void main(String[] args) {
//        String s = "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z";
//        String s = "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U";
        String s = "A,B";
        String[] split = s.split(",");
        List<String> list = Arrays.asList(split);
        List<List<String>> lists = generatePermutations(list);
        System.out.println(lists.size());
//        System.out.println(lists);

    }

    public static List<List<String>> generatePermutations(List<String> list) {
        List<List<String>> result = new ArrayList<>();
        backtrack(list, new ArrayList<>(), result);
        return result;
    }

    private static void backtrack(List<String> list, List<String> tempList, List<List<String>> result) {
        if (!tempList.isEmpty()) {
            result.add(new ArrayList<>(tempList));
        }
        for (int i = 0; i < list.size(); i++) {
            tempList.add(list.get(i));
            ArrayList<String> remainingList = new ArrayList<>(list.subList(i + 1, list.size()));
            backtrack(remainingList, tempList, result);
            tempList.remove(tempList.size() - 1);
        }
    }

}
