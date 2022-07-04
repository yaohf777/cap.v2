package com.sap.grc.iag.accesscertification.config;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.olingo.odata2.core.servlet.ODataServlet;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sap.cloud.sdk.service.prov.v2.web.ServiceInitializer;
import com.sap.cloud.servicesdk.spring.autoconfig.ODataAutoConfiguration;
import com.sap.cloud.servicesdk.spring.autoconfig.SpringConnectionProvider;
import com.sap.cloud.servicesdk.spring.autoconfig.SpringServletListener;
import com.sap.grc.iag.accesscertification.odata.ODataCustomServletContextListener;
//@formatter:off
/**
* To provide oData V2 service based on Cloud SDK:
* 
* 1. Add necessary Maven dependency to pom.xml
* 
* 2  Provide oData metadata through resources/edmx/DataService.xml which is parsed in 
*    CDXRuntimeDelegate.populateProviderFromFile() through ServiceInitializer<T> for ODataV2
*    Only the namespace in srv/dataService.cds is relevant , db/data-model.cds is irrelevant
*    
* 3. In ODataConfig.java, register the ODataServlet to listen to oData request through the mapped URL.
* 
* 4. The actual Data Provider is setup in com.sap.cloud.sdk.service.prov.v2.rt.core.RuntimeDelegate.getDataProvider 
*    according to string DPC which is specified through below classes:
*     a. com.sap.cloud.sdk.service.prov.v2.rt.core.web.ServletListener<T>.initializeRuntimeDelegateForHanaView for non Spring boot
*        ServletListener is registered in ODataConfig.java.
*     b. com.sap.cloud.servicesdk.spring.autoconfig.SpringServletListener for Spring boot auto configuration
*     To change the standard behavior:
*     a. Disable ServletListener in ODataConfig.java
*     b. Disable SpringServletListener through "exclude = { ODataAutoConfiguration.class }" in MainApplication
*     c. Override CDSDataProvider with ODataCustomDataProvider in ODataServletContextListener that extends SpringServletListener 
*        It is necessary to switch to HANA provider through RequestProcessingHelper.setDatasourceProvider("HANA");
*        c1. HANA provider -> better SQL but worse field mapping
*        SELECT ROW_ID AS ROW_ID,USER_ID AS USER_ID,CONNECTOR AS CONNECTOR,PRIVILEGE AS PRIVILEGE FROM DATASELECTION_USER_DETAILS_ROLES_SYSTEMS_VIEW
*        c2. CDS provider  -> wrong SQL but better field mapping
*        SELECT FROM DATASELECTION_USER_DETAILS_ROLES_SYSTEMS_VIEW AS ROOT_ENTITY_ALIAS{ROW_ID AS ROW_ID,USER_ID AS USER_ID,CONNECTOR AS CONNECTOR,PRIVILEGE AS PRIVILEGE}
*        
* 5. In ODataConfig.java specify the package containing the OData custom implementation code through Servlet context parameter. 
*    The package name is read in ServiceInitializer<T> for ODataV2 ( ODataApplicationInitializer for ODataV4) which need to be
*    added as ServletContextListener in WebAppContextConfig.java.
*    Issues found if package is set to "com.sap.grc.iag.accesscertification.odata"
*    a. Load annotated classes in ClassHelper.loadClasses
*    b. Register annotated methods in ServiceInitializer<T>
*    The issue is gone if the scan package is set to "com.sap.grc.iag.accesscertification"
*    Maybe we cannot add OData custom implementation for V2
*    
* 6. Only use names in UPPER CASE in .cds files to prevent issue in oData service CDSQuery which is case sensitive.
* 
* 7. To supply data for UI5 analytic table, oData $select must be provided to avoid error with aggregation in StableId.java
* 
* Open issue
* 1. If any key field is not requested through $select, there is an exception within call stack of AtomEntryEntityProducer.createSelfLink()
*    This is because CDSDataProvider only fetch the fields requested through $select, but all key fields are needed to generate the link.
*    The issue does not exist for ODataJPAProcessor since all fields are fetched through JPA query regardless of $select.
*    For example, if USER_ID, CONNECTOR and PRIVILEGE are key fields:
*    a. Working     -> /v2/odata/DataselectionUserDetailsRolesSystemsView?$select=USER_ID,CONNECTOR,PRIVILEGE
*    b. Not working -> /v2/odata/DataselectionUserDetailsRolesSystemsView?$select=USER_ID
*    One workaround is to set EntityProviderWriteProperties.contentOnly = true which is prepared in GenericODataProcessor.getEntitySetResponse().
*    But whether there is impact on UI5 is not tested.
* 
* oData service standard call stack without enhancement
* com.sap.cloud.sdk.service.prov.v2.rt.cds.ODataToCDSProcessor.execute(ODataToCDSProcessor.java:332)
* com.sap.cloud.sdk.service.prov.v2.rt.cds.ODataToCDSProcessor.handleAndExecute(ODataToCDSProcessor.java:118)
* com.sap.cloud.sdk.service.prov.v2.rt.cds.CDSDataProvider.readEntitySet(CDSDataProvider.java:198)
* com.sap.cloud.sdk.service.prov.v2.rt.core.extensions.ExtensionDataProvider.readEntitySetNonDraftFlow(ExtensionDataProvider.java:716)
* com.sap.cloud.sdk.service.prov.v2.rt.core.extensions.ExtensionDataProvider.readEntitySet(ExtensionDataProvider.java:502)
* com.sap.gateway.core.api.provider.data.GenericODataProcessor.readEntitySet(GenericODataProcessor.java:891)
* 
* oData query result conversion is performed in 
* com.sap.cloud.sdk.service.prov.v2.rt.cds.ResultSetProcessor.toEntityCollection()
* 
* Context listeners added by RequestContextListenerChain
* com.sap.cloud.sdk.cloudplatform.connectivity.DestinationsRequestContextListener
* com.sap.cloud.sdk.cloudplatform.security.user.UserRequestContextListener
* om.sap.cloud.sdk.cloudplatform.security.AuthTokenRequestContextListener
* com.sap.cloud.sdk.cloudplatform.tenant.TenantRequestContextListener
*/
//@formatter:on

