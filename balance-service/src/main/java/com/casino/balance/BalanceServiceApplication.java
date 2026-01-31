package com.casino.balance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class BalanceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BalanceServiceApplication.class, args);
    }
}
