package com.bruce.designpattern;

import org.apache.poi.ss.formula.functions.T;

/**
 * @author heyyon
 * @date 2023-04-27 17:17
 */
public class TreeNode {

    private TreeNode left;

    public TreeNode(Integer val) {
        this.val = val;
    }

    private TreeNode right;
    
    private Integer val;

    public TreeNode getLeft() {
        return left;
    }

    public void setLeft(TreeNode left) {
        this.left = left;
    }

    public TreeNode getRight() {
        return right;
    }

    public void setRight(TreeNode right) {
        this.right = right;
    }

    public Integer getVal() {
        return val;
    }

    public void setVal(Integer val) {
        this.val = val;
    }
}
