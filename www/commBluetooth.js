/*global cordova*/
module.exports = {

    connect: function (macAddress, success, failure) {
        cordova.exec(success, failure, "CommBluetooth", "CONNECT", [macAddress]);
    },

    // list bound devices
    list: function (success, failure) {
        cordova.exec(success, failure, "CommBluetooth", "LIST", []);
    },

    setName: function (newName) {
        cordova.exec(null, null, "CommBluetooth", "SET_NAME", [newName]);
    },
    enable: function (success, failure) {
        cordova.exec(success, failure, "CommBluetooth", "ENABLE", []);
    },
    discoverUnpaired: function (success, failure) {
        cordova.exec(success, failure, "CommBluetooth", "DISCOVER_UNPAIRED", []);
    },
    searchByDeviceName: function (deviceName, success, failure) {
        cordova.exec(success, failure, "CommBluetooth", "SEARCH_BY_DEVICE_NAME", [deviceName]);
    },
    deviceServer: function (success, failure) {
        cordova.exec(success, failure, "CommBluetooth", "DEVICE_SERVER", []);
    },
    sendMessage: function (macAddress , message, success, failure) {
    	// convert to ArrayBuffer
        if (typeof message === 'string') {
        	message = stringToArrayBuffer(data);
        } else if (message instanceof Array) {
            // assuming array of interger
        	message = new Uint8Array(message).buffer;
        } else if (message instanceof Uint8Array) {
        	message = message.buffer;
        }
        cordova.exec(success, failure, "CommBluetooth", "SEND_MESSAGE", [macAddress , message]);
    }
};