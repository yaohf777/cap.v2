#!/usr/bin/env node --harmony
module.exports = function (grunt) {

    grunt.loadNpmTasks('@sap/grunt-sapui5-bestpractice-build');

    grunt.registerTask('default', [
        'clean',
        'build'
    ]);
};