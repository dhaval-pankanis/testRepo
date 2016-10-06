cordova.define("gold.cam.kit.GoldCamKitPlugin", function(require, exports, module) {

var exec = require('cordova/exec');
    // XXX: commented out
    //CameraPopoverHandle = require('./CameraPopoverHandle');

var cameraExport = {};


cameraExport.openCam = function(successCallback, errorCallback, options) {

    exec(successCallback, errorCallback, "GoldCamKitPlugin", "openCamera", []);
    // XXX: commented out
    //return new CameraPopoverHandle();
};

module.exports = cameraExport;

});