@Configuration
//Make sure this configuration is executed before the DispatcherServletAutoConfiguration is. 
//It needs to expose Servlet beans that the DispatcherServletAutoConfiguration will pick up.
@AutoConfigureBefore(DispatcherServletAutoConfiguration.class)
@ConditionalOnWebApplication(type = Type.SERVLET)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ODataConfig extends ODataAutoConfiguration{
    
// oData JPA provider	
//	@Bean
//	public ServletRegistrationBean registerODataServlet() {
//
//		ServletRegistrationBean odataServRegstration = new ServletRegistrationBean(new CXFNonSpringJaxrsServlet(),
//				"/odata/*");
//		Map<String, String> initParameters = new HashMap<>();
//		initParameters.put("javax.ws.rs.Application", "org.apache.olingo.odata2.core.rest.app.ODataApplication");
//		initParameters.put("org.apache.olingo.odata2.service.factory",
//				"com.sap.grc.iag.accesscertification.config.ODataJPAServiceFactoryProvider");
//		odataServRegstration.setInitParameters(initParameters);
//		return odataServRegstration;
//	}
	
	@Bean
	// oData cloud SDK provider based on HANA query
	public ServletRegistrationBean oDataServletRegistration() {
		
		// ODataServiceFactory loaded in ODataServlet.java
		ServletRegistrationBean odataServRegstration = new ServletRegistrationBean(new ODataServlet(), "/odata/*");
		Map<String, String> initParameters = new HashMap<>();
		// Use parent class of CloudSDKODataServiceFactory to avoid
		initParameters.put("org.apache.olingo.odata2.service.factory",
				"com.sap.grc.iag.accesscertification.odata.ODataCustomServiceFactory");
		odataServRegstration.setInitParameters(initParameters);
		return odataServRegstration;
	}
	
    /**
     * Exposes a Servlet listener required to set the oData provider
     */
    @Bean
    public SpringServletListener springServletListener(SpringConnectionProvider connectionProvider) {
    	
    	// Override getRuntimeDelegateForHanaView() through this ODataServletContextListener
    	ODataCustomServletContextListener listener = new ODataCustomServletContextListener(connectionProvider);
        return listener;
    }
 
// Not needed since the setup is performed by SpringServletListener above
//	@Bean
//	public ServletListenerRegistrationBean oDataServletListener() {
//		
//		// Used to initialize oData environment, e.g. Data Provider
//		return new ServletListenerRegistrationBean( new ServletListener());
//	}
	
	@Bean
	public ServletListenerRegistrationBean oDataServletInitializer() {
		
		return new ServletListenerRegistrationBean( new ServiceInitializer<T>());
	}
	
	@Bean
	// Specify package to scan for oData service implementation class
	// The classes are scanned in ServiceInitializer.java which need to be registered as ServletContextListener
	// !!! Needed even if no implementation class is provided to prevent error with setting oData provider
	public ServletContextInitializer contextInitializer() {
		return new ServletContextInitializer() {
			@Override
			public void onStartup(ServletContext servletContext) throws ServletException {
				servletContext.setInitParameter("package", "com.sap.grc.iag.accesscertification");
			}
		};
	}
}
