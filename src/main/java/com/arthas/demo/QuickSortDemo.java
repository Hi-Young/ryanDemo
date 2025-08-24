package com.arthas.demo;

/**
 * 快速排序算法演示类
 * 用于在Arthas中演示和测试排序算法的性能和行为
 */
public class QuickSortDemo {

    /**
     * 快速排序主方法
     * @param arr 待排序数组
     */
    public void quickSort(int[] arr) {
        if (arr == null || arr.length <= 1) {
            return;
        }
        quickSort(arr, 0, arr.length - 1);
    }

    /**
     * 递归快速排序
     * @param arr 数组
     * @param low 起始索引
     * @param high 结束索引
     */
    private void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            // 获取分区点
            int pivotIndex = partition(arr, low, high);
            
            // 递归排序左右两部分
            quickSort(arr, low, pivotIndex - 1);
            quickSort(arr, pivotIndex + 1, high);
        }
    }

    /**
     * 分区操作
     * @param arr 数组
     * @param low 起始索引
     * @param high 结束索引
     * @return 分区点索引
     */
    private int partition(int[] arr, int low, int high) {
        // 选择最后一个元素作为基准
        int pivot = arr[high];
        int i = low - 1; // 小于基准的元素的索引

        for (int j = low; j < high; j++) {
            // 如果当前元素小于或等于基准
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j);
            }
        }
        
        // 将基准元素放到正确位置
        swap(arr, i + 1, high);
        return i + 1;
    }

    /**
     * 交换数组中两个元素的位置
     * @param arr 数组
     * @param i 索引i
     * @param j 索引j
     */
    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    /**
     * 打印数组内容
     * @param arr 数组
     */
    public void printArray(int[] arr) {
        for (int value : arr) {
            System.out.print(value + " ");
        }
        System.out.println();
    }

    /**
     * 演示快速排序的使用
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        QuickSortDemo demo = new QuickSortDemo();
        
        // 测试数组
        int[] arr = {64, 34, 25, 12, 22, 11, 90, 88, 76, 50, 42};
        
        System.out.println("排序前:");
        demo.printArray(arr);
        
        demo.quickSort(arr);
        
        System.out.println("排序后:");
        demo.printArray(arr);
    }
}