'use strict';
sap.ui.define([
	'sap/ui/core/util/MockServer',
	'sap/ui/model/json/JSONModel',
	'sap/base/util/UriParameters',
	'sap/base/Log',
	'./testConstants'
], function (MockServer, JSONModel, UriParameters, Log, testConstants) {


	/**
	 * References:
	 * * UI5 API Reference
	 * * * https://sapui5.hana.ondemand.com/#/api/sap.ui.core.util.MockServer
	 * * MockServer FAQs
	 * * * https://sapui5.hana.ondemand.com/1.30.10/docs/guide/c9a91ddaef47461c9c44bfc2198ea3f0.html
	 * * Mocking requests other than OData
	 * * * https://blogs.sap.com/2016/05/30/mock-non-odata-restfull-services-using-sapui5-mockserver/
	 */
	return {

		/**
		 * Configure and initialise the mock server
		 */
		init: function () {

			// Some constants
			const _sAppModulePath = 'my/fake/namespace/forreal/fs/';
			const _sAccessCertificationJsonFilesModulePath = _sAppModulePath + 'localService/access_certification/mockdata';

			// ACert Service
			const sACertMetadataUrl = jQuery.sap.getModulePath(_sAppModulePath + testConstants.accessCertificationService.settings.localUri.replace('.xml', ''), '.xml');
			// ensure there is a trailing slash
			const sMockServerUrlACert = /.*\/$/.test(testConstants.accessCertificationService.uri) ? testConstants.accessCertificationService.uri : testConstants.accessCertificationService.uri + '/';


			// Mock Server 1 for ACert
			this.oMockServer = new MockServer({
				rootUri: sMockServerUrlACert
			});
			const oUriParameters = jQuery.sap.getUriParameters();
			// configure
			MockServer.config({
				autoRespond: true,
				autoRespondAfter: oUriParameters.get('serverDelay') || 1000
			});

			// simulate all requests using mock data
			this.oMockServer.simulate(sACertMetadataUrl, {
				sMockdataBaseUrl: sap.ui.require.toUrl(_sAccessCertificationJsonFilesModulePath),
				//Limit loaded Entity sets to these
				aEntitySetsNames: [
					'Dataselection'
				],
				bGenerateMissingMockData: true
			});

			// Attach request loggers
			this.oMockServer.attachBefore(sap.ui.core.util.MockServer.HTTPMETHOD.GET, this._requestLogger.bind(this));
			this.oMockServer.attachAfter(sap.ui.core.util.MockServer.HTTPMETHOD.GET, this._responseLogger.bind(this));
			this.oMockServer.attachBefore(sap.ui.core.util.MockServer.HTTPMETHOD.POST, this._requestLogger.bind(this));
			this.oMockServer.attachAfter(sap.ui.core.util.MockServer.HTTPMETHOD.POST, this._responseLogger.bind(this));
			this.oMockServer.attachBefore(sap.ui.core.util.MockServer.HTTPMETHOD.PUT, this._requestLogger.bind(this));
			this.oMockServer.attachAfter(sap.ui.core.util.MockServer.HTTPMETHOD.PUT, this._responseLogger.bind(this));
			this.oMockServer.attachBefore(sap.ui.core.util.MockServer.HTTPMETHOD.DELETE, this._requestLogger.bind(this));
			this.oMockServer.attachAfter(sap.ui.core.util.MockServer.HTTPMETHOD.DELETE, this._responseLogger.bind(this));

			// Intercept and reformat requests / responses to /IagCampaigns
			//this.oMockServer.attachBefore(sap.ui.core.util.MockServer.HTTPMETHOD.POST, this._interceptAndReformatRequestBodyToMatchODataStandard.bind(this), 'IagCampaigns');
			//this.oMockServer.attachAfter(sap.ui.core.util.MockServer.HTTPMETHOD.POST, this._interceptAndReformatResponseToMatchOurCustomCoding.bind(this), 'IagCampaigns');


			// start OData Mock Servers
			this.oMockServer.start();

		},

		/**
		 * they see me rollin'
		 */
		start: function () {
			this.oMockServer.start();
		},

		/**
		 * they hatin'
		 */
		stop: function () {
			this.oMockServer.stop();
		},

		/**
		 * Destroys the object and it's references
		 * NOTE: it cannot be used anymore!
		 */
		destroy: function () {
			this.oMockServer.destroy();
		},

		// /**
		//  * The mock server does not accept saveCampaign / persistCampaign call,
		//  * because the CampaignDTO we use is not compliant with OData Standards
		//  * as it relies on extra logic in the backend's Custom OData Processor
		//  * For the sake of Unit / OPA testing let's just make this request compliant
		//  * @param {sap.ui.base.Event} oEvent: refer to https://sapui5.hana.ondemand.com/#/api/sap.ui.core.util.MockServer%23methods/attachBefore
		//  * @private
		//  */
		// _interceptAndReformatRequestBodyToMatchODataStandard: function (oEvent) {
		// 	console.info('******* IAGCAMPAIGN POST REQ INTERCEPTOR *****');
		// 	if (JSON.parse(oEvent.getParameter('oXhr').requestBody)
		// 		&& JSON.parse(oEvent.getParameter('oXhr').requestBody).CampaignName
		// 		&& JSON.parse(oEvent.getParameter('oXhr').requestBody).CampaignName.includes('MAKE_REQUEST_FAIL')) {
		// 		console.log('Not adjusting request payload because the campaign name contains a hint to make it fail.');
		// 		console.info('------------------------------');
		// 		return;
		// 	}
		// 	console.log('XHr Snapshot (1):');
		// 	console.log(JSON.parse(JSON.stringify(oEvent.getParameter('oXhr'))));
		// 	try {
		// 		let oRequestBody = JSON.parse(oEvent.getParameter('oXhr').requestBody);
		// 		oRequestBody.StartDate = `/Date(${new Date(oRequestBody.StartDate).getTime()})/`;
		// 		oRequestBody.EndDate = `/Date(${new Date(oRequestBody.EndDate).getTime()})/`;
		// 		oRequestBody.CampaignId = 'RANDOMID' + new Date().getTime();

		// 		console.log('Setting requestbody to:');
		// 		console.log(JSON.stringify(oRequestBody));

		// 		oEvent.getParameter('oXhr').requestBody = JSON.stringify(oRequestBody);

		// 		console.log('XHr Snapshot (2):');
		// 		console.log(JSON.parse(JSON.stringify(oEvent.getParameter('oXhr'))));
		// 		console.info('XHr evaluated now: ');
		// 		console.info(oEvent.getParameter('oXhr'));

		// 	} catch (oError) {

		// 		console.info('Error:');
		// 		console.info(oError);
		// 	}
		// 	console.info('------------------------------');

		// },

		// /**
		//  * Our backend's Custom OData Processor logic is not compliant
		//  * with OData standards, that's why the app's logic has to expect / check for
		//  * a different kind of response. This handler adds the 'missing' fields
		//  * to the mockserver's standard OData response
		//  * @param {sap.ui.base.Event} oEvent: refer to https://sapui5.hana.ondemand.com/#/api/sap.ui.core.util.MockServer%23methods/attachBefore
		//  * @private
		//  */
		// _interceptAndReformatResponseToMatchOurCustomCoding: function (oEvent) {
		// 	console.info('******* IAGCAMPAIGN POST RES INTERCEPTOR *****');
		// 	if (oEvent.getParameter('oEntity').CampaignName
		// 		&& oEvent.getParameter('oEntity').CampaignName.includes('MAKE_RESPONSE_FAIL')) {
		// 		console.log('Not adjusting response payload because the campaign name contains a hint to make it fail.');
		// 		console.info('------------------------------');
		// 		return;
		// 	}
		// 	console.log('oEntity Snapshot (1):');
		// 	console.log(JSON.parse(JSON.stringify(oEvent.getParameter('oEntity'))));

		// 	if (oEvent.getParameter('oEntity').CampaignId && oEvent.getParameter('oEntity').CampaignId.includes(/*reference to _interceptAndReformatRequestBodyToMatchODataStandard*/'RANDOMID') ) {
		// 		oEvent.getParameter('oEntity').results = [{
		// 			statusCode: 200,
		// 			statusMessage: oEvent.getParameter('oEntity').CampaignId
		// 		}];
		// 	}

		// 	console.log('oEntity Snapshot (2):');
		// 	console.log(JSON.parse(JSON.stringify(oEvent.getParameter('oEntity'))));
		// 	console.info('oEntity evaluated now: ');
		// 	console.info(oEvent.getParameter('oEntity'));
		// },

		/**
		 * Simple Request Logger
		 * @param {sap.ui.base.Event} oEvent: refer to https://sapui5.hana.ondemand.com/#/api/sap.ui.core.util.MockServer%23methods/attachBefore
		 * @private
		 */
		_requestLogger: function (oEvent) {
			let oParams = oEvent.getParameters();
			console.info('******* BEFORE REQUEST HANDLING *****');
			console.info(`Incoming ${oParams.oXhr.method ? oParams.oXhr.method : '??'} Request`);
			this._logParamsSnapshotAndReference(oParams);
		},

		/**
		 * Simple Response Logger
		 * @param {sap.ui.base.Event} oEvent: refer to https://sapui5.hana.ondemand.com/#/api/sap.ui.core.util.MockServer%23methods/attachAfter
		 * @private
		 */
		_responseLogger: function (oEvent) {
			let oParams = oEvent.getParameters();
			console.info('******* AFTER REQUEST HANDLING *****');
			console.info(`Handled ${oParams.oXhr.method ? oParams.oXhr.method : '??'} Request/Response [${oParams.oXhr.status ? oParams.oXhr.status : '??'} ${oParams.oXhr.statusText ? oParams.oXhr.statusText : '??'}] `);
			this._logParamsSnapshotAndReference(oParams);
		},

		/**
		 * Just an outsourcing function for _responseLogger & _requestLogger to avoid code duplication
		 * @param {Object} oParams: MockServer built-in request processing params (refer to https://sapui5.hana.ondemand.com/#/api/sap.ui.core.util.MockServer%23methods/attachBefore)
		 * @private
		 */
		_logParamsSnapshotAndReference: function (oParams) {
			console.info(`URL: ${oParams.oXhr.url ? oParams.oXhr.url : '??'}`);
			console.info('Params snapshot:');
			console.info(JSON.parse(JSON.stringify(oParams)));
			console.info('Params evaluated now:');
			console.info(oParams);
			console.info('------------------------------');
		}
	};

});
