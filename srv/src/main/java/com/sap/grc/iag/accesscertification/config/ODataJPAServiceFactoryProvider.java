package com.sap.grc.iag.accesscertification.config;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManagerFactory;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.edm.provider.AnnotationAttribute;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAServiceFactory;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmExtension;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmSchemaView;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmMappingImpl;

import com.sap.grc.iag.accesscertification.infrastructure.UserContext;

import lombok.extern.slf4j.Slf4j;

//@formatter:off
/**
 *
 * By default data is processed within JPAProcessorImpl. Or we can hook logic in
 * CustomoDataJPAProcessor.java
 *
 * For oData call trouble shooting, set breakpoint in
 * CXFNonSpringServlet.invoke() & ODataSubLocator & ODataRequestHandler.handle()
 *
 */
//@formatter:on
@Slf4j
public class ODataJPAServiceFactoryProvider extends ODataJPAServiceFactory {

	
	public static final String PERSISTENCE_UNIT_NAME = "com.sap.grc.iag.accesscertification";
	
	@Override
	public ODataJPAContext initializeODataJPAContext() throws ODataJPARuntimeException {

		EntityManagerFactory entityManagerFactory = UserContext.getEntityManagerFactory();

		ODataJPAContext oDataJPAContext = getODataJPAContext();
		oDataJPAContext.setEntityManagerFactory(entityManagerFactory);
		oDataJPAContext.setPersistenceUnitName(PERSISTENCE_UNIT_NAME);
		// oDataJPAContext.setPersistenceUnitName(entityManagerFactory.getPersistenceUnitUtil().toString());
		oDataJPAContext.setJPAEdmExtension(new EdmExtension());
		oDataJPAContext.setDefaultNaming(true);
		setDetailErrors(true);

		return oDataJPAContext;
	}

	@Override
	public ODataService createODataSingleProcessorService(EdmProvider edmProvider, ODataSingleProcessor processor) {

		// Important to use same name for schema and persistence unit otherwise oData
		// call has 500 error
		// Refer to JPAEdmBaseViewImpl.java and JPAEdmSchema.java
		try {
			List<Schema> schemaList = edmProvider.getSchemas();
			for (Schema schema : schemaList) {
				schema.setNamespace(PERSISTENCE_UNIT_NAME);
			}
		} catch (ODataException e) {
			log.error("Exception with oData schema", e);
		}
		return super.createODataSingleProcessorService(edmProvider, processor);
	}

//	@Override
//	public ODataSingleProcessor createCustomODataProcessor(ODataJPAContext oDataJPAContext) {
//		return new ODataJPADefaultProcessor(oDataJPAContext);
//	}

	public class EdmExtension implements JPAEdmExtension {

		private static final String SAP_NAMESPACE = "http://www.sap.com/Protocols/SAPData";
		private static final String SAP_PREFIX = "sap";

		@Override
		public void extendWithOperation(JPAEdmSchemaView view) {

			// view.registerOperations(EdmOperation.class, null);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void extendJPAEdmSchema(JPAEdmSchemaView view) {

			try {
				
				Schema edmSchema = view.getEdmSchema();
				List<EntityType> entityTypeList = edmSchema.getEntityTypes();
				for (EntityType entityType : entityTypeList) {
					SapSemantics sapEntityAnnotation = ((JPAEdmMappingImpl) entityType.getMapping()).getJPAType()
							.getAnnotation(SapSemantics.class);
					if (sapEntityAnnotation != null) {
						List<AnnotationAttribute> annotationAttributeList = entityType.getAnnotationAttributes();
						if (annotationAttributeList == null) {
							annotationAttributeList = new ArrayList<AnnotationAttribute>();
						}

						InvocationHandler handler = Proxy.getInvocationHandler(sapEntityAnnotation);
						Field field = handler.getClass().getDeclaredField("memberValues");
						field.setAccessible(true);
						Map<String, Object> memberValues = (Map<String, Object>) field.get(handler);
						for (Entry<String, Object> memberValue : memberValues.entrySet()) {
							AnnotationAttribute annotationAttribute = new AnnotationAttribute()
									.setNamespace(SAP_NAMESPACE).setPrefix(SAP_PREFIX).setName(memberValue.getKey())
									.setText(String.valueOf(memberValue.getValue()));
							annotationAttributeList.add(annotationAttribute);
						}
						entityType.setAnnotationAttributes(annotationAttributeList);

						for (Property property : entityType.getProperties()) {
							List<AnnotationAttribute> propertyAnnotationList = new ArrayList<AnnotationAttribute>();

							if (property.getName().equals("USER_ID")
							  || property.getName().equals("CONNECTOR")
							  || property.getName().equals("PRIVILEGE")) {
								propertyAnnotationList.add(new AnnotationAttribute().setName("sap:aggregation-role").setText("dimension"));

							}
							property.setAnnotationAttributes(propertyAnnotationList);
						}
					}
				}
			} catch (Exception e) {
				log.info("Exception with enhance EDM annotation attribute", e);
			}
		}

		@Override
		public InputStream getJPAEdmMappingModelStream() {

			// Refine oData Metadata
			// https://olingo.apache.org/doc/odata2/tutorials/jparedefinemetadata.html
			// The model is prepared in JPAEdmMappingModelService.java
			// From ClassLoader all paths are absolute. No slash needed for context path.
			// /srv/src/main/webapp/resources/ODataEdmMapping.xml
			// PersistenceUnit name does not need to be same as ODataJPAContext
			return this.getClass().getClassLoader().getResourceAsStream("ODataEdmMapping.xml");
		}
	}

}
