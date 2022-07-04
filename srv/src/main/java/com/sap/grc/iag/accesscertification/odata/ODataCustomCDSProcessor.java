package com.sap.grc.iag.accesscertification.odata;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.uri.UriInfo;

import com.sap.cloud.sdk.hana.sql.DatasourceFactory;
import com.sap.cloud.sdk.hana.sql.HanaDatasource;
import com.sap.cloud.sdk.service.prov.api.util.RequestProcessingHelper;
import com.sap.cloud.sdk.service.prov.rt.cds.domain.Parameter;
import com.sap.cloud.sdk.service.prov.rt.datasource.factory.Datasource;
import com.sap.cloud.sdk.service.prov.v2.rt.cds.HanaQueryHelperV2;
import com.sap.cloud.sdk.service.prov.v2.rt.cds.ODataToCDSProcessor;
import com.sap.cloud.sdk.service.prov.v2.rt.cds.ResultSetProcessor;
import com.sap.grc.iag.accesscertification.infrastructure.UserContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
// This class is to 1. get rid of name space 2. use the right DB artifact
// It is performed in method getSQL()
public class ODataCustomCDSProcessor extends ODataToCDSProcessor {

	// http://localhost:8080/v2/odata/DataselectionUserDetailsRolesSystemsView?$select=USER_ID,CONNECTOR,PRIVILEGE&$top=1000&$skip=0
	// http://localhost:8080/v2/odata/DataselectionUserDetailsRolesSystemsView/$count
	// http://localhost:8080/v2/odata/DataselectionUserDetailsRolesSystemsView?$select=ROW_ID,USER_ID,CONNECTOR,PRIVILEGE&$top=0&$inlinecount=allpages
	// http://localhost:8080/cap/odata/$metadata

	private static final String limitOffset = "LIMIT ? OFFSET ?";

	private static Map<String, String> nameMap = new HashMap<String, String>();

	public ODataCustomCDSProcessor() {
		
		// Switch on HANA as data source provider
		RequestProcessingHelper.setDatasourceProvider("HANA");
	}
	
	public List<Map<String, Object>> readEntitySet(UriInfo uriInfo, ODataContext context)
			throws ODataException, SQLException {

		HanaQueryHelperV2 queryHelper = new HanaQueryHelperV2(uriInfo, context, null);
		if (uriInfo.getTop() != null) {
			
			// For oData, $top=0 means select all
			// Don't set the value if $top=0 which leads to nothing selected
			long topCount = uriInfo.getTop();
			if(topCount > 0 ) {
				queryHelper.setTopValue((long) uriInfo.getTop());
			}
		}
		if (uriInfo.getSkip() != null) {
			queryHelper.setSkipValue((long) uriInfo.getSkip());
		}
		Datasource datasource = DatasourceFactory.getDatasource(queryHelper);
		Deque<Parameter> parameters = ((HanaDatasource) datasource).getHanaQuery().getParameters();
		String sql = getSQL(datasource, queryHelper);
		log.info("Generated SQL>>>> {}.", sql);

		List<Map<String, Object>> resultProcessor = null;
		try (Connection conn = UserContext.getDatasource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet result = execute(sql, conn, stmt, parameters);) {
			resultProcessor = ResultSetProcessor.toEntityCollection(result, queryHelper.getEntityInfo());
		}
		return resultProcessor;
	}

	// Refer to HanaQueryBuilder.setTable()
	private String getSQL(Datasource datasource, HanaQueryHelperV2 queryHelper) {

		String entityName = queryHelper.getEntityInfo().getEntityName();

		// Prepare the real table name
		String databaseName = getDatabaseName(entityName);

		// Replace the CAP table name with the real table name
		String sql = datasource.getSQL();
		String capTableName = queryHelper.getSchema().replace("._.", "::") + "_" + entityName;
		return StringUtils.replace(sql, capTableName, databaseName);
	}

	// DataselectionUserDetailsRolesSystemsView->DATASELECTION_USER_DETAILS_ROLES_SYSTEMS_VIEW
	private String getDatabaseName(String entityName) {

		String databaseName = nameMap.get(entityName);
		if (databaseName == null) {
			List<String> nameList = new ArrayList<String>();
			for (String individualStr : entityName.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
				nameList.add(individualStr);
			}
			databaseName = StringUtils.join(nameList, "_").toUpperCase();
			nameMap.put(entityName, databaseName);
		}
		return databaseName;
	}

	public Integer countEntitySet(UriInfo uriInfo, ODataContext context) throws ODataException, SQLException {

		HanaQueryHelperV2 queryHelper = new HanaQueryHelperV2(uriInfo, context, null, true, false);
		Datasource datasource = DatasourceFactory.getDatasource(queryHelper);
		Deque<Parameter> parameters = ((HanaDatasource) datasource).getHanaQuery().getParameters();
		int size = parameters.size();
		while (size-- > 0) {
			Parameter parameter = parameters.removeFirst();
			if (parameter.getType() == "Edm.Int64")
				parameter.setValue(null);
			parameters.addLast(parameter);
		}
		String sql = getSQL(datasource, queryHelper);
		int pos = sql.indexOf(limitOffset);
		if (pos != -1) {
			String temp = sql.substring(0, pos - 1);
			sql = temp;
		}
		/*
		 * For aggregated queries we need to wrap the sql separately within a SQL that
		 * would retrieve the count from the original SQL.
		 */
		if (queryHelper.isSqlAggregated()) {
			sql = "SELECT COUNT(*) AS C FROM ( " + sql + " )";
		}
		log.info("Generated SQL>>>> {}.", sql);

		Integer count = null;
		try (Connection conn = UserContext.getDatasource().getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet result = execute(sql, conn, stmt, parameters);) {
			if (result != null) {
				if (result.next() != false) {
					do {
						count = result.getInt(1);
						break;
					} while (result.next());
				}
			}
		}

		return count;
	}

	// Copied from ODataToCDSProcessor removing SAPStatistic and Schema
	private ResultSet execute(String sqlQuery, Connection conn, PreparedStatement stmt, Deque<Parameter> deque)
			throws SQLException {

		int index = 1;
		while (deque != null && !deque.isEmpty()) {
			Parameter param = deque.remove();
			switch (param.getType()) {
			case "Edm.Int":
				stmt.setInt(index, Integer.parseInt(param.getValue()));
				break;
			case "Edm.Int64":
				if (param.getValue() == null)
					;
				else
					stmt.setLong(index, Long.parseLong(param.getValue()));
				break;
			case "Edm.Double":
				stmt.setDouble(index, Double.parseDouble(param.getValue()));
				break;
			case "Edm.Single":
				stmt.setFloat(index, Float.parseFloat(param.getValue()));
				break;
			case "Edm.Decimal":
				double val = Double.parseDouble(param.getValue());
				stmt.setBigDecimal(index, BigDecimal.valueOf(val));
				break;
			case "Edm.Binary":
				stmt.setBytes(index, param.getValue().getBytes());
				break;
			case "Edm.Guid":
				stmt.setString(index, param.getValue().toLowerCase());
				break;
			case "Edm.String":
				stmt.setString(index, param.getValue());
				break;
			case "Edm.Date":
			default:
				stmt.setString(index, param.getValue());
			}
			index++;
		}

		return stmt.executeQuery();
	}
}
