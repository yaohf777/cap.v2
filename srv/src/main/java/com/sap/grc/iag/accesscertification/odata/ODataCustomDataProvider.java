package com.sap.grc.iag.accesscertification.odata;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.uri.UriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetCountUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;

import com.sap.cloud.sdk.service.prov.v2.rt.cds.CDSDataProvider;
import com.sap.cloud.sdk.service.prov.v2.rt.cds.exceptions.CDSRuntimeException;
import com.sap.cloud.sdk.service.prov.v2.rt.util.LocaleUtil;
import com.sap.gateway.core.api.provider.data.BaseDataProviderResponse;
import com.sap.gateway.core.api.provider.data.IDataProviderResponse;
import com.sap.gateway.core.api.srvrepo.IServiceInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
// This class is to use ODataCustomCDSProcessor instead of ODataToCDSProcessor
public class ODataCustomDataProvider extends CDSDataProvider {

	private ODataCustomCDSProcessor cdsprocessor;

	public ODataCustomDataProvider(IServiceInfo service) {
		super(service);

		this.cdsprocessor = new ODataCustomCDSProcessor();
		// setCSN(new
		// ObjectMapper().readTree(getClass().getClassLoader().getResourceAsStream("edmx/abcd.json")));
	}

	@Override
	public IDataProviderResponse readEntitySet(GetEntitySetUriInfo uriInfo, ODataContext context)
			throws ODataException {

		try {
			BaseDataProviderResponse response = new BaseDataProviderResponse();
			List<Map<String, Object>> ec = cdsprocessor.readEntitySet((UriInfo) uriInfo, context);
			response.setResultEntities(ec);
			if (uriInfo.getInlineCount() != null
					&& uriInfo.getInlineCount().name().equals(InlineCount.ALLPAGES.toString())) {
				response.setInlineCount(getInlineCount((UriInfo) uriInfo, context));
			}
			return response;
		} catch (SQLException e) {

			log.error(e.getMessage(), e);
			throw new CDSRuntimeException(CDSRuntimeException.MessageKeys.INTERNAL_ERROR, e.getMessage(),
					LocaleUtil.getLocaleforException(), HttpStatusCodes.INTERNAL_SERVER_ERROR, e);
		}
	}

	@Override
	public IDataProviderResponse countEntitySet(GetEntitySetCountUriInfo uriInfo, ODataContext context)
			throws ODataException {

		try {

			BaseDataProviderResponse response = new BaseDataProviderResponse();
			Integer count = getInlineCount((UriInfo)uriInfo, context);
			response.setCount(String.valueOf(count));
			return response;
			
		} catch (SQLException e) {
			
			log.error(e.getMessage(), e);
			throw new CDSRuntimeException(CDSRuntimeException.MessageKeys.INTERNAL_ERROR, e.getMessage(),
					LocaleUtil.getLocaleforException(), HttpStatusCodes.INTERNAL_SERVER_ERROR, e);
		} 
	}

	private int getInlineCount(UriInfo uriInfo, ODataContext context) throws ODataException, SQLException {

		Integer count = cdsprocessor.countEntitySet(uriInfo, context);
		return count;
	}

//	private void setPageParameters(UriInfo uriInfo) {
//
//		if (uriInfo.getTop() != null) {
//			cdsprocessor.setPageTop((long) uriInfo.getTop());
//		}
//		if (uriInfo.getSkip() != null) {
//			cdsprocessor.setPageSkip((long) uriInfo.getSkip());
//		}
//	}
//
//	private void cleanPageParameters() {
//
//		cdsprocessor.setPageTop(null);
//		cdsprocessor.setPageSkip(null);
//	}

}
