package com.geektime.designpattern.templatepattern;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 抽象类：你来填空
public abstract class MyExcelImportService {

    // 主流程方法（你觉得这个方法应该怎么写？）
    public void importData(MultipartFile file) {
        List<List<String>> rows = readExcel(file);

        verifyData(rows);

        List<?> data = transformData(rows);

        batchInsert(data);

        printLog(data);
    }

    List<List<String>> readExcel(MultipartFile file) {
        List<List<String>> list = new ArrayList<>();
        return list;
    }

    abstract void verifyData(List<List<String>> rows);

    abstract List<?> transformData(List<List<String>> rows);

    abstract void batchInsert(List<?> data);

    abstract void printLog(List<?> data);
    

    // 下面该定义哪些方法？哪些是普通方法？哪些是抽象方法？
}

// 具体实现类：你来写商品导入
@Slf4j
class ProductImportServiceMy extends MyExcelImportService {
    @Override
    void verifyData(List<List<String>> rows) {
        
    }

    @Override
    List<?> transformData(List<List<String>> rows) {
        return Collections.emptyList();
    }

    @Override
    void batchInsert(List<?> data) {

    }

    @Override
    void printLog(List<?> data) {
        log.info("导入商品{}条", data.size());
    }

    // TODO: 实现抽象方法
}
