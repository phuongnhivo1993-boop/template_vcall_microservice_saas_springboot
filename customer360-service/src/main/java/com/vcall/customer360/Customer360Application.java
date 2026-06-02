package com.vcall.customer360;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.vcall.customer360", "com.vcall.common"})
@EnableDiscoveryClient
public class Customer360Application {
    public static void main(String[] args) {
        SpringApplication.run(Customer360Application.class, args);
    }
}
