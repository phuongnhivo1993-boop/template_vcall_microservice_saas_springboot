package com.vcall.omnichannel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class OmnichannelServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OmnichannelServiceApplication.class, args);
    }
}
