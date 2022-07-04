package com.sap.grc.iag.accesscertification.infrastructure;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserContextFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		log.error("Start doFilter in UserContextFilter");
		try {
			prepareContextInfo(request);
			chain.doFilter(request, response);
		} catch (Exception e) {
			// Exception should not occur in SCP
			log.error("Exception with UserContextFilter: {} ", e);
		} finally {
			// Clear context to release the memory
			UserContext.clear();
		}
	}

	private void prepareContextInfo(ServletRequest request) throws ParseException {

		// Set application context
		UserContext.setAppContext(
				WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext()));

		// Set HTTP request and session
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		UserContext.setHttpRequest(httpRequest);
		HttpSession httpSession = httpRequest.getSession();
		UserContext.setHttpSession(httpSession);
	}


	public void init(FilterConfig filterConfig) throws ServletException {
		// Dummy method to avoid below exception
		// java.lang.AbstractMethodError: does not define or inherit an implementation
		// of the
		// resolved method abstract init(Ljavax/servlet/FilterConfig;) of interface
		// javax.servlet.Filter.
	}

	public void destroy() {
		// Dummy method like init()
	}
}