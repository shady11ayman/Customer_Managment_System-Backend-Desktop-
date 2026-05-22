package com.CustomerManagmentApp.Customer_Managment_System.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApiTokenFilter apiTokenFilter;

    @Bean
    public FilterRegistrationBean<ApiTokenFilter> tokenFilterRegistration() {
        FilterRegistrationBean<ApiTokenFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(apiTokenFilter);
        registrationBean.addUrlPatterns("/customers/*", "/customers");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
