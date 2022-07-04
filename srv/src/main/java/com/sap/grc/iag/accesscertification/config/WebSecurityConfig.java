package com.sap.grc.iag.accesscertification.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

import com.sap.cloud.security.xsuaa.XsuaaServiceConfigurationDefault;
import com.sap.cloud.security.xsuaa.XsuaaServicePropertySourceFactory;
import com.sap.grc.iag.accesscertification.infrastructure.UserContextFilter;

@Configuration
@EnableWebSecurity
@PropertySource(factory = XsuaaServicePropertySourceFactory.class, value = { "" })
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	// configure Spring Security, demand authentication and specific scopes
	@Override
	public void configure(HttpSecurity httpSecurity) throws Exception {

		httpSecurity.addFilterAfter(new UserContextFilter(), SecurityContextHolderAwareRequestFilter.class)
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER);

		// Use 404 instead of 401
		httpSecurity.exceptionHandling().authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.NOT_FOUND)).and()
				.exceptionHandling().accessDeniedHandler(new CustomeAccessDeniedHandler());

		// Make public resource accessible by "anybody"
		httpSecurity.authorizeRequests().antMatchers("/*").permitAll();

//		httpSecurity.authorizeRequests().antMatchers(GET, "/index.html", "/").permitAll()
//				.antMatchers(GET, "/health", "/").permitAll() // health check on CF
//				.antMatchers(GET, "/check").permitAll().antMatchers("/odata/*")
//				.access("@PermissionEvaluator.isValidLogonUser()").antMatchers("/api/v1/*")
//				.access("@PermissionEvaluator.isValidLogonUser()").antMatchers("/*").permitAll();
		
		httpSecurity.csrf().disable();
	}

	@Bean
	public XsuaaServiceConfigurationDefault xsuaaConfig() {
		return new XsuaaServiceConfigurationDefault();
	}
}

class CustomeAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {

		response.setStatus(404);
	}

}