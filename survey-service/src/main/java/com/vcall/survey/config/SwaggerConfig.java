package com.vcall.survey.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI surveyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Survey Service API")
                        .description("Survey, CSAT and NPS management API for VCall Contact Center")
                        .version("1.0.0")
                        .license(new License().name("Proprietary")));
    }
}
