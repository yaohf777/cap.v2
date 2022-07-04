package com.sap.grc.iag.accesscertification.odata;

import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.sap.cloud.sdk.service.prov.api.connection.DataSourceParams;
import com.sap.cloud.sdk.service.prov.v2.rt.api.extensions.ExtensionConfig;
import com.sap.cloud.sdk.service.prov.v2.rt.core.RuntimeDelegate;
import com.sap.cloud.sdk.service.prov.v2.rt.core.extensions.ExtensionConfigFactory;
import com.sap.cloud.sdk.service.prov.v2.rt.core.extensions.ExtensionConfigFactory.ConventionExtensionConfig;
import com.sap.cloud.servicesdk.spring.autoconfig.SpringConnectionProvider;
import com.sap.cloud.servicesdk.spring.autoconfig.SpringServletListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ODataCustomServletContextListener extends SpringServletListener {

	// Only needed is to override CDSDataProvider with ODataCustomDataProvider
	//private static final String AE_DPC = "com.sap.cloud.sdk.service.prov.v2.rt.cds.CDSDataProvider";
	private static final String AE_DPC = "com.sap.grc.iag.accesscertification.odata.ODataCustomDataProvider";
	private static final String MPC = "com.sap.cloud.sdk.service.prov.v2.rt.cdx.CDXEdmProvider";

	private DataSourceParams connProvider = null;
	private String connectionProviderClass = JNDI_CONNECTION_UTIL_CLASS_NAME;
	private String userContextProviderClass = XS_USERCONTEXT_UTIL_CLASS_NAME;

	public ODataCustomServletContextListener(SpringConnectionProvider springConnectionProvider) {
		
		super(springConnectionProvider);
	}

	public void contextInitialized(ServletContextEvent contextEvent) {

		super.contextInitialized(contextEvent);

		//connProvider = RequestProcessingHelper.getConnectionProvider();
		//ServletContext context = contextEvent.getServletContext();
		//connectionProviderClass = getConnectionProviderClass(context);
		//userContextProviderClass = getUserContextConfigurationClass(context);
	}
	
	protected RuntimeDelegate getRuntimeDelegateForHanaView(ServletContext context) {

		RuntimeDelegate delegate = null;
		try {
			delegate = createRuntimeDelegateInstance();
			delegate.setMPC(MPC);
			delegate.setDPC(AE_DPC);
			delegate.setExtensionConfig(getExtensionConfig(context));
			if (connProvider == null) {
				delegate.setConnectionProvider(connectionProviderClass);
			} else {
				delegate.setConnectionProviderDirect(connProvider);
			}
			delegate.setUserContextProvider(userContextProviderClass);
			// delegate.setLocaleExtensionProvider(getLocaleExtensionClass(context));
		} catch (Exception e) {
			log.error("Error Initializing Runtime Delegate", e);
		}

		return delegate;
	}

	private ExtensionConfig getExtensionConfig(ServletContext context) throws ClassNotFoundException {

		String extensionConfigClassName = context.getInitParameter(PARAMETER_EXTENSION_CONFIG);
		if (extensionConfigClassName != null) {
			return ExtensionConfigFactory.createExtensionConfig(extensionConfigClassName);
		}
		return new ConventionExtensionConfig(new ArrayList<>(), false);
	}

//	private String getConnectionProviderClass(ServletContext context) {
//
//		String connection = context.getInitParameter("DBConnection");
//		if (connection == null)
//			connection = JNDI_CONNECTION_UTIL_CLASS_NAME;
//		return connection;
//	}
//
//	private String getUserContextConfigurationClass(ServletContext context) {
//
//		String userContext = context.getInitParameter("UserContextConfiguration");
//		if (userContext == null) {
//			try {
//				Class.forName(XS_USERCONTEXT_UTIL_CLASS_NAME);
//			} catch (ClassNotFoundException e) {
//				log.error("No UserContext Class Found, Fallback to properties file");
//				return null;
//			}
//			userContext = XS_USERCONTEXT_UTIL_CLASS_NAME;
//			return userContext;
//		}
//		return userContext;
//	}
}
