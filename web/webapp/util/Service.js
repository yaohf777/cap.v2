jQuery.sap.declare("com.sap.rc.web.util.Service");

com.sap.rc.web.util.Service = {

	doAjax : function(path, async,type,content) {
		var params = {
			url : path, // url : "/springui5" + path,
			dataType : "json",
			contentType : "application/json",
			context : this,
			cache : false
		};
		params["type"] = type || "GET";
		if (async === false) {
			params["async"] = async;
		}
		if (content) {
			params["data"] = JSON.stringify(content);
		}		
		return jQuery.ajax(params);
	},

	updateModelData : function(view, modelData, modelName) {
		// console.debug("Ajax response: ", modelData);
		var model = view.getModel(modelName);
		if (model == null) {
			// create new JSON model
			view.setModel(new sap.ui.model.json.JSONModel(modelData),
							modelName);
		} else {
			// update existing view model
			model.setData(modelData);
			model.refresh();
		}
	},

	handleAjaxError : function(view, xhr) {

		// Try to parse as a JSON string
		// var messageText =
		// JSON.parse(oResponse.responseRaw).error.message.value;

		this.updateModelData(view, JSON.parse(xhr.responseText));
	}

};