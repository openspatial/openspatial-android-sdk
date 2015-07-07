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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.primitives.Ints;
import com.unity3d.player.UnityPlayerNativeActivity;

import net.openspatial.AnalogDataEvent;
import net.openspatial.ButtonEvent;
import net.openspatial.GestureEvent;
import net.openspatial.OpenSpatialConstants;
import net.openspatial.OpenSpatialEvent;
import net.openspatial.OpenSpatialException;
import net.openspatial.OpenSpatialService;
import net.openspatial.PointerEvent;
import net.openspatial.Pose6DEvent;
import net.openspatial.Motion6DEvent;
import net.openspatial.ExtendedEvent;

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
    private static Map<Integer, Integer> mButtonMap = new HashMap<Integer, Integer>();
    private static Map<Integer, Integer> mGestureMap = new HashMap<Integer, Integer>();
    private static Map<Integer, int[]> mAnalogDataMap = new HashMap<Integer, int[]>();
    private static Map<Integer, Integer> mExtendedMap = new HashMap<Integer, Integer>();
    private static Map<Integer, Integer> mBatteryMap = new HashMap<Integer, Integer>();

    protected UnityPlugin() {}

    public static UnityPlugin getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new UnityPlugin();
        }

        return INSTANCE;
    }

    private static OpenSpatialService.OpenSpatialServiceCallback mOpenSpatialServiceCallback =
            new OpenSpatialService.OpenSpatialServiceCallback() {
                @Override
                public void deviceConnected(BluetoothDevice device) {
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
                    mPointerMap.put(deviceId, new int[2]);
                    mButtonMap.put(deviceId, new Integer(0));
                    mGestureMap.put(deviceId, new Integer(-1));
                    mAnalogDataMap.put(deviceId, new int[]{128, 128, 255});
                    mExtendedMap.put(deviceId, 0);
                    mBatteryMap.put(deviceId, new Integer(0));
                }

                @Override
                public void deviceDisconnected(BluetoothDevice device) {
                    Integer deviceId = mDeviceIdMap.inverse().get(device);
                    if (deviceId != null) {
                        mConnectedMap.put(deviceId, false);
                        mRotationMap.put(deviceId, new float[]{0.0f, 0.0f, 0.0f});
                        mGyroMap.put(deviceId, new float[]{0.0f, 0.0f, 0.0f});
                        mAccelMap.put(deviceId, new float[]{0.0f, 0.0f, 0.0f});
                        mPointerMap.put(deviceId, new int[]{0, 0});
                        mButtonMap.put(deviceId, 0);
                        mGestureMap.put(deviceId, 0);
                        mAnalogDataMap.put(deviceId, new int[]{128, 128, 255});
                        mExtendedMap.put(deviceId, 0);
                        mBatteryMap.put(deviceId, 0);
                    }
                }

                @Override
                public void eventRegistrationResult(BluetoothDevice device,
                                                    OpenSpatialEvent.EventType type,
                                                    int status) {
                }

                @Override
                public void deviceInfoReceived(BluetoothDevice device,
                                               String infoType,
                                               Bundle infoData) {
                    if(infoType.equals(OpenSpatialConstants.INFO_BATTERY_LEVEL)) {
                        int batteryLevel =
                                infoData.getInt(OpenSpatialConstants.INFO_BATTERY_LEVEL);
                        try {
                            mBatteryMap.put(mDeviceIdMap.inverse().get(device), batteryLevel);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to record battery level of device "
                                    + device.getName());
                        }
                    }
                }
            };

    private static ServiceConnection mOpenSpatialServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service Connected");
            mOpenSpatialService = ((OpenSpatialService.OpenSpatialServiceBinder)service).getService();
            mOpenSpatialService.initialize(TAG, mOpenSpatialServiceCallback);
            mOpenSpatialService.getConnectedDevices();
            mNextId = 0;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mOpenSpatialServiceCallback = null;
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

    public static String getDeviceAddress(int deviceId) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "No device with id=" + deviceId + " found");
            return null;
        }
        return device.getAddress();
    }

    public static boolean registerForButtonEvents(final int deviceId) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "Register for Button events failed: No device with id=" + deviceId);
            return false;
        }

        try {
            mOpenSpatialService.registerForEvents(device, OpenSpatialEvent.EventType.EVENT_BUTTON,
                    new OpenSpatialEvent.EventListener()
            {
                @Override
                public void onEventReceived(OpenSpatialEvent event) {
                    ButtonEvent bEvent = (ButtonEvent) event;
                    Log.d(TAG, device.getName() + ": received ButtonEvent of type " + bEvent.buttonEventType);

                    int buttons = mButtonMap.get(deviceId);
                    switch (bEvent.buttonEventType) {
                        case TOUCH0_DOWN:   buttons |=  (1 << 0); break;
                        case TOUCH0_UP:     buttons &= ~(1 << 0); break;
                        case TOUCH1_DOWN:   buttons |=  (1 << 1); break;
                        case TOUCH1_UP:     buttons &= ~(1 << 1); break;
                        case TOUCH2_DOWN:   buttons |=  (1 << 2); break;
                        case TOUCH2_UP:     buttons &= ~(1 << 2); break;
                        case TACTILE0_DOWN: buttons |=  (1 << 3); break;
                        case TACTILE0_UP:   buttons &= ~(1 << 3); break;
                        case TACTILE1_DOWN: buttons |=  (1 << 4); break;
                        case TACTILE1_UP:   buttons &= ~(1 << 4); break;
                    }

                    mButtonMap.put(deviceId, buttons);
                    Log.d(TAG, "buttons: " + Integer.toHexString(buttons));
                }
            });
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Register for Button events failed: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean registerForPointerEvents(final int deviceId) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "Register for Pointer events failed: No device with id=" + deviceId);
            return false;
        }

        try {
            mOpenSpatialService.registerForEvents(device, OpenSpatialEvent.EventType.EVENT_POINTER,
                    new OpenSpatialEvent.EventListener()
            {
                @Override
                public void onEventReceived(OpenSpatialEvent event) {
                    PointerEvent pEvent = (PointerEvent) event;
                    int[] pos2d = mPointerMap.get(deviceId);
                    pos2d[0] = pEvent.x;
                    pos2d[1] = pEvent.y;
                }
            });
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Register for Pointer events failed: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean registerForPose6DEvents(final int deviceId) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "Register for Pose6D events failed: No device with id=" + deviceId);
            return false;
        }

        try {
            mOpenSpatialService.registerForEvents(device, OpenSpatialEvent.EventType.EVENT_POSE6D,
                    new OpenSpatialEvent.EventListener()
            {
                @Override
                public void onEventReceived(OpenSpatialEvent event) {
                    Pose6DEvent pose6DEvent = (Pose6DEvent) event;
                    float[] pose = mRotationMap.get(deviceId);
                    pose[0] = pose6DEvent.pitch;
                    pose[1] = pose6DEvent.roll;
                    pose[2] = pose6DEvent.yaw;
                }
            });
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Register for Pose6D events failed: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean registerForAnalogDataEvents(final int deviceId) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "Register for AnalogData events failed: No device with id=" + deviceId);
            return false;
        }

        try {
            mOpenSpatialService.registerForEvents(device,
                    OpenSpatialEvent.EventType.EVENT_ANALOGDATA,
                    new OpenSpatialEvent.EventListener()
                    {
                        @Override
                        public void onEventReceived(OpenSpatialEvent event) {
                            AnalogDataEvent analogDataEvent = (AnalogDataEvent) event;
                            int[] pose = mAnalogDataMap.get(deviceId);
                            pose[0] = analogDataEvent.joystickX;
                            pose[1] = analogDataEvent.joystickY;
                            pose[2] = analogDataEvent.trigger;
                        }
                    });
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Register for AnalogData events failed: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean registerForMotion6DEvents(final int deviceId) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "Register for Motion6D events failed: No device with id=" + deviceId);
            return false;
        }

        try {
            mOpenSpatialService.registerForEvents(device, OpenSpatialEvent.EventType.EVENT_POSE6D,
                    new OpenSpatialEvent.EventListener()
            {
                @Override
                public void onEventReceived(OpenSpatialEvent event) {
                    Motion6DEvent Motion6DEvent = (Motion6DEvent) event;
                    float[] gyro = mGyroMap.get(deviceId);
                    gyro[0] = Motion6DEvent.gyroX;
                    gyro[1] = Motion6DEvent.gyroY;
                    gyro[2] = Motion6DEvent.gyroZ;

                    float[] accel = mAccelMap.get(deviceId);
                    accel[0] = Motion6DEvent.accelX;
                    accel[1] = Motion6DEvent.accelY;
                    accel[2] = Motion6DEvent.accelZ;
                }
            });
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Register for Motion6D events failed: " + e.getMessage());
            return false;
        }

        return true;
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
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "Register for Gesture events failed: No device with id=" + deviceId);
            return false;
        }

        try {
            mOpenSpatialService.registerForEvents(device, OpenSpatialEvent.EventType.EVENT_GESTURE,
                    new OpenSpatialEvent.EventListener()
            {
                @Override
                public void onEventReceived(OpenSpatialEvent event) {
                    GestureEvent gesture = (GestureEvent) event;
                    Log.d(TAG, device.getName() + ": got GestureEvent of type " + gesture.gestureEventType +
                            " with magnitude " + gesture.magnitude);
                    mGestureMap.put(deviceId, gesture.gestureEventType.ordinal());
                }
            });
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Register for Gesture events failed: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean unregisterFromButtonEvents(int deviceId) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "Unregister from Button events failed: No device with id=" + deviceId);
            return false;
        }
        try {
            mOpenSpatialService.unregisterForEvents(device,
                    OpenSpatialEvent.EventType.EVENT_BUTTON);
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Unregister from Button events failed: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean unregisterFromPointerEvents(int deviceId) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "Unregister from Pointer events failed: No device with id=" + deviceId);
            return false;
        }
        try {
            mOpenSpatialService.unregisterForEvents(device,
                    OpenSpatialEvent.EventType.EVENT_POINTER);
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Unregister from Pointer events failed: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean unregisterFromPose6DEvents(int deviceId) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "Unregister from Pose6D events failed: No device with id=" + deviceId);
            return false;
        }
        try {
            mOpenSpatialService.unregisterForEvents(device,
                    OpenSpatialEvent.EventType.EVENT_POSE6D);
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Unregister from Pose6D events failed: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean unregisterFromAnalogDataEvents(int deviceId) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "Unregister from AnalogData events failed: No device with id=" + deviceId);
            return false;
        }
        try {
            mOpenSpatialService.unregisterForEvents(device,
                    OpenSpatialEvent.EventType.EVENT_ANALOGDATA);
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Unregister from AnalogData events failed: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean unregisterFromMotion6DEvents(int deviceId) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "Unregister from Motion6D events failed: No device with id=" + deviceId);
            return false;
        }
        try {
            mOpenSpatialService.unregisterForEvents(device,
                    OpenSpatialEvent.EventType.EVENT_MOTION6D);
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Unregister from Motion6D events failed: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean unregisterFromGestureEvents(int deviceId) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "Unregister from Gesture events failed: No device with id=" + deviceId);
            return false;
        }
        try {
            mOpenSpatialService.unregisterForEvents(device,
                    OpenSpatialEvent.EventType.EVENT_GESTURE);
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Unregister from Gesture events failed: " + e.getMessage());
            return false;
        }

        return true;
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

    // Returns a boolean array of form [touch0, touch1, touch2]
    public static int getButtonData(int deviceId) {
        return mButtonMap.get(deviceId);
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
