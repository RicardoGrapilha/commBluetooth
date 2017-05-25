/*global cordova*/
module.exports = {

    connect: function (macAddress, success, failure) {
        cordova.exec(success, failure, "BluetoothSerial", "connect", [macAddress]);
    },

    // list bound devices
    list: function (success, failure) {
        cordova.exec(success, failure, "CommBluetooth", "list", []);
    },


    setName: function (newName) {
        cordova.exec(null, null, "CommBluetooth", "setName", [newName]);
    }


};
