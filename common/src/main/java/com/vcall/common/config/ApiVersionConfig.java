// ApiVersionConfig is disabled because controllers already use the /api/v1 prefix explicitly.
// Enabling this would cause double-prefixing (e.g., /api/v1/api/v1/auth/login).
//package com.vcall.common.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class ApiVersionConfig implements WebMvcConfigurer {
//
//    @Override
//    public void configurePathMatch(PathMatchConfigurer configurer) {
//        configurer.addPathPrefix("/api/v1", handler -> true);
//    }
//}
