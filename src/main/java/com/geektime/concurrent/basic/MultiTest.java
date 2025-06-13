//package com.ryan.multithread;
//
//import org.apache.poi.hssf.record.Record;
//import org.w3c.dom.ranges.Range;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//public class MultiTest {
//    
//    
//    public static void main(String[] args) {
//        // 1. 先获取外部表中数据的最小和最大ID
//        long minId = getMinId(); // 例如返回 1
//        long maxId = getMaxId(); // 例如返回 10000
//        long batchSize = 1000;   // 每批处理1000条
//
//// 2. 根据ID范围划分出不重叠的区间
//        List<Range> ranges = new ArrayList<>();
//        for (long start = minId; start <= maxId; start += batchSize) {
//            long end = Math.min(start + batchSize - 1, maxId);
//            ranges.add(new Range(start, end));
//        }
//
//// 3. 使用多线程处理各个区间
//        ExecutorService executor = Executors.newFixedThreadPool(4); // 例如使用4个线程
//        for (Range range : ranges) {
//            executor.submit(() -> {
//                // 4. 查询指定ID区间的数据
//                List<Record> records = queryExternalData("WHERE id >= " + range.start + " AND id <= " + range.end);
//                // 5. 对每批数据进行转换后插入目标表
//                for (Record record : records) {
//                    insertIntoTargetTable(record);
//                }
//            });
//        }
//        executor.shutdown();
//        executor.awaitTermination(1, TimeUnit.HOURS);
//
//    }
//}
