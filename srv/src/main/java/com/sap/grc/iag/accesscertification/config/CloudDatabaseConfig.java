package com.sap.grc.iag.accesscertification.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.instrument.classloading.SimpleLoadTimeWeaver;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaDialect;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.sap.cloud.mt.runtime.IdentityZoneDeterminer;
import com.sap.cloud.mt.runtime.TenantAwareDataSource;
import com.sap.cloud.mt.subscription.InstanceLifecycleManager;
import com.sap.cloud.mt.subscription.exceptions.UnknownTenant;
import com.sap.cloud.spring.boot.mt.lib.Const;
import com.sap.xsa.core.instancemanager.client.InstanceCreationOptions;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableJpaRepositories(basePackages = { "com.sap.grc.iag.accesscertification" })
@EnableTransactionManagement
@Slf4j
public class CloudDatabaseConfig extends AbstractCloudConfig {

	@Value("${eclipselink.logging.level:WARNING}")
	private String eclipselinkLoggingLevel;

	@Value("${eclipselink.logging.level.sql:WARNING}")
	private String eclipselinkLoggingLevelSql;

	@Value("${eclipselink.logging.parameters:true}")
	private String eclipselinkLoggingParameters;

	@Autowired
	private DataSourceProperties dataSourceProperties;

	@Bean
	public EntityManager entityManager(EntityManagerFactory entityManagerFactory) {
		return entityManagerFactory.createEntityManager();
	}

	@Bean("transactionManager")
	public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager transactionManager = new JpaTransactionManager(entityManagerFactory);
		transactionManager.setJpaDialect(new EclipseLinkJpaDialect());
		return transactionManager;
	}

	@Bean
	public JpaVendorAdapter jpaVendorAdapter() {
		return new EclipseLinkJpaVendorAdapter();
	}

	@Bean
	@Primary
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(TenantAwareDataSource dataSource,
			IdentityZoneDeterminer tenantIdDeterminer, InstanceLifecycleManager instanceLifecycleManager) throws Exception {

		return this.setupEntityManagerFactory(dataSource, instanceLifecycleManager);
	}
	
	protected LocalContainerEntityManagerFactoryBean setupEntityManagerFactory(TenantAwareDataSource dataSource,
			InstanceLifecycleManager instanceLifecycleManager) throws Exception {

		String dummyTenent = "DUMMY_TENANT";
		// Refer to InstanceLifecycleManagerImpl
		boolean dummyTenantExists = false;
		try {
			instanceLifecycleManager.checkThatTenantExists(dummyTenent);
			dummyTenantExists = true;
			log.info("Dummy tenant {} already exists", dummyTenent);
		} catch (UnknownTenant e) {
			log.info("Dummy tenant {} not yet exists", dummyTenent);
		}

		if (!dummyTenantExists) {
			log.info("Create Instance for dummy tenant {}", dummyTenent);
			instanceLifecycleManager.createNewInstance(dummyTenent, new InstanceCreationOptions());
		}
		return setupEntityManagerFactory(dataSource);
	}

	@Bean
	@ConditionalOnProperty(name = Const.COM_SAP_MT_ENABLED, havingValue = "false")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
		return setupEntityManagerFactory(dataSource);
	}

	protected LocalContainerEntityManagerFactoryBean setupEntityManagerFactory(DataSource dataSource) {

		LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
		factoryBean.setDataSource(dataSource);

		factoryBean.setPersistenceProvider(new PersistenceProvider());
		List<String> packageList = new ArrayList<String>();
		packageList.add("com.sap.grc.iag.accesscertification.model");
		packageList.add("com.sap.grc.iag.accesscertification.model.ext");
		packageList.add("com.sap.grc.iag.accesscertification.workflow.model");
		factoryBean.setPackagesToScan(packageList.toArray(new String[0]));
		factoryBean.setPersistenceUnitName("com.sap.grc.iag.accesscertification");
		// factoryBean.setPersistenceXmlLocation("classpath:persistence.xml");
		// For JPA we use the class loader that Spring uses to avoid class loader issues
		factoryBean.setJpaPropertyMap(getJPAProperties());
		factoryBean.setLoadTimeWeaver(new SimpleLoadTimeWeaver());
		factoryBean.setJpaVendorAdapter(new EclipseLinkJpaVendorAdapter());

		factoryBean.afterPropertiesSet();

		return factoryBean;
	}

	private Map<String, Object> getJPAProperties() {

		Map<String, Object> properties = new HashMap<>();
		
		properties.put(PersistenceUnitProperties.CLASSLOADER, dataSourceProperties.getClassLoader());
		properties.put(PersistenceUnitProperties.CACHE_SHARED_DEFAULT, "false");

		// You can also tweak performance by configuring DB connection pool.
		// http://www.eclipse.org/eclipselink/documentation/2.4/jpa/extensions/p_connection_pool.htm
		properties.put(PersistenceUnitProperties.CONNECTION_POOL_MAX, 20);

		// https://github.com/eclipse-ee4j/eclipselink.git
		// BATCH_WRITING_SIZE is setup in
		// org.eclipse.persistence.internal.jpa.EntityManagerSetupImpl.updateBatchWritingSetting
		properties.put(PersistenceUnitProperties.BATCH_WRITING, "JDBC");
		properties.put(PersistenceUnitProperties.BATCH_WRITING_SIZE, "500");

		// EclipseLink is not aware of multi-tenancy which causes problem with caching
		// on entity manager factory level. Hence disable the cache on this level.
		properties.put("eclipselink.cache.shared.default", "false");

		properties.put(PersistenceUnitProperties.LOGGING_LEVEL, eclipselinkLoggingLevel);
		properties.put("eclipselink.logging.level.sql", eclipselinkLoggingLevelSql);

		properties.put(PersistenceUnitProperties.LOGGING_PARAMETERS, eclipselinkLoggingParameters);
		properties.put(PersistenceUnitProperties.ALLOW_NATIVE_SQL_QUERIES, "true");
		properties.put(PersistenceUnitProperties.LOGGING_TIMESTAMP, "false");
		properties.put(PersistenceUnitProperties.LOGGING_THREAD, "false");

		// Use static weaving
		properties.put(PersistenceUnitProperties.WEAVING, "static");
		return properties;
	}
}