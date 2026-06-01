package com.vcall.pbx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PbxServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PbxServiceApplication.class, args);
    }
}
