/*
 * Copyright 2015, Nod Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nod_labs.unityplugin;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.app.Activity;
import android.util.SparseArray;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.primitives.Ints;
import com.unity3d.player.UnityPlayerNativeActivity;

import net.openspatial.AccelerometerData;
import net.openspatial.AnalogData;
import net.openspatial.AnalogDataEvent;
import net.openspatial.ButtonData;
import net.openspatial.ButtonEvent;
import net.openspatial.ButtonState;
import net.openspatial.DataType;
import net.openspatial.DeviceParameter;
import net.openspatial.EulerData;
import net.openspatial.GestureData;
import net.openspatial.GestureEvent;
import net.openspatial.GyroscopeData;
import net.openspatial.OpenSpatialConstants;
import net.openspatial.OpenSpatialData;
import net.openspatial.OpenSpatialEvent;
import net.openspatial.OpenSpatialException;
import net.openspatial.OpenSpatialInterface;
import net.openspatial.OpenSpatialService;
import net.openspatial.PointerEvent;
import net.openspatial.Pose6DEvent;
import net.openspatial.Motion6DEvent;
import net.openspatial.ExtendedEvent;
import net.openspatial.RelativeXYData;
import net.openspatial.ResponseCode;
import net.openspatial.TranslationData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class UnityPlugin {

    private static OpenSpatialService mOpenSpatialService;
    private static String TAG = UnityPlugin.class.getSimpleName();

    private static UnityPlugin INSTANCE = null;

    private static Activity mActivity;

    private static int mNextId = 0;
    private static BiMap<Integer, BluetoothDevice> mDeviceIdMap = HashBiMap.create();

    private static Map<Integer, Boolean> mConnectedMap = new HashMap<Integer, Boolean>();
    private static Map<Integer, float[]> mRotationMap = new HashMap<Integer, float[]>();
    private static Map<Integer, float[]> mGyroMap = new HashMap<Integer, float[]>();
    private static Map<Integer, float[]> mAccelMap = new HashMap<Integer, float[]>();
    private static Map<Integer, int[]> mPointerMap = new HashMap<Integer, int[]>();
    private static Map<Integer, Map<Integer, Integer>> mButtonMap
            = new HashMap<Integer, Map<Integer, Integer>>();
    private static Map<Integer, Integer> mGestureMap = new HashMap<Integer, Integer>();
    private static Map<Integer, int[]> mAnalogDataMap = new HashMap<Integer, int[]>();
    private static Map<Integer, Integer> mExtendedMap = new HashMap<Integer, Integer>();
    private static Map<Integer, Integer> mBatteryMap = new HashMap<Integer, Integer>();
    private static SparseArray<float[]> mTranslationMap = new SparseArray<float[]>();

    protected UnityPlugin() {}

    public static UnityPlugin getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new UnityPlugin();
        }

        return INSTANCE;
    }

    private static OpenSpatialInterface mOpenSpatialInterface = new OpenSpatialInterface() {
        @Override
        public void onDeviceConnected(BluetoothDevice device) {
            Log.d(TAG, "Connected to device: " + device.getAddress());

            int deviceId;
            if (mDeviceIdMap.containsValue(device)) {
                deviceId = mDeviceIdMap.inverse().get(device);
            } else {
                // FIXME: not ideal for generating id, use hash of address
                deviceId = mNextId++;
                mDeviceIdMap.put(deviceId, device);
            }

            mConnectedMap.put(deviceId, true);
            mRotationMap.put(deviceId, new float[3]);
            mGyroMap.put(deviceId, new float[3]);
            mAccelMap.put(deviceId, new float[3]);
            mTranslationMap.put(deviceId, new float[3]);
            mPointerMap.put(deviceId, new int[2]);
            mButtonMap.put(deviceId, new HashMap<Integer, Integer>());
            mGestureMap.put(deviceId, new Integer(-1));
            mAnalogDataMap.put(deviceId, new int[]{128, 128, 255});
            mExtendedMap.put(deviceId, 0);
            mBatteryMap.put(deviceId, new Integer(0));
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device) {
            Integer deviceId = mDeviceIdMap.inverse().get(device);
            if (deviceId != null) {
                mConnectedMap.put(deviceId, false);
                mRotationMap.put(deviceId, new float[]{0.0f, 0.0f, 0.0f});
                mGyroMap.put(deviceId, new float[]{0.0f, 0.0f, 0.0f});
                mAccelMap.put(deviceId, new float[]{0.0f, 0.0f, 0.0f});
                mTranslationMap.put(deviceId, new float[]{0.0f, 0.0f, 0.0f});
                mPointerMap.put(deviceId, new int[]{0, 0});
                mButtonMap.put(deviceId, new HashMap<Integer, Integer>());
                mGestureMap.put(deviceId, 0);
                mAnalogDataMap.put(deviceId, new int[]{128, 128, 255});
                mExtendedMap.put(deviceId, 0);
                mBatteryMap.put(deviceId, 0);
            }
        }

        @Override
        public void onGetParameterResponse(BluetoothDevice bluetoothDevice,
                                           DataType dataType,
                                           DeviceParameter deviceParameter,
                                           ResponseCode responseCode,
                                           short[] shorts) {

        }

        @Override
        public void onSetParameterResponse(BluetoothDevice bluetoothDevice,
                                           DataType dataType,
                                           DeviceParameter deviceParameter,
                                           ResponseCode responseCode,
                                           short[] shorts) {

        }

        @Override
        public void onGetIdentifierResponse(BluetoothDevice bluetoothDevice,
                                            DataType dataType,
                                            byte index,
                                            ResponseCode responseCode,
                                            String identifier) {

        }

        @Override
        public void onGetParameterRangeResponse(BluetoothDevice bluetoothDevice,
                                                DataType dataType,
                                                DeviceParameter deviceParameter,
                                                ResponseCode responseCode,
                                                Number low, Number high) {

        }

        @Override
        public void onDataEnabledResponse(BluetoothDevice bluetoothDevice,
                                          DataType dataType,
                                          ResponseCode responseCode) {

        }

        @Override
        public void onDataDisabledResponse(BluetoothDevice bluetoothDevice,
                                           DataType dataType, ResponseCode
                                                   responseCode) {

        }

        @Override
        public void onDataReceived(OpenSpatialData openSpatialData) {
            BluetoothDevice device = openSpatialData.device;
            Integer deviceId = mDeviceIdMap.inverse().get(device);
            if (deviceId == null) {
                Log.e(TAG, "Received data from an unknown source!");
                return;
            }

            switch (openSpatialData.dataType) {
                case BUTTON:
                    handleButtonReceipt(deviceId, openSpatialData);
                    break;
                case RAW_ACCELEROMETER:
                    handleAccelReceipt(deviceId, openSpatialData);
                    break;
                case RAW_GYRO:
                    handleGyroReceipt(deviceId, openSpatialData);
                    break;
                case RELATIVE_XY:
                    handleRelativeXYReceipt(deviceId, openSpatialData);
                    break;
                case GESTURE:
                    handleGestureReceipt(deviceId, openSpatialData);
                    break;
                case EULER_ANGLES:
                    handleEulerReceipt(deviceId, openSpatialData);
                    break;
                case ANALOG:
                    handleAnalogReceipt(deviceId, openSpatialData);
                    break;
                case TRANSLATIONS:
                    handleTranslationReceipt(deviceId, openSpatialData);
                    break;
                case RAW_COMPASS:
                case GENERAL_DEVICE_INFORMATION:
                case SLIDER:
                    Log.e(TAG, openSpatialData.dataType.name() + " is not implemented.");
            }

        }
    };

    private static ServiceConnection mOpenSpatialServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service Connected");
            mOpenSpatialService = ((OpenSpatialService.OpenSpatialServiceBinder)service).getService();
            mOpenSpatialService.initialize(TAG, mOpenSpatialInterface);
            mOpenSpatialService.getConnectedDevices();
            mNextId = 0;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mOpenSpatialInterface = null;
            mOpenSpatialService = null;
            mNextId = 0;
        }
    };

    public static String nodGetName(int deviceId) {
        BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if(device == null) {
            Log.e(TAG, "Requested the name of an unknown device!");
            return "";
        }

        return device.getName();
    }

    public static int[] getDeviceIds() {
        return Ints.toArray(mDeviceIdMap.keySet());
    }

    public static int getNumDevices() {
        Collection<Boolean> connected = mConnectedMap.values();

        int result = 0;

        for (Boolean connState : connected) {
            result += connState ? 1 : 0;
        }

        return result;
    }

    public static int getNumButtons() { return 10;}

    public static String getDeviceAddress(int deviceId) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "No device with id=" + deviceId + " found");
            return null;
        }
        return device.getAddress();
    }

    private static boolean enableData(int deviceId, DataType dataType) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "Enabling " + dataType.name() + " data failed: No device with id="
                    + deviceId);
            return false;
        }

        mOpenSpatialService.enableData(device, dataType);
        return true;
    }

    private static boolean disableData(int deviceId, DataType dataType) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "Disabling " + dataType.name() + " data failed: No device with id="
                    + deviceId);
            return false;
        }

        mOpenSpatialService.disableData(device, dataType);
        return true;
    }

    private static void handleButtonReceipt(int deviceId, OpenSpatialData openSpatialData) {
        ButtonData data = (ButtonData) openSpatialData;
        int id = data.getButtonId();
        ButtonState state = data.getButtonState();

        Map<Integer, Integer> buttonMap = mButtonMap.get(deviceId);
        buttonMap.put(id, state.equals(ButtonState.UP) ? 0 : 1);
    }

    private static void handleAccelReceipt(int deviceId, OpenSpatialData openSpatialData) {
        AccelerometerData data = (AccelerometerData) openSpatialData;
        float[] accelData = mAccelMap.get(deviceId);

        accelData[0] = data.getX();
        accelData[1] = data.getY();
        accelData[2] = data.getZ();
    }

    private static void handleGyroReceipt(int deviceId, OpenSpatialData openSpatialData) {
        GyroscopeData data = (GyroscopeData) openSpatialData;
        float[] gyroData = mGyroMap.get(deviceId);

        gyroData[0] = data.getX();
        gyroData[1] = data.getY();
        gyroData[2] = data.getZ();
    }

    private static void handleRelativeXYReceipt(int deviceId, OpenSpatialData openSpatialData) {
        RelativeXYData data = (RelativeXYData) openSpatialData;
        int[] xyData = mPointerMap.get(deviceId);

        xyData[0] = data.getX();
        xyData[1] = data.getY();
    }

    private static void handleGestureReceipt(int deviceId, OpenSpatialData openSpatialData) {
        GestureData data = (GestureData) openSpatialData;
        mGestureMap.put(deviceId, data.getGestureType().ordinal());
    }

    private static void handleEulerReceipt(int deviceId, OpenSpatialData openSpatialData) {
        EulerData data = (EulerData) openSpatialData;
        float[] eulerData = mRotationMap.get(deviceId);

        eulerData[0] = data.getRoll();
        eulerData[1] = data.getPitch();
        eulerData[2] = data.getYaw();
    }

    private static void handleTranslationReceipt(int deviceId, OpenSpatialData openSpatialData) {
        TranslationData data = (TranslationData) openSpatialData;
        float[] translationData = mTranslationMap.get(deviceId);

        translationData[0] = data.getX();
        translationData[1] = data.getY();
        translationData[2] = data.getZ();
    }

    private static void handleAnalogReceipt(int deviceId, OpenSpatialData openSpatialData) {
        AnalogData data = (AnalogData) openSpatialData;
        int[] analogData = mAnalogDataMap.get(deviceId);

        analogData[0] = data.getAnalogValue(0);
        analogData[1] = data.getAnalogValue(1);
        analogData[2] = data.getAnalogValue(2);
    }

    public static boolean registerForButtonEvents(final int deviceId) {
        return enableData(deviceId, DataType.BUTTON);
    }

    public static boolean sendHaptic(int deviceId, int index, int argument) {
        BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.d(TAG, "Invalid device ID.");
            return false;
        }

        if (index == 0) {
            mOpenSpatialService.setParameter(device, DataType.HAPTIC,
                    DeviceParameter.GENERIC_INDEX_0, argument);
            return true;
        }

        if (index == 1) {
            mOpenSpatialService.setParameter(device, DataType.HAPTIC,
                    DeviceParameter.GENERIC_INDEX_1, 80);
            return true;
        }

        return false;
    }

    public static boolean registerForPointerEvents(final int deviceId) {
        return enableData(deviceId, DataType.RELATIVE_XY);
    }

    public static boolean registerForPose6DEvents(final int deviceId) {
        return enableData(deviceId, DataType.EULER_ANGLES);
    }

    public static boolean registerForAnalogDataEvents(final int deviceId) {
        return enableData(deviceId, DataType.ANALOG);
    }

    public static boolean registerForMotion6DEvents(final int deviceId) {
        return enableData(deviceId, DataType.RAW_ACCELEROMETER)
                && enableData(deviceId, DataType.RAW_GYRO);
    }

    public static boolean registerForTranslationEvents(final int deviceId) {
        return enableData(deviceId, DataType.TRANSLATIONS);
    }

    public static boolean registerForExtendedEvents(final int deviceId, final String type) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);

        if (device == null) {
            Log.e(TAG, "Register for Motion6D events failed: No device with id=" + deviceId);
            return false;
        }

        try {
            mOpenSpatialService.registerForEvents(device, OpenSpatialEvent.EventType.EVENT_EXTENDED,
                    type, new OpenSpatialEvent.EventListener() {
                        @Override
                        public void onEventReceived(OpenSpatialEvent event) {
                            ExtendedEvent extendedEvent = (ExtendedEvent) event;
                            Integer value = mExtendedMap.get(deviceId);
                            value = extendedEvent.eventId;
                        }
                    });
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Register for Motion6D events failed: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean registerForGestureEvents(final int deviceId) {
        return enableData(deviceId, DataType.GESTURE);
    }

    public static boolean unregisterFromButtonEvents(int deviceId) {
        return disableData(deviceId, DataType.BUTTON);
    }

    public static boolean unregisterFromPointerEvents(int deviceId) {
        return disableData(deviceId, DataType.RELATIVE_XY);
    }

    public static boolean unregisterFromPose6DEvents(int deviceId) {
        return disableData(deviceId, DataType.EULER_ANGLES);
    }

    public static boolean unregisterFromAnalogDataEvents(int deviceId) {
        return disableData(deviceId, DataType.ANALOG);
    }

    public static boolean unregisterFromMotion6DEvents(int deviceId) {
        return disableData(deviceId, DataType.RAW_ACCELEROMETER)
                && disableData(deviceId, DataType.RAW_GYRO);
    }

    public static boolean unregisterFromGestureEvents(int deviceId) {
        return disableData(deviceId, DataType.GESTURE);
    }

    public static boolean unregisterFromTranslationEvents(int deviceId) {
        return disableData(deviceId, DataType.TRANSLATIONS);
    }

    public static boolean unregisterFromExtendedEvents(int deviceId, String type) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "Unregister from Extended events failed: No device with id=" + deviceId);
            return false;
        }
        try {
            mOpenSpatialService.unregisterForEvents(device,
                    OpenSpatialEvent.EventType.EVENT_EXTENDED, type);
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Unregister from Extended events failed: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static void requestBatteryLevel(int deviceId) {
        try {
            mOpenSpatialService.queryDeviceInfo(mDeviceIdMap.get(deviceId),
                    OpenSpatialConstants.INFO_BATTERY_LEVEL);
        } catch (Exception e) {
            Log.d(TAG, "Failed to request battery level!");
        }
    }

    public static int getBatteryLevel(int deviceId) {
        return mBatteryMap.get(deviceId);
    }

    public static int getButtonState(int deviceId, int buttonId) {
        Integer buttonState = mButtonMap.get(deviceId).get(buttonId);
        return buttonState == null ? 0 : buttonState;
    }

    // Returns an integer array of form [x, y]
    public static int[] getPointerData(int deviceId) {
        int[] pos2d =  mPointerMap.get(deviceId);

        int[] ret = new int[pos2d.length];
        System.arraycopy(pos2d, 0, ret, 0, pos2d.length);

        // reset on read
        pos2d[0] = 0;
        pos2d[1] = 0;

        return ret;
    }

    // Returns a float array of form [pitch, roll, yaw]
    public static float[] getRotationData(int deviceId) {
        return mRotationMap.get(deviceId);
    }

    public static float[] getTranslationData(int deviceId) {
        return mTranslationMap.get(deviceId);
    }

    // Returns an int array of form [joystickX, joystickY, trigger]
    public static int[] getAnalogData(int deviceId) {
        return mAnalogDataMap.get(deviceId);
    }

    // Returns a float array of form [gyroX, gyroY, gyroZ] (units: radians/sec)
    public static float[] getGyroData(int deviceId) {
        return mGyroMap.get(deviceId);
    }

    // Returns a float array of form [accelX, accelY, accelZ] (units: G's)
    public static float[] getAccelData(int deviceId) {
        return mAccelMap.get(deviceId);
    }

    // Returns an int corresponding to an ExtendedEvent
    public static int getExtendedData(int deviceId) {
        int result =  mExtendedMap.get(deviceId);
        mExtendedMap.put(deviceId, 0);
        return result;
    }

    public static int getGestureData(int deviceId) {
        int result = mGestureMap.get(deviceId);
        mGestureMap.put(deviceId, -1);
        return result;
    }

    public void init(Activity activity) {
        mActivity = activity;
        mActivity.bindService(new Intent(activity, OpenSpatialService.class),
                mOpenSpatialServiceConnection, Activity.BIND_AUTO_CREATE);
    }

    public static void shutdown() {
        mActivity.unbindService(mOpenSpatialServiceConnection);
    }
}
