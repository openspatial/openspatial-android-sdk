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

package net.openspatial;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.*;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.*;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class OpenSpatialSingleUserService extends Service {
    public class OpenSpatialSingleUserServiceBinder extends Binder {
        public OpenSpatialSingleUserService getService() {
            return OpenSpatialSingleUserService.this;
        }
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "Connected!");
                    gatt.discoverServices();
                } else {
                    Log.d(TAG, gatt.getDevice().getName() + " disconnected!");
                    gatt.close();
                    mCallback.deviceDisconnected(gatt.getDevice());
                }
            } else {
                Log.e(TAG, "Connection to " + gatt.getDevice().getName() + " failed with status: " + status);
                mCallback.deviceConnectFailed(gatt.getDevice(), status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Discovering services failed with status: " + status);
                mCallback.deviceConnectFailed(gatt.getDevice(), status);
                return;
            }

            mCallback.deviceConnected(gatt.getDevice());
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "Descriptor " + descriptor.getUuid() + " written with status " + status);

            if (descriptor.getCharacteristic().getUuid().equals(
                    UUID.fromString(OpenSpatialConstants.OPENSPATIAL_BUTTONSTATE_CHARACTERISTIC))) {
                mCallback.buttonEventRegistrationResult(gatt.getDevice(), status);
            } else if (descriptor.getCharacteristic().getUuid().equals(
                    UUID.fromString(OpenSpatialConstants.OPENSPATIAL_POSITION_2D_CHARACTERISTIC))) {
                mCallback.pointerEventRegistrationResult(gatt.getDevice(), status);
            } else if (descriptor.getCharacteristic().getUuid().equals(
                    OpenSpatialConstants.OPENSPATIAL_GESTURE_CHARACTERISTIC)) {
                mCallback.gestureEventRegistrationResult(gatt.getDevice(), status);
            } else if (descriptor.getCharacteristic().getUuid().equals(
                    UUID.fromString(OpenSpatialConstants.OPENSPATIAL_POSE_6D_CHARACTERISTIC))) {
                mCallback.pose6DEventRegistrationResult(gatt.getDevice(), status);
            } else if (descriptor.getCharacteristic().getUuid().equals(
                    UUID.fromString(OpenSpatialConstants.OPENSPATIAL_MOTION6D_CHARACTERISTIC))) {
                mCallback.motion6DEventRegistrationResult(gatt.getDevice(), status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            OpenSpatialEvent.EventListener listener = null;
            List<OpenSpatialEvent> events = new LinkedList<OpenSpatialEvent>();

            if (characteristic.getUuid().equals(
                    UUID.fromString(OpenSpatialConstants.OPENSPATIAL_BUTTONSTATE_CHARACTERISTIC))) {
                synchronized (mButtonEventCallbacks) {
                    listener = mButtonEventCallbacks.get(gatt.getDevice());
                }

                for (OpenSpatialEvent event : mEventFactory.getButtonEventsFromCharacteristic(
                        gatt.getDevice(), characteristic.getValue())) {
                    events.add(event);
                }
            } else if (characteristic.getUuid().equals(
                    UUID.fromString(OpenSpatialConstants.OPENSPATIAL_POSITION_2D_CHARACTERISTIC))) {
                synchronized (mPointerEventCallbacks) {
                    listener = mPointerEventCallbacks.get(gatt.getDevice());
                }

                events.add(mEventFactory.getPointerEventFromCharacteristic(
                        gatt.getDevice(), characteristic.getValue()));
            } else if (characteristic.getUuid().equals(
                    UUID.fromString(OpenSpatialConstants.OPENSPATIAL_GESTURE_CHARACTERISTIC))) {
                synchronized (mGestureEventCallbacks) {
                    listener = mGestureEventCallbacks.get(gatt.getDevice());
                }

                events.add(mEventFactory.getGestureEventFromCharacteristic(
                        gatt.getDevice(), characteristic.getValue()));
            } else if (characteristic.getUuid().equals(
                    UUID.fromString(OpenSpatialConstants.OPENSPATIAL_POSE_6D_CHARACTERISTIC))) {
                synchronized (m3DRotationEventCallbacks) {
                    listener = m3DRotationEventCallbacks.get(gatt.getDevice());
                }

                events.add(mEventFactory.getPose6DEventFromCharacteristic(
                        gatt.getDevice(), characteristic.getValue()));
            } else if (characteristic.getUuid().equals(
                    UUID.fromString(OpenSpatialConstants.OPENSPATIAL_MOTION6D_CHARACTERISTIC))) {
                synchronized (mMotion6DEventCallbacks) {
                    listener = mMotion6DEventCallbacks.get(gatt.getDevice());
                }

                events.add(mEventFactory.getMotion6DEventFromCharacteristic(
                        gatt.getDevice(), characteristic.getValue()));
            }

            if (listener == null) {
                Log.e(TAG, "No listener for characteristic " + characteristic.getUuid());
            } else {
                for (OpenSpatialEvent event : events) {
                    listener.onEventReceived(event);
                }
            }
        }
    };

    private final IBinder mBinder = new OpenSpatialSingleUserServiceBinder();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private OpenSpatialSingleUserServiceCallback mCallback;

    private final OpenSpatialEventFactory mEventFactory = new OpenSpatialEventFactory();
    private final Map<BluetoothDevice, BluetoothGatt> mDeviceGattMap = new HashMap<BluetoothDevice, BluetoothGatt>();

    private final HashMap<BluetoothDevice, OpenSpatialEvent.EventListener> mPointerEventCallbacks =
            new HashMap<BluetoothDevice, OpenSpatialEvent.EventListener>();
    private final HashMap<BluetoothDevice, OpenSpatialEvent.EventListener> mButtonEventCallbacks =
            new HashMap<BluetoothDevice, OpenSpatialEvent.EventListener>();
    private final HashMap<BluetoothDevice, OpenSpatialEvent.EventListener> m3DRotationEventCallbacks =
            new HashMap<BluetoothDevice, OpenSpatialEvent.EventListener>();
    private final HashMap<BluetoothDevice,OpenSpatialEvent.EventListener> mGestureEventCallbacks =
            new HashMap<BluetoothDevice, OpenSpatialEvent.EventListener>();
    private final HashMap<BluetoothDevice, OpenSpatialEvent.EventListener> mMotion6DEventCallbacks =
            new HashMap<BluetoothDevice, OpenSpatialEvent.EventListener>();

    private static final String TAG = "OpenSpatialSingleUserService";
    private static final String CLIENT_CHARACTERISTIC_CONFIG     = "00002902-0000-1000-8000-00805f9b34fb";

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void initialize(OpenSpatialSingleUserServiceCallback cb) throws OpenSpatialException {
        mBluetoothManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            throw new OpenSpatialException(
                    OpenSpatialException.ErrorCode.BLUETOOTH_NOT_SUPPORTED,
                    "Bluetooth not supported");
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            throw new OpenSpatialException(OpenSpatialException.ErrorCode.BLUETOOTH_OFF, "Bluetooth turned off");
        }

        mCallback = cb;
    }

    private void setGattForDevice(BluetoothDevice device, BluetoothGatt gatt) {
        synchronized (mDeviceGattMap) {
            mDeviceGattMap.put(device, gatt);
        }
    }

    private BluetoothGatt getGattForDevice(BluetoothDevice device) {
        synchronized (mDeviceGattMap) {
            return mDeviceGattMap.get(device);
        }
    }

    private void removeGattForDevice(BluetoothDevice device) {
        synchronized (mDeviceGattMap) {
            mDeviceGattMap.remove(device);
        }
    }

    public void connectToDevice(BluetoothDevice device, boolean autoconnect) {
        BluetoothGatt gatt = device.connectGatt(this, autoconnect, mGattCallback);
        setGattForDevice(device, gatt);
    }

    public void disconnectFromDevice(BluetoothDevice device) {
        BluetoothGatt gatt = getGattForDevice(device);
        removeGattForDevice(device);

        gatt.disconnect();
        gatt.close();
    }

    // Must *ONLY* be called when map is synchronized
    private int registerCallback(HashMap<BluetoothDevice, OpenSpatialEvent.EventListener> map,
                                  BluetoothDevice device,
                                  OpenSpatialEvent.EventListener listener,
                                  String uuid) throws OpenSpatialException {
        if (map.get(device) != null) {
            throw new OpenSpatialException(OpenSpatialException.ErrorCode.DEVICE_ALREADY_REGISTERED,
                    "Bluetooth device " + device.getName() + " (" + device.getAddress() + ") already registered");
        }

        int status = enableNotificationsForCharacteristic(device, uuid, true);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            map.put(device, listener);
        }

        return status;
    }

    // Must *ONLY* be called when map is synchronized
    private void unRegisterCallback(HashMap<BluetoothDevice, OpenSpatialEvent.EventListener> map,
                                    BluetoothDevice device,
                                    String uuid) throws OpenSpatialException {
        if (map.get(device) == null) {
            throw new OpenSpatialException(OpenSpatialException.ErrorCode.DEVICE_NOT_REGISTERED,
                    "Bluetooth device " + device.getName() + " (" + device.getAddress() + ") is not registered");
        }

        map.remove(device);

        enableNotificationsForCharacteristic(device, uuid, false);
    }

    private int enableNotificationsForCharacteristic(BluetoothDevice device, String uuid, boolean enable) {
        BluetoothGatt gatt = getGattForDevice(device);
        BluetoothGattService service = gatt.getService(UUID.fromString(OpenSpatialConstants.OPENSPATIAL_SERVICE_UUID));
        if (service == null) {
            Log.e(TAG, "OpenSpatial service not found");
            return BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED;
        }

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(uuid));
        if (characteristic == null) {
            Log.e(TAG, "Characteristic " + uuid + " not found");
            return BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED;
        }

        return enableNotificationsForCharacteristic(characteristic, gatt, enable);
    }

    private int enableNotificationsForCharacteristic(BluetoothGattCharacteristic characteristic,
                                                     BluetoothGatt gatt,
                                                     boolean enable) {
        boolean ret = gatt.setCharacteristicNotification(characteristic, enable);
        if (!ret) {
            Log.e(TAG, "Error setting notification on characteristic");
            return BluetoothGatt.GATT_FAILURE;
        } else {
            Log.d(TAG, "notifications enabled");
        }

        // Write CCC to enable notifications
        byte[] value;

        if (enable) {
            value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
        } else {
            value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
        }

        BluetoothGattDescriptor descriptor =
                characteristic.getDescriptor(
                        UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
        if (descriptor == null) {
            Log.e(TAG, "Error getting CLIENT_CHARACTERISTIC_CONFIG descriptor. Notifications NOT enabled");
            return BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED;
        }

        descriptor.setValue(value);
        ret = gatt.writeDescriptor(descriptor);
        if (!ret) {
            Log.e(TAG, "Error writing descriptor. Notifications NOT enabled!");
        } else {
            Log.d(TAG, "Descriptor written successfully");
        }

        return BluetoothGatt.GATT_SUCCESS;
    }

    /**
     * Register for {@link net.openspatial.ButtonEvent}s from the specified {@code device}
     * @param device The device to listen for {@code ButtonEvent}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">}
     *                   BluetoothDevice</a>}. Use null if you're using the emulator service.
     * @param listener An instance of {@link net.openspatial.OpenSpatialEvent.EventListener}. When an
     *                 {@link net.openspatial.OpenSpatialEvent} is received, the {@code onEventReceived} method is
     *                 called
     */
    public void registerForButtonEvents(BluetoothDevice device, OpenSpatialEvent.EventListener listener)
            throws OpenSpatialException {
        int status;
        synchronized (mButtonEventCallbacks) {
            status = registerCallback(mButtonEventCallbacks,
                    device,
                    listener,
                    OpenSpatialConstants.OPENSPATIAL_BUTTONSTATE_CHARACTERISTIC);
        }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            mCallback.buttonEventRegistrationResult(device, status);
        }
    }

    /**
     * Register for {@link net.openspatial.PointerEvent}s from the specified {@code device}
     * @param device The device to listen for {@code PointerEvent}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">}
     *                   BluetoothDevice</a>}. Use null if you're using the emulator service.
     * @param listener An instance of {@link net.openspatial.OpenSpatialEvent.EventListener}. When an
     *                 {@link net.openspatial.OpenSpatialEvent} is received, the {@code onEventReceived} method is
     *                 called
     */
    public void registerForPointerEvents(BluetoothDevice device, OpenSpatialEvent.EventListener listener)
            throws OpenSpatialException {
        int status;
        synchronized (mPointerEventCallbacks) {
            status = registerCallback(mPointerEventCallbacks,
                    device,
                    listener,
                    OpenSpatialConstants.OPENSPATIAL_POSITION_2D_CHARACTERISTIC);
        }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            mCallback.pointerEventRegistrationResult(device, status);
        }
    }

    /**
     * Register for {@link net.openspatial.Pose6DEvent}s from the specified {@code device}
     * @param device The device to listen for {@code Pose6DEvent}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">}
     *                   BluetoothDevice</a>}. Use null if you're using the emulator service.
     * @param listener An instance of {@link net.openspatial.OpenSpatialEvent.EventListener}. When an
     *                 {@link net.openspatial.OpenSpatialEvent} is received, the {@code onEventReceived} method is
     *                 called
     */
    public void registerForPose6DEvents(BluetoothDevice device, OpenSpatialEvent.EventListener listener)
            throws OpenSpatialException {
        int status;
        synchronized (m3DRotationEventCallbacks) {
            status = registerCallback(m3DRotationEventCallbacks,
                    device,
                    listener,
                    OpenSpatialConstants.OPENSPATIAL_POSE_6D_CHARACTERISTIC);
        }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            mCallback.pose6DEventRegistrationResult(device, status);
        }
    }

    /**
     * Register for {@link net.openspatial.GestureEvent}s for the given device
     * @param device The device to listen for {@code GestureEvent}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">}
     * @param listener An instance of {@link net.openspatial.OpenSpatialEvent.EventListener}. When an
     *                 {@link net.openspatial.OpenSpatialEvent} is received, the {@code onEventReceived} method is
     *                 called
     */
    public void registerForGestureEvents(BluetoothDevice device,
                                         OpenSpatialEvent.EventListener listener) throws OpenSpatialException {
        int status;
        synchronized (mGestureEventCallbacks) {
            status = registerCallback(mGestureEventCallbacks,
                    device,
                    listener,
                    OpenSpatialConstants.OPENSPATIAL_GESTURE_CHARACTERISTIC);
        }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            mCallback.gestureEventRegistrationResult(device, status);
        }
    }

    /**
     * Register for {@link net.openspatial.Motion6DEvent}s from the specified {@code device}
     * @param device The device to listen for {@code Motion6DEvent}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">}
     *                   BluetoothDevice</a>}. Use null if you're using the emulator service.
     * @param listener An instance of {@link net.openspatial.OpenSpatialEvent.EventListener}. When an
     *                 {@link net.openspatial.OpenSpatialEvent} is received, the {@code onEventReceived} method is
     *                 called
     */
    public void registerForMotion6DEvents(BluetoothDevice device, OpenSpatialEvent.EventListener listener)
            throws OpenSpatialException {
        int status;
        synchronized (mMotion6DEventCallbacks) {
            status = registerCallback(mMotion6DEventCallbacks,
                    device,
                    listener,
                    OpenSpatialConstants.OPENSPATIAL_MOTION6D_CHARACTERISTIC);
        }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            mCallback.motion6DEventRegistrationResult(device, status);
        }
    }

    /**
     * Unregister for {@link net.openspatial.ButtonEvent}s from the specified {@code device}
     * @param device The device to stop listening for {@code ButtonEvent}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">
     *                   BluetoothDevice</a>}. Use null if you're using the emulator service.
     */
    public void unRegisterForButtonEvents(BluetoothDevice device) throws OpenSpatialException {
        synchronized (mButtonEventCallbacks) {
            unRegisterCallback(mButtonEventCallbacks,
                    device,
                    OpenSpatialConstants.OPENSPATIAL_BUTTONSTATE_CHARACTERISTIC);
        }
    }

    /**
     * Unregister for {@link net.openspatial.PointerEvent}s from the specified {@code device}
     * @param device The device to stop listening for {@code PointerEvent}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">
     *                   BluetoothDevice</a>}. Use null if you're using the emulator service.
     */
    public void unRegisterForPointerEvents(BluetoothDevice device) throws OpenSpatialException {
        synchronized (mPointerEventCallbacks) {
            unRegisterCallback(mPointerEventCallbacks,
                    device,
                    OpenSpatialConstants.OPENSPATIAL_POSITION_2D_CHARACTERISTIC);
        }
    }

    /**
     * Unregister for {@link net.openspatial.Pose6DEvent}s from the specified {@code device}
     * @param device The device to stop listening for {@code Pose6DEvents}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">
     *                   BluetoothDevice</a>}. Use null if you're using the emulator service.
     */
    public void unRegisterForPose6DEvents(BluetoothDevice device) throws OpenSpatialException {
        synchronized (m3DRotationEventCallbacks) {
            unRegisterCallback(m3DRotationEventCallbacks,
                    device,
                    OpenSpatialConstants.OPENSPATIAL_POSE_6D_CHARACTERISTIC);
        }
    }

    /**
     * Unregister for {@link net.openspatial.GestureEvent}s from the specified {@code device}
     * @param device The device to stop listening for {@code GestureEvent}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">
     *                   BluetoothDevice</a>}. Use null if you're using the emulator service.
     * @throws OpenSpatialException
     */
    public void unRegisterForGestureEvents(BluetoothDevice device)
            throws OpenSpatialException {
        synchronized (mGestureEventCallbacks) {
            unRegisterCallback(mGestureEventCallbacks,
                    device,
                    OpenSpatialConstants.OPENSPATIAL_GESTURE_CHARACTERISTIC);
        }
    }

    /**
     * Unregister for {@link net.openspatial.Motion6DEvent}s from the specified {@code device}
     * @param device The device to stop listening for {@code Motion6DEvents}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">
     *                   BluetoothDevice</a>}. Use null if you're using the emulator service.
     */
    public void unRegisterForMotion6DEvents(BluetoothDevice device) throws OpenSpatialException {
        synchronized (mMotion6DEventCallbacks) {
            unRegisterCallback(mMotion6DEventCallbacks,
                    device,
                    OpenSpatialConstants.OPENSPATIAL_MOTION6D_CHARACTERISTIC);
        }
    }

    public interface OpenSpatialSingleUserServiceCallback {
        public void deviceConnected(BluetoothDevice device);
        public void deviceConnectFailed(BluetoothDevice device, int status);
        public void deviceDisconnected(BluetoothDevice device);
        public void buttonEventRegistrationResult(BluetoothDevice device, int status);
        public void pointerEventRegistrationResult(BluetoothDevice device, int status);
        public void pose6DEventRegistrationResult(BluetoothDevice device, int status);
        public void gestureEventRegistrationResult(BluetoothDevice device, int status);
        public void motion6DEventRegistrationResult(BluetoothDevice device, int status);
    }
}
