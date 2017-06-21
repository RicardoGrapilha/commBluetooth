package com.jota.cordova;

import android.content.BroadcastReceiver;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import android.content.Context;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import android.content.Intent;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.PluginResult;
import android.app.Activity;
import android.content.IntentFilter;
import android.util.Log;
import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.util.Set;

public class CommBluetooth extends CordovaPlugin {
	public static int ENABLE_BLUETOOTH = 1;
	public static int SELECT_PAIRED_DEVICE = 2;
	public static int SELECT_DISCOVERED_DEVICE = 3;
	private static final int CHECK_PERMISSIONS_REQ_CODE = 2;
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_READ_RAW = 6;

	private static final String TAG = "CommBluetooth";
	public static final String DEVICE_NAME = "device_name";
	private static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
	public static final String TOAST = "toast";
	StringBuffer buffer = new StringBuffer();
	public static String handlerMessage;

	private ConnectionThread connectionThread;
	private BluetoothAdapter bluetoothAdapter;
	private CallbackContext deviceDiscoveredCallback;

	private enum Methods {
		LIST, SET_NAME, ENABLE, DISCOVER_UNPAIRED, CONNECT, SEARCH_BY_DEVICE_NAME, DEVICE_SERVER, SEND_MESSAGE, READ;
	}

	public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
		boolean validAction = true;
		LOG.d(TAG, "action = " + action);

		if (bluetoothAdapter == null) {
			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}

		Methods method = Methods.valueOf(action);

		this.deviceDiscoveredCallback = callbackContext;

		switch (method) {
		case CONNECT:
			boolean secure = true;
			connect(args, secure, callbackContext);
			break;
		case SEND_MESSAGE:
			sendMessage(args, callbackContext);
			break;
		case READ:
			callbackContext.success(handlerMessage);
			break;
		case DEVICE_SERVER:
			connectionThread = new ConnectionThread(callbackContext);
			connectionThread.start();
			break;
		case SEARCH_BY_DEVICE_NAME:
			searchByDeviceName(args, callbackContext);
			break;
		case LIST:
			listBondedDevices(callbackContext);
			break;
		case SET_NAME:
			String newName = args.getString(0);
			bluetoothAdapter.setName(newName);
			callbackContext.success();
			break;
		case ENABLE:
			enableAction(callbackContext);
			break;
		case DISCOVER_UNPAIRED:

			if (this.cordova.hasPermission(ACCESS_COARSE_LOCATION)) {
				discoverUnpairedDevices(callbackContext);
			} else {

				cordova.requestPermission(this, CHECK_PERMISSIONS_REQ_CODE, ACCESS_COARSE_LOCATION);
			}

			break;
		default:
			validAction = false;
			break;
		}

