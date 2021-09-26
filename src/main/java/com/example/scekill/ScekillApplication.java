package com.example.scekill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;


@SpringBootApplication
@MapperScan("com.example.scekill.mapper")
public class ScekillApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScekillApplication.class, args);
    }
//    public static void main(String[] args) {
//        Object a =new Object();
//        Object b =new Object();
//        System.out.println(a.hashCode());
//        System.out.println(b.hashCode());
//        HashMap
//    }
}

