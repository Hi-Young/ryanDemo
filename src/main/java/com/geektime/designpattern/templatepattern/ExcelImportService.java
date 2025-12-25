//package com.geektime.designpattern.templatepattern;
//
//import com.geektime.basic.generic.training.day1.entities.Product;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Slf4j
//public abstract class ExcelImportService<T> {
//    
//    // 模板方法：定义算法骨架
//    public void importData(MultipartFile file) {
//        // 步骤1：读取（公共）
//        List<List<String>> rows = readExcel(file);
//        
//        // 步骤2：校验（变化点）
//        validateData(rows);
//        
//        // 步骤3：转换（变化点）
//        List<T> data = transformData(rows);
//        
//        // 步骤4：保存（变化点）
//        saveData(data);
//        
//        // 步骤5：日志（公共）
//        logResult(data);
//    }
//    
//    // 公共方法：读取Excel
//    private List<List<String>> readExcel(MultipartFile file) {
//        // 通用的Excel读取逻辑
//        return new ArrayList<>();
//    }
//    
//    // 抽象方法：校验（子类实现）
//    protected abstract void validateData(List<List<String>> rows);
//    
//    // 抽象方法：转换（子类实现）
//    protected abstract List<T> transformData(List<List<String>> rows);
//    
//    // 抽象方法：保存（子类实现）
//    protected abstract void saveData(List<T> data);
//    
//    // 钩子方法：获取业务名称（子类实现）
//    protected abstract String getBusinessName();
//    
//    // 公共方法：记录日志
//    private void logResult(List<T> data) {
//        log.info("导入{}{}条", getBusinessName(), data.size());
//    }
//}
//
//// 商品导入
//@Service
//public class ProductImportService extends ExcelImportService<Product> {
//    
//    @Autowired
//    private ProductMapper productMapper;
//    
//    @Override
//    protected void validateData(List<List<String>> rows) {
//        for (List<String> row : rows) {
//            if (row.get(0) == null || row.get(0).isEmpty()) {
//                throw new BusinessException("商品编码不能为空");
//            }
//        }
//    }
//    
//    @Override
//    protected List<Product> transformData(List<List<String>> rows) {
//        List<Product> products = new ArrayList<>();
//        for (List<String> row : rows) {
//            Product product = new Product();
//            product.setCode(row.get(0));
//            product.setName(row.get(1));
//            product.setPrice(new BigDecimal(row.get(2)));
//            products.add(product);
//        }
//        return products;
//    }
//    
//    @Override
//    protected void saveData(List<Product> data) {
//        productMapper.batchInsert(data);
//    }
//    
//    @Override
//    protected String getBusinessName() {
//        return "商品";
//    }
//}