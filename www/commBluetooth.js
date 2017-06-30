/*global cordova*/

var commBluetooth = function() {
};
var exec = require('cordova/exec');

	commBluetooth.getName = function (success, failure) {
	    exec(success, failure, "CommBluetooth", "GET_NAME", []);
	};
    commBluetooth.connect = function (macAddress, success, failure) {
        exec(success, failure, "CommBluetooth", "CONNECT", [macAddress]);
    };
    commBluetooth.disconnect = function ( success, failure) {
        exec(success, failure, "CommBluetooth", "DISCONNECT", []);
    };
    commBluetooth.isEnable = function ( success, failure) {
        exec(success, failure, "CommBluetooth", "IS_ENABLE", []);
    };
    // list bound devices
    commBluetooth.list = function (success, failure) {
        exec(success, failure, "CommBluetooth", "LIST", []);
    };

    commBluetooth.setName = function (newName) {
        exec(null, null, "CommBluetooth", "SET_NAME", [newName]);
    };
    commBluetooth.enable = function (success, failure) {
        exec(success, failure, "CommBluetooth", "ENABLE", []);
    };
    commBluetooth.discoverUnpaired = function (success, failure) {
        exec(success, failure, "CommBluetooth", "DISCOVER_UNPAIRED", []);
    };
    commBluetooth.searchByDeviceName = function (deviceName, success, failure) {
        exec(success, failure, "CommBluetooth", "SEARCH_BY_DEVICE_NAME", [deviceName]);
    };
    commBluetooth.deviceServer = function (success, failure) {
        exec(success, failure, "CommBluetooth", "DEVICE_SERVER", []);
    };
    commBluetooth.sendMessage = function (message, success, failure) {
    	// convert to ArrayBuffer
        /*if (typeof message === 'string') {
        	message = stringToArrayBuffer(message);
        } else if (message instanceof Array) {
            // assuming array of interger
        	message = new Uint8Array(message).buffer;
        } else if (message instanceof Uint8Array) {
        	message = message.buffer;
        }*/
        console.log("message para java", message);
        exec(success, failure, "CommBluetooth", "SEND_MESSAGE", [message]);
    },
    commBluetooth.arrayBufferToStr = function(arrayBuf){
    	return decodeBase64(String.fromCharCode.apply(null, new Uint8Array(arrayBuf)));
    };
    commBluetooth.read = function (success, failure) {
        exec(success, failure, "CommBluetooth", "READ", []);
    };
    commBluetooth.disconnect = function (success,failure){
    	exec(success, failure, "CommBluetooth", "DISCONNECT", []);
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


	module.exports = commBluetooth;