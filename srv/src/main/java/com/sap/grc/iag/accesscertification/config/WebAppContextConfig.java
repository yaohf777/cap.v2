package com.sap.grc.iag.accesscertification.config;

import javax.servlet.DispatcherType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.sap.hcp.cf.logging.servlet.filter.RequestLoggingFilter;

@Configuration
@EnableWebMvc
@SuppressWarnings({ "rawtypes", "unchecked" })
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackages = { "com.sap.grc.iag" })
public class WebAppContextConfig implements WebMvcConfigurer {

	@Value("${cacheControl.max-age:3600}")
	private int cacheControlMaxage;

	@Bean
	public PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
		// make environment variables available for Spring's @Value annotation
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public CommonsRequestLoggingFilter logFilter() {

		// register logging Servlet filter which logs HTTP request processing details
		CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setMaxPayloadLength(10000);
		filter.setIncludeHeaders(false);
		filter.setAfterMessagePrefix("REQUEST DATA: ");
		return filter;
	}

	/**
	 * Enabling the Feature "Instrumenting Servlets" of cf-java-logging-support
	 * https://github.com/SAP/cf-java-logging-support/wiki/Instrumenting-Servlets#filterregistrationbean-in-spring-boot
	 */
	@Bean
	public FilterRegistrationBean loggingFilter() {

		FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
		filterRegistrationBean.setFilter(new RequestLoggingFilter());
		filterRegistrationBean.setName("request-logging");
		filterRegistrationBean.addUrlPatterns("/*");
		filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST);
		return filterRegistrationBean;
	}

	/**
	 * Enable default view ("index.html") mapped under "/".
	 */
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	/**
	 * Set up the cached resource handling for UI5 runtime served from the web jar
	 * in {@code /WEB-INF/lib} directory and local JavaScript files in
	 * {@code /resources} directory.
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/resources/", "/resources/**")
				.setCachePeriod(cacheControlMaxage);
	}
}