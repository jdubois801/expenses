package com;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SpringSecurityWebAppConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            // TODO - add the other common Spring Boot resources as permitAll()
            .antMatchers("/", "/expense", "/expenses").permitAll();
        	// TODO - secure /expense/* and /expenses with basic auth to credentials 'demo':'demo'
        
        http
            .csrf().disable();
    }
}
