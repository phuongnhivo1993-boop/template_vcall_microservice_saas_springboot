package com.vcall.xr.gis.config;

import com.vcall.common.config.CommonSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CommonSecurityConfig.class)
public class SecurityConfig {
}
