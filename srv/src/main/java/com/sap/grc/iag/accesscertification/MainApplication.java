package com.sap.grc.iag.accesscertification;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.sap.cloud.servicesdk.spring.autoconfig.ODataAutoConfiguration;

@SpringBootApplication
@EnableConfigurationProperties
@EnableAutoConfiguration(exclude = { ODataAutoConfiguration.class })
public class MainApplication extends SpringBootServletInitializer {

	// @formatter:off
    /*
     * Active Spring profile set through SPRING_PROFILES_ACTIVE: ${space} in mta.yaml
     * Filters added and configurations performed by Spring Boot automatically
     * 
     * Configuration: 
     * dataSource --> DataSourceConfiguration & application.properties 
     * 
     * Filters: 
     * "dispatcherServlet"         --> ServletRegistrationBean
     * "springSecurityFilterChain" --> WebSecurityConfiguration & SecurityFilterAutoConfiguration 
     * "webRequestLoggingFilter"   --> FilterRegistrationBean
     * "errorPageFilter"           --> FilterRegistrationBean
     * "characterEncodingFilter"   --> FilterRegistrationBean
     * 
     * need to disable JerseyAutoConfiguration which contains app initializer
     */
    // @formatter:on
}