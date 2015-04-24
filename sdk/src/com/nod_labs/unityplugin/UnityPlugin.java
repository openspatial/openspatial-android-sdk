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

import net.openspatial.ButtonEvent;
import net.openspatial.GestureEvent;
import net.openspatial.OpenSpatialEvent;
import net.openspatial.OpenSpatialException;
import net.openspatial.OpenSpatialService;
import net.openspatial.PointerEvent;
import net.openspatial.Pose6DEvent;
import net.openspatial.Motion6DEvent;

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

    private static Map<Integer, float[]> mRotationMap = new HashMap<Integer, float[]>();
    private static Map<Integer, float[]> mGyroMap = new HashMap<Integer, float[]>();
    private static Map<Integer, float[]> mAccelMap = new HashMap<Integer, float[]>();
    private static Map<Integer, int[]> mPointerMap = new HashMap<Integer, int[]>();
    private static Map<Integer, Integer> mButtonMap = new HashMap<Integer, Integer>();
    private static Map<Integer, Integer> mGestureMap = new HashMap<Integer, Integer>();

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

                    // FIXME: not ideal for generating id, use hash of address
                    int deviceId = mNextId++;
                    mDeviceIdMap.put(deviceId, device);

                    mRotationMap.put(deviceId, new float[3]);
                    mGyroMap.put(deviceId, new float[3]);
                    mAccelMap.put(deviceId, new float[3]);
                    mPointerMap.put(deviceId, new int[2]);
                    mButtonMap.put(deviceId, new Integer(0));
                    mGestureMap.put(deviceId, new Integer(-1));
                }

                @Override
                public void deviceDisconnected(BluetoothDevice device) {
                    Integer deviceId = mDeviceIdMap.inverse().get(device);
                    if (deviceId != null) {
                        mRotationMap.put(deviceId, new float[]{0.0f, 0.0f, 0.0f});
                        mGyroMap.put(deviceId, new float[]{0.0f, 0.0f, 0.0f});
                        mAccelMap.put(deviceId, new float[]{0.0f, 0.0f, 0.0f});
                        mPointerMap.put(deviceId, new int[]{0, 0});
                        mButtonMap.put(deviceId, 0);
                        mGestureMap.put(deviceId, 0);
                    }
                }

                @Override
                public void buttonEventRegistrationResult(BluetoothDevice device, int status) {
                }

                @Override
                public void pointerEventRegistrationResult(BluetoothDevice device, int status) {
                }

                @Override
                public void pose6DEventRegistrationResult(BluetoothDevice device, int status) {
                }

                @Override
                public void gestureEventRegistrationResult(BluetoothDevice device, int status) {
                }

                @Override
                public void motion6DEventRegistrationResult(BluetoothDevice device, int status) {
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

    public String nodGetName(int deviceId) {
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

    public static int getNumDevices(){
        return mDeviceIdMap.size();
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
            mOpenSpatialService.registerForButtonEvents(device, new OpenSpatialEvent.EventListener() {
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
            mOpenSpatialService.registerForPointerEvents(device, new OpenSpatialEvent.EventListener() {
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
            mOpenSpatialService.registerForPose6DEvents(device, new OpenSpatialEvent.EventListener() {
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

    public static boolean registerForMotion6DEvents(final int deviceId) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "Register for Motion6D events failed: No device with id=" + deviceId);
            return false;
        }

        try {
            mOpenSpatialService.registerForMotion6DEvents(device, new OpenSpatialEvent.EventListener() {
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

    public static boolean registerForGestureEvents(final int deviceId) {
        final BluetoothDevice device = mDeviceIdMap.get(deviceId);
        if (device == null) {
            Log.e(TAG, "Register for Gesture events failed: No device with id=" + deviceId);
            return false;
        }

        try {
            mOpenSpatialService.registerForGestureEvents(device, new OpenSpatialEvent.EventListener() {
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
            mOpenSpatialService.unRegisterForButtonEvents(device);
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
            mOpenSpatialService.unRegisterForPointerEvents(device);
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
            mOpenSpatialService.unRegisterForPose6DEvents(device);
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Unregister from Pose6D events failed: " + e.getMessage());
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
            mOpenSpatialService.unRegisterForMotion6DEvents(device);
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
            mOpenSpatialService.unRegisterForGestureEvents(device);
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Unregister from Gesture events failed: " + e.getMessage());
            return false;
        }

        return true;
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

    // Returns a float array of form [gyroX, gyroY, gyroZ] (units: radians/sec)
    public static float[] getGyroData(int deviceId) {
        return mGyroMap.get(deviceId);
    }

    // Returns a float array of form [accelX, accelY, accelZ] (units: G's)
    public static float[] getAccelData(int deviceId) {
        return mAccelMap.get(deviceId);
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