		return validAction;

	}

	public void sendMessage(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
		String message = args.getString(0);
		byte[] data = message.getBytes();
		callbackContext.success(data);
		connectionThread.write(data);
	}

	private void searchByDeviceName(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
		String deviceName = args.getString(0);

		isEnabledBlueetooth();

		JSONArray deviceList = new JSONArray();
		Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

		for (BluetoothDevice device : bondedDevices) {
			if (device.getName().equals(deviceName))
				deviceList.put(deviceToJSON(device));
		}
		if (deviceList.length() > 0)
			callbackContext.success(deviceList);
		else {
			discoverUnpairedDevices(callbackContext, deviceName);
		}
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
			// Throw an enabling error
			JSONObject returnObj = new JSONObject();

			addProperty(returnObj, "error", "enable");
			addProperty(returnObj, "message", "Bluetooth not enabled");

			callbackContext.error(returnObj);
		} else {
			JSONObject returnObj = new JSONObject();

			addProperty(returnObj, "error", "enable");
			addProperty(returnObj, "message", "Bluetooth enabled");

			callbackContext.error(returnObj);

		}

		// Else listen to initialize callback for enabling
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

	// Helpers to Check Conditions
	private boolean isNotInitialized(CallbackContext callbackContext, boolean checkIsNotEnabled) {
		if (bluetoothAdapter == null) {
			JSONObject returnObj = new JSONObject();

			addProperty(returnObj, "error", "initialize");
			addProperty(returnObj, "message", "Bluetooth not initialized");

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

			addProperty(returnObj, "error", "enable");
			addProperty(returnObj, "message", "Bluetooth not enabled");

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

	private void connect(CordovaArgs args, boolean secure, CallbackContext callbackContext) throws JSONException {
		isEnabledBlueetooth();

		String macAddress = args.getString(0);
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);

		if (device != null) {
			connectionThread = new ConnectionThread(macAddress, callbackContext);
			connectionThread.start();

			StringBuffer buffer = new StringBuffer();
			buffer.setLength(0);

			PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
			result.setKeepCallback(true);
			callbackContext.sendPluginResult(result);

		} else {
			callbackContext.error("Could not connect to " + macAddress);
		}
	}

	private void discoverUnpairedDevices(final CallbackContext callbackContext, final String deviceName)
			throws JSONException {
		isEnabledBlueetooth();

		final CallbackContext ddc = deviceDiscoveredCallback;

		final BroadcastReceiver discoverReceiver = new BroadcastReceiver() {

			private JSONArray unpairedDevices = new JSONArray();

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					try {
						if (device.getName().equals(deviceName)) {
							JSONObject o = deviceToJSON(device);
							unpairedDevices.put(o);
							if (ddc != null) {
								PluginResult res = new PluginResult(PluginResult.Status.OK, unpairedDevices);
								res.setKeepCallback(true);
								ddc.sendPluginResult(res);
							}
							cordova.getActivity().unregisterReceiver(this);
						}
					} catch (JSONException e) {
						// This shouldn't happen, log and ignore
						Log.e(TAG, "Problem converting device to JSON", e);
					}
				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					callbackContext.success(unpairedDevices);
					cordova.getActivity().unregisterReceiver(this);
				}
			}
		};

		Activity activity = cordova.getActivity();
		activity.registerReceiver(discoverReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		activity.registerReceiver(discoverReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		bluetoothAdapter.startDiscovery();
	}

	private void discoverUnpairedDevices(final CallbackContext callbackContext) throws JSONException {
		isEnabledBlueetooth();

		final CallbackContext ddc = deviceDiscoveredCallback;

		final BroadcastReceiver discoverReceiver = new BroadcastReceiver() {

			private JSONArray unpairedDevices = new JSONArray();

			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					try {
						JSONObject o = deviceToJSON(device);
						unpairedDevices.put(o);
						if (ddc != null) {
							PluginResult res = new PluginResult(PluginResult.Status.OK, o);
							res.setKeepCallback(true);
							ddc.sendPluginResult(res);
						}
					} catch (JSONException e) {
						// This shouldn't happen, log and ignore
						Log.e(TAG, "Problem converting device to JSON", e);
					}
				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					callbackContext.success(unpairedDevices);
					cordova.getActivity().unregisterReceiver(this);
				}
			}
		};

		Activity activity = cordova.getActivity();
		activity.registerReceiver(discoverReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		activity.registerReceiver(discoverReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		bluetoothAdapter.startDiscovery();
	}

	private void listBondedDevices(CallbackContext callbackContext) throws JSONException {
		isEnabledBlueetooth();

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

	private void isEnabledBlueetooth() {
		if (!bluetoothAdapter.isEnabled())
			bluetoothAdapter.enable();
	}

	public static Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			Bundle bundle = msg.getData();
			byte[] data = bundle.getByteArray("data");
			String dataString = new String(data);

			if (dataString.equals("---N"))
				handlerMessage = "An error occurred while connecting";
			else if (dataString.equals("---S"))
				handlerMessage = "Connected Devices";
			else {

				handlerMessage = new String(data);
			}
		}
	};
}
