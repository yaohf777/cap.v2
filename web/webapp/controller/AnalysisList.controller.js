jQuery.sap.require("com.sap.rc.web.util.Service");

sap.ui.define([ "sap/ui/core/mvc/Controller", 
	            "sap/ui/model/json/JSONModel",
		        "sap/ui/model/Filter", 
		        "sap/ui/model/FilterOperator" ], 
function(Controller, JSONModel, Filter, FilterOperator) {
	
	"use strict";

	return Controller.extend("com.sap.rc.web.controller.AnalysisList", {

		onInit : function() {

			// use $.proxy for the callback to retain the context
			com.sap.rc.web.util.Service.doAjax("/api/v1/ana").done(
					$.proxy(this.onGetAnalysisSuccess, this)).fail(
					$.proxy(this.onGetAnalysisError, this));

		},

		onGetAnalysisSuccess : function(modelData) {
			com.sap.rc.web.util.Service.updateModelData(this.getView(), modelData,
					"dataModel");

		},

		onGetAnalysisError : function(xhr) {
			com.sap.rc.web.util.Service.handleAjaxError(this.getView(), xhr);

		},

		// add: function (analysis) {
		// this.doAjax("/api/v1/ana",
		// fruit).done(this.updateModelData).fail(this.handleAjaxError);
		// },
		//
		// delete: function (analysisId) {
		// this.doAjax("/api/v1/ana" + analysisId).done(this.updateModelData);
		// },
		//
		// update: function (fruit) {
		// this.doAjax("/api/v1/ana",
		// analysis).done(this.updateModelData).fail(this.handleAjaxError);
		// },

		onFilterAnalysis : function(oEvent) {

		},

		onAnalysisPress : function(oEvent) {
			var oRouter = sap.ui.core.UIComponent.getRouterFor(this);
			oRouter.navTo("analysisOverview");
		}
	});

});
