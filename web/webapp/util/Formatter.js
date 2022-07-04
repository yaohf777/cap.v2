jQuery.sap.declare("com.sap.rc.web.util.Formatter");

com.sap.rc.web.util.Formatter = {
	
	getSimplNoteVersion: function(version) {

		if (version !== null && version !== "0000" && typeof version !== "undefined") {
			var tts = version.toString();
			while (tts.substr(0, 1) === "0" && tts !== "") {
				tts = tts.substr(1);
			}
			var noteVersion = "2399707" + " (" + tts + ")";
		}

		return noteVersion;

	},

	getDateTimeString: function(rDate) {

		if (rDate) {
			return rDate.toLocaleDateString() + " " + rDate.toLocaleTimeString();
		}

		return rDate;
	},

	getDateString: function(rDate) {

		if (rDate) {
			return rDate.toLocaleDateString();
		}

		return rDate;
	},

	trimNumberLeadingZero: function(tt) {

		if (tt === null) {
			return "";
		}

		var tts = tt.toString();
		while (tts.substr(0, 1) === "0" && tts !== "") {
			tts = tts.substr(1);
		}

		return tts;
	},
	
	convertNumber2LocalString: function(number) {

		var oRouter = sap.ui.core.routing.Router.getRouter("shellRouter");
		if (typeof oRouter !== "undefined") {
			var sLanguage = globalLanguage;
		} else {
			sLanguage = sap.ui.getCore().getConfiguration().getLanguage();
		}
		var floatNumber = (parseFloat(number).toFixed(2)).toLocaleString(sLanguage);

		return floatNumber;

	},

	getLieteralSyntaxStr: function(string) {

		return "'" + string + "'";
	}

};