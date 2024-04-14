package com.bruce.designpattern;

/**
 * @author heyyon
 * @date 2023-04-27 17:18
 */
public class NodeTest {

    
    public static void main(String[] args) {
        TreeNode root = new TreeNode(2);
        TreeNode firstLeft = new TreeNode(5);
        TreeNode firstRight = new TreeNode(1);
        TreeNode secondLeftLeft = new TreeNode(4);
        root.getLeft().setLeft(firstLeft);
        root.getRight().setRight(firstRight);
        root.getLeft().getLeft().setLeft(secondLeftLeft);
        queryTree(root);    }
    
    public static void queryTree(TreeNode root) {
        if (null != root) {
            System.out.println(root.getVal());
            queryTree(root.getLeft());
            queryTree(root.getRight());
        }
        
    }
    
}
