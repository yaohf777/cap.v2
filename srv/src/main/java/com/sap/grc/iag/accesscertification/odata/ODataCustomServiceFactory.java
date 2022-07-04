package com.sap.grc.iag.accesscertification.odata;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;

import com.sap.cloud.sdk.service.prov.v2.rt.core.CloudSDKODataServiceFactory;
import com.sap.gateway.core.api.provider.delegate.ProviderFactory;
import com.sap.gateway.core.api.srvrepo.IServiceInfo;
import com.sap.gateway.core.api.srvrepo.ServiceRepositoryProviderFacade;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ODataCustomServiceFactory extends CloudSDKODataServiceFactory {

	private static ODataService oDataService;
	
	@Override
	public ODataService createService(ODataContext context) throws ODataException {
		
		if(oDataService == null) {
			
			String namespace = "";//"com.sap.grc.iag.accesscertification";
			String serviceName = "DataService";
			int version = 2;
			
			IServiceInfo gwService = ServiceRepositoryProviderFacade.getService(namespace, serviceName, version);
			EdmProvider edmProvider = ProviderFactory.getEdmModelProvider(namespace, serviceName, version, context, gwService);
			ODataSingleProcessor processor = ProviderFactory.getODataSingleProcessor(gwService, null);

			log.info("ServiceFactory:createService(ODataContext context) END");
			oDataService = createODataSingleProcessorService(edmProvider, processor);
		}

		return oDataService;
	}
}
