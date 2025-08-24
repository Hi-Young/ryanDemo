package com.arthas.demo;

import org.springframework.stereotype.Component;

@Component
public class ArthasClassLoaderDemo {

    public void printClassLoader() {
        System.out.println("ArthasClassLoaderDemo's ClassLoader: " + ArthasClassLoaderDemo.class.getClassLoader());
        System.out.println("Context ClassLoader: " + Thread.currentThread().getContextClassLoader());
    }

    // This method is for testing 'retransform'. 
    // You can modify its behavior and then reload the class.
    public String getVersion() {
        return "Version 1.0";
    }
}
