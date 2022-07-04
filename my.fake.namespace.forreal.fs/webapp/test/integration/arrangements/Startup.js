sap.ui.define([
  "sap/ui/test/Opa5"
], function(Opa5) {
  "use strict";

  return Opa5.extend("my.fake.namespace.forreal.fs.test.integration.arrangements.Startup", {

    iStartMyApp: function () {
      this.iStartMyUIComponent({
        componentConfig: {
          name: "my.fake.namespace.forreal.fs",
          async: true,
          manifest: true
        }
      });
    }

  });
});
