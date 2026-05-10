package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MapApplication {
    public static void main(String[] args) {
        SpringApplication.run(MapApplication.class, args);
        System.out.println("http://localhost:8080");
    }
}
