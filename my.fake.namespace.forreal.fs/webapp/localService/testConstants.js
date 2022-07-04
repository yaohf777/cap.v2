'use strict';
sap.ui.define([], function () {


	return  {

		accessCertificationService: {
            uri: '/v2/Dummy/',
                    type: 'OData',
                    settings: {
                        odataVersion: '2.0',
                        localUri: 'localService/access_certification/metadata.xml',
                        annotations: [
                            'annotation0'
                        ]
                    }
                }

	};
});