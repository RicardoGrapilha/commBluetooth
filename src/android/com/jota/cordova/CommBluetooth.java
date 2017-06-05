package com.jota.cordova;

import android.Manifest;
import android.content.pm.PackageManager;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.Set;

public class CommBluetooth extends CordovaPlugin {
    public static int ENABLE_BLUETOOTH = 1;
    public static int SELECT_PAIRED_DEVICE = 2;
    public static int SELECT_DISCOVERED_DEVICE = 3;
    private CallbackContext enableBluetoothCallback;
    private CordovaPlugin activityResultCallback;
    //ConnectionThread connect;
    private BluetoothAdapter bluetoothAdapter;
    private static final String TAG = "CommBluetooth";
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private enum Methods {
    	LIST ,
        SET_NAME,
        ENABLE ;
    }


    
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
    	boolean validAction = true;
    	LOG.d(TAG, "action = " + action);

        if (bluetoothAdapter == null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        Methods method = Methods.valueOf(action); 

        switch(method) {
	        case LIST:
				listBondedDevices(callbackContext);
				break;
			case SET_NAME:
				String newName = args.getString(0);
				bluetoothAdapter.setName(newName);
				callbackContext.success();
				break;
			case ENABLE:
				enableAction( callbackContext);
	        	
	        	//cordova.startActivityForResult(this, intent, ENABLE_BLUETOOTH);
	        	//org.apache.cordova.api.CordovaPlugin cordovaP = new org.apache.cordova.api.CordovaPlugin();
	             // cordova.startActivityForResult(cordovaP ,intent, REQUEST_ENABLE_BLUETOOTH);
				break;
			 
			default:
				validAction = false;
				break;
        }
        
    	return validAction;
    	
    }
    private void enableAction(CallbackContext callbackContext) {
        if (isNotInitialized(callbackContext, false)) {
          return;
        }

        if (isNotDisabled(callbackContext)) {
          return;
        }

        boolean result = bluetoothAdapter.enable();

        if (!result) {
          //Throw an enabling error
          JSONObject returnObj = new JSONObject();

          addProperty(returnObj,  "error", "enable");
          addProperty(returnObj, "message", "Bluetooth not enabled");

          callbackContext.error(returnObj);
        }

        //Else listen to initialize callback for enabling
      }
    private void addProperty(JSONObject returnObj, String string, String string2) {
		// TODO Auto-generated method stub
    	try {
			returnObj.put(string, string2);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    //Helpers to Check Conditions
    private boolean isNotInitialized(CallbackContext callbackContext, boolean checkIsNotEnabled) {
      if (bluetoothAdapter == null) {
        JSONObject returnObj = new JSONObject();

        addProperty(returnObj,  "error", "initialize");
        addProperty(returnObj, "message",  "Bluetooth not initialized");

        callbackContext.error(returnObj);

        return true;
      }

      if (checkIsNotEnabled) {
        return isNotEnabled(callbackContext);
      } else {
        return false;
      }
    }
    private boolean isNotEnabled(CallbackContext callbackContext) {
        if (!bluetoothAdapter.isEnabled()) {
          JSONObject returnObj = new JSONObject();

          addProperty(returnObj,  "error", "enable");
          addProperty(returnObj, "message",  "Bluetooth not enabled");

          callbackContext.error(returnObj);

          return true;
        }

        return false;
      }

	private boolean isNotDisabled(CallbackContext callbackContext) {
        if (bluetoothAdapter.isEnabled()) {
          JSONObject returnObj = new JSONObject();

          addProperty(returnObj, "error", "disable");
          addProperty(returnObj, "message", "Bluetooth not disabled");

          callbackContext.error(returnObj);

          return true;
        }

        return false;
      }

    private void listBondedDevices(CallbackContext callbackContext) throws JSONException {
        JSONArray deviceList = new JSONArray();
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : bondedDevices) {
            deviceList.put(deviceToJSON(device));
        }
        callbackContext.success(deviceList);
    }
    private JSONObject deviceToJSON(BluetoothDevice device) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", device.getName());
        json.put("address", device.getAddress());
        json.put("id", device.getAddress());
        json.put("uuids", device.getUuids());
        if (device.getBluetoothClass() != null) {
            json.put("class", device.getBluetoothClass().getDeviceClass());
        }
        return json;
    }
    public void setActivityResultCallback(CordovaPlugin plugin) {
    	 this.activityResultCallback = plugin;
    }
   
}

