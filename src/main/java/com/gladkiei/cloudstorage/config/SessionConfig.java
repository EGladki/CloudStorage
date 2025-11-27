package com.gladkiei.cloudstorage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;

@Configuration
public class SessionConfig {

    @Bean
    public CookieHttpSessionIdResolver cookieHttpSessionIdResolver() {
        return new CookieHttpSessionIdResolver();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

}
