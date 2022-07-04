sap.ui.define([
  "sap/ui/test/Opa5",
  "my/fake/namespace/forreal/fs/test/integration/arrangements/Startup",
  "my/fake/namespace/forreal/fs/test/integration/BasicJourney"
], function(Opa5, Startup) {
  "use strict";

  Opa5.extendConfig({
    arrangements: new Startup(),
    pollingInterval: 1
  });

});
