package com.geektime.concurrent.basic;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * 即使是单线程的 也能处理完请求不关闭，这个老师说错了
 */
public class BsServer01 {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("服务器 启动.....  ");
        System.out.println("开启端口 : 9999.....  ");
        // 1. 创建服务端ServerSocket
        ServerSocket serverSocket = new ServerSocket(9999);
        // 2. 循环接收,建立连接
        while (true) {
            Socket accept = serverSocket.accept();
            /*
             *3. socket对象进行读写操作
             */
            //转换流，读取浏览器请求第一行
            BufferedReader readWb = new BufferedReader(new InputStreamReader(accept.getInputStream()));
            String requst = readWb.readLine();//包含URL地址
            //取出请求资源的路径
            String[] strArr = requst.split(" ");
            System.out.println(Arrays.toString(strArr));
            String path = strArr[1].substring(1);//截取请求path
            System.out.println(path);
            FileInputStream fis = null;
            boolean flag = false;
            try {
                //----前提请求的Path与文件相对路径的Path是相同的
                fis = new FileInputStream(path);
            } catch (FileNotFoundException e) {
//                throw new RuntimeException(e);
                flag = true;
            }
            System.out.println(fis);
            if (flag) {
                continue;
            }
            byte[] bytes = new byte[1024];
            int len = 0;

            //向浏览器 回写数据
            OutputStream out = accept.getOutputStream();
            out.write("HTTP/1.1 200 OK\r\n".getBytes());
            out.write("Content-Type:text/html\r\n".getBytes());
            out.write("\r\n".getBytes());
            while ((len = fis.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }
            Thread.sleep(5000);

            fis.close();
            out.close();
            readWb.close();
            accept.close();
        }

    }
}
