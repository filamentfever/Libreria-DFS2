package com.libreriajuanin.resenas;

        import org.springframework.boot.SpringApplication;
        import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

        @SpringBootApplication
        @EnableFeignClients
public class ResenasApplication {

            public static void main(String[] args) {
                SpringApplication.run(ResenasApplication.class, args);
            }
        }
