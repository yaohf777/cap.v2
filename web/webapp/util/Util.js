jQuery.sap.require("jquery.sap.resources");
jQuery.sap.require("sap.m.MessageBox");
jQuery.sap.declare("com.sap.rc.web.util.Util");

com.sap.rc.web.util.Util = {

	getResourceBundle: function() {

		if (!com.sap.rc.util.Util._bundle) {

			//var sLocale = sap.ui.getCore().getConfiguration().getLanguage();
			var path = jQuery.sap.getModulePath("com.sap.rc");
			var url = path + "/i18n/i18n.properties";

			com.sap.rc.util.Util._bundle = jQuery.sap.resources({
				url: url
					//,locale: sLocale
			});
		}
		return com.sap.rc.util.Util._bundle;
	},


	displayMessageText: function(messageText, titleText, messageType) {

		var titleStr;
		var bundle = com.sap.rc.util.Util.getResourceBundle();

		//Check if this is XML
		switch (messageType) {
			case sap.m.MessageBox.Icon.SUCCESS:
				titleStr = bundle.getText("msgSuccess");
				break;

			case sap.m.MessageBox.Icon.WARNING:
				titleStr = bundle.getText("msgWarning");
				break;

			case sap.m.MessageBox.Icon.ERROR:
				titleStr = bundle.getText("msgError");
				break;
		}

		if (titleText !== undefined && titleText !== "") {
			titleStr = titleText;
		}

		sap.m.MessageBox.show(messageText, {
			icon: messageType,
			title: titleStr
		});
	},

	getSelectedParameter: function(array, parameter) {

		for (var i = 0; i < array.length; i++) {

			var params = array[i].split("&");

			for (var f = 0; f < params.length; f++) {

				var kk = params[f].split("=");
				var vk = {
					name: kk[0],
					value: kk[1]
				};

				if (vk.name === parameter) {
					break;
				} else {
					vk = "";
				}

			}

		}

		return vk;
	}

};