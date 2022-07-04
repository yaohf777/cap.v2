package com.sap.grc.iag.accesscertification.infrastructure;

import java.util.LinkedHashMap;
import java.util.Locale;

import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public class UserContext {

	private static ThreadLocal<LinkedHashMap<String, Object>> threadSession = new ThreadLocal<LinkedHashMap<String, Object>>();

	private static ApplicationContext applicationContext;
	
	static void setLocale(Locale locale) {
		setProperty("locale", locale);
	}

	public static Locale getLocale() {
		Object locale = getProperty("locale");
		if (locale != null) {
			return (Locale) getProperty("locale");
		}
		return Locale.ENGLISH;
	}

	public static void setLoggedInUserId(String userName) {
		setProperty("user", userName);
	}

	public static String getLoggedInUserId() {

		Object user = getProperty("user");
		if (user != null) {
			return (String) user;
		}
		return "";
	}
	public static void setJWToken(String token) {

		setProperty("token", token);
	}

	public static String getJWToken() {

		Object token = getProperty("token");
		if (token != null) {
			return (String) token;
		}
		return null;
	}

	public static void setWorkflowTechnicalJWToken(String token) {

		setProperty("WorkflowTTechnicalJWT", token);
	}

	public static String getWorkflowTechnicalJWToken() {

		Object token = getProperty("WorkflowTTechnicalJWT");
		if (token != null) {
			return (String) token;
		}
		return null;
	}

	static void setHttpRequest(HttpServletRequest request) {
		setProperty("httpRequest", request);
	}

	public static HttpServletRequest getHttpRequest() {
		return (HttpServletRequest) getProperty("httpRequest");
	}

	public static void setHttpSession(HttpSession httpSession) {
		setProperty("httpSession", httpSession);
	}

	public static HttpSession getHttpSession() {
		
		Object maybeSession = getProperty("httpSession");
		if (maybeSession != null) {
			return (HttpSession) maybeSession;
		}
		return null;
	}

	public static void setSessionAttribute(String name, Object value) {

		HttpSession session = getHttpSession();
		if (session != null) {
			session.setAttribute(name, value);
		}
	}

	public static Object getSessionAttribute(String name) {
		HttpSession session = getHttpSession();
		return session != null ? session.getAttribute(name) : null;
	}

	public static void removeSessionAttribute(String name) {
		HttpSession session = getHttpSession();
		if (session != null)
			session.removeAttribute(name);
	}

	public static void setAppContext(ApplicationContext context) {
		setProperty("appContext", context);
		applicationContext = context;
	}

	public static ApplicationContext getAppContext() {

		ApplicationContext context = (WebApplicationContext) getProperty("appContext");
		if (context == null) {
			context = applicationContext;
		}
		return context;
	}

	// Retrieve instance managed by Spring in non-spring context
	public static DataSource getDatasource() {
		return getBean(DataSource.class);
	}

	// Retrieve instance managed by Spring in non-spring context
	public static EntityManagerFactory getEntityManagerFactory() {
		return getBean(EntityManagerFactory.class);
	}

	public static Object getBeanByName(String beanName) {
		return getAppContext().getBean(beanName);
	}

	public static <T> T getBean(Class<T> requiredType) {
		return getAppContext().getBean(requiredType);
	}

	private static void setProperty(String name, Object value) {
		if (threadSession.get() == null) {
			LinkedHashMap<String, Object> paramterMap = new LinkedHashMap<String, Object>();
			threadSession.set(paramterMap);
		}
		threadSession.get().put(name, value);
	}

	private static Object getProperty(String key) {
		if (threadSession.get() == null) {
			return null;
		}
		return threadSession.get().get(key);
	}
	
	// to be called as part of TenantFilter to release the memory
	public static void clear() {
		threadSession.remove();
	}
}