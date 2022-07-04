/* global QUnit */

QUnit.config.autostart = false;

sap.ui.getCore().attachInit(function() {
  "use strict";

  sap.ui.require([
    "my/fake/namespace/forreal/fs/test/integration/AllJourneys"
  ], function() {
    QUnit.start();
  });
});
