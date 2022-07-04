package com.sap.grc.iag.accesscertification.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sap.grc.iag.accesscertification.config.SapAggregationRole;
import com.sap.grc.iag.accesscertification.config.SapSemantics;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SapSemantics(semantics = "aggregate")
@Entity
@Table(name="DATASELECTION_USER_DETAILS_ROLES_SYSTEMS_VIEW")
@Getter @Setter @NoArgsConstructor
public class DataselectionUserDetailsRolesSystemsView {

	@Id
	@Column(name="ROW_ID")
	private String ROW_ID;
	
	@Column(name="USER_ID", insertable=false, updatable=false)
	@SapAggregationRole(aggregationRole = "dimension")
	private String USER_ID;
	
	@Column(name="CONNECTOR")
	@SapAggregationRole(aggregationRole = "dimension")
	private String CONNECTOR;

	@Column(name="PRIVILEGE")
	@SapAggregationRole(aggregationRole = "dimension")
	private String PRIVILEGE;
}