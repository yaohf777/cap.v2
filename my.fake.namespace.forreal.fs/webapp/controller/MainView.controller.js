'use strict';
sap.ui.define([
  'my/fake/namespace/forreal/fs/controller/BaseController'
], function(Controller) {


  return Controller.extend('my.fake.namespace.forreal.fs.controller.MainView', {

    onInit: function () {

      this.getView().setModel(this.getACertModel());

    }

  });
});
