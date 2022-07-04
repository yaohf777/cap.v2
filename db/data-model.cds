namespace  ac;
using cds;

@cds.persistence.exists
@sap.semantics: 'aggregate'
entity DATASELECTION_USER_DETAILS_ROLES_SYSTEMS_VIEW {

    @sap.aggregation.role: 'dimension'
    key USER_ID     : String(50);
    @sap.aggregation.role: 'dimension'
    key CONNECTOR   : String(20);
    @sap.aggregation.role: 'dimension'
    key PRIVILEGE   : String(300);
}