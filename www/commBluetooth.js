/*global cordova*/
module.exports = {

    connect: function (macAddress, success, failure) {
        cordova.exec(success, failure, "CommBluetooth", "connect", [macAddress]);
    },

    // list bound devices
    list: function (success, failure) {
        cordova.exec(success, failure, "CommBluetooth", "list", []);
    },

    setName: function (newName) {
        cordova.exec(null, null, "CommBluetooth", "setName", [newName]);
    },
    enable: function (success, failure) {
        cordova.exec(success, failure, "CommBluetooth", "ENABLE", []);
    }


};