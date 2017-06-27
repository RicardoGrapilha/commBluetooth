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
    sendMessage: function (message, success, failure) {
    	// convert to ArrayBuffer
        if (typeof message === 'string') {
        	message = stringToArrayBuffer(message);
        } else if (message instanceof Array) {
            // assuming array of interger
        	message = new Uint8Array(message).buffer;
        } else if (message instanceof Uint8Array) {
        	message = message.buffer;
        }
        console.log("message para java", message);
        cordova.exec(success, failure, "CommBluetooth", "SEND_MESSAGE", [message]);
    },
    arrayBufferToStr: function(arrayBuf){
    	return decodeBase64(String.fromCharCode.apply(null, new Uint8Array(arrayBuf)));
    },
    read: function (success, failure) {
        cordova.exec(success, failure, "CommBluetooth", "READ", []);
    },
    disconnect: function (success,failure){
    	cordova.exec(success, failure, "CommBluetooth", "DISCONNECT", []);
    }
    
};

var stringToArrayBuffer = function(str) {
    var ret = new Uint8Array(str.length);
    for (var i = 0; i < str.length; i++) {
        ret[i] = str.charCodeAt(i);
    }
    return ret.buffer;
};

var decodeBase64 = function(s) {
    var e={},i,b=0,c,x,l=0,a,r='',w=String.fromCharCode,L=s.length;
    var A="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    for(i=0;i<64;i++){e[A.charAt(i)]=i;}
    for(x=0;x<L;x++){
        c=e[s.charAt(x)];b=(b<<6)+c;l+=6;
        while(l>=8){((a=(b>>>(l-=8))&0xff)||(x<(L-2)))&&(r+=w(a));}
    }
    return r;
};
