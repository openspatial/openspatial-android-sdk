/*
 * Copyright (C) 2014 Nod Labs
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
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.*;

/**
 * This service provides clients with OpenSpatialEvents that they are interested in. Clients bind with this service and
 * call one of the {@code register_*()} functions to register with the kind of events they are interested in and on which
 * device. Once an event is received from the specified device the service calls the
 * {@link net.openspatial.OpenSpatialEvent.EventListener#onEventReceived(OpenSpatialEvent)} of the listener specified in the
 * {@code register_*()} function.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class OpenSpatialService extends Service {
    // BroadcastReceiver to receive intents from the emulator service/BLE service
    private final BroadcastReceiver mEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null) {
                Log.e(TAG, "Got null action");
                return;
            }

            if (action.equals(OpenSpatialConstants.OPENSPATIAL_DEVICE_CONNECTED_INTENT_ACTION) ||
                    action.equals(OpenSpatialConstants.OPENSPATIAL_DEVICE_DISCONNECTED_INTENT_ACTION)) {
                Log.d(TAG, "Got device connected event");
                processDeviceConnectionIntent(intent);
            } else {
                processEventIntent(intent);
            }
        }
    };

    private final BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mServiceCallback == null) {
                Log.e(TAG, "No callback interface registered");
                return;
            }

            String action = intent.getAction();
            if (action == null) {
                Log.e(TAG, "Got null action");
                return;
            }

            String identifier = intent.getStringExtra(OpenSpatialConstants.IDENTIFIER);
            if (identifier == null || !identifier.equals(mIdentifier)) {
                return;
            }

            BluetoothDevice device = intent.getParcelableExtra(OpenSpatialConstants.BLUETOOTH_DEVICE);
            if (device == null) {
                Log.e(TAG, "Device not set in result");
                return;
            }

            int status = intent.getIntExtra(OpenSpatialConstants.STATUS, -1);
            if (status == -1) {
                Log.e(TAG, "Status not set in result");
                return;
            }

            if (action.equals(OpenSpatialConstants.OPENSPATIAL_REGISTER_BUTTON_EVENT_RESULT)) {
                mServiceCallback.buttonEventRegistrationResult(device, status);
            } else if (action.equals(OpenSpatialConstants.OPENSPATIAL_REGISTER_POINTER_EVENT_RESULT)) {
                mServiceCallback.pointerEventRegistrationResult(device, status);
            } else if (action.equals(OpenSpatialConstants.OPENSPATIAL_REGISTER_GESTURE_EVENT_RESULT)) {
                mServiceCallback.gestureEventRegistrationResult(device, status);
            } else if (action.equals(OpenSpatialConstants.OPENSPATIAL_REGISTER_POSE6D_EVENT_RESULT)) {
                mServiceCallback.pose6DEventRegistrationResult(device, status);
            } else if (action.equals(OpenSpatialConstants.OPENSPATIAL_REGISTER_MOTION6D_EVENT_RESULT)) {
                mServiceCallback.motion6DEventRegistrationResult(device, status);
            } else {
                Log.e(TAG, "Got unknown action: " + action);
            }
        }
    };

    private final OpenSpatialServiceBinder mBinder = new OpenSpatialServiceBinder();

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

    private String mIdentifier;
    private OpenSpatialServiceCallback mServiceCallback;

    private static final String TAG = OpenSpatialService.class.getSimpleName();

    // Must *ONLY* be called when map is synchronized
    private void registerCallback(HashMap<BluetoothDevice, OpenSpatialEvent.EventListener> map,
                                  BluetoothDevice device,
                                  OpenSpatialEvent.EventListener listener) throws OpenSpatialException {
        if (map.get(device) != null) {
            throw new OpenSpatialException(OpenSpatialException.ErrorCode.DEVICE_ALREADY_REGISTERED,
                    "Bluetooth device " + device.getName() + " (" + device.getAddress() + ") already registered");
        }

        map.put(device, listener);
    }

    private void sendIntent(BluetoothDevice device, String action) {
        Intent intent = new Intent(action);
        intent.putExtra(OpenSpatialConstants.BLUETOOTH_DEVICE, device);
        intent.putExtra(OpenSpatialConstants.IDENTIFIER, mIdentifier);

        sendBroadcast(intent);
    }

    // Must *ONLY* be called when map is synchronized
    private void unRegisterCallback(HashMap<BluetoothDevice, OpenSpatialEvent.EventListener> map,
                                    BluetoothDevice device) throws OpenSpatialException {
        if (map.get(device) == null) {
            throw new OpenSpatialException(OpenSpatialException.ErrorCode.DEVICE_NOT_REGISTERED,
                    "Bluetooth device " + device.getName() + " (" + device.getAddress() + ") is not registered");
        }

        map.remove(device);
    }

    /**
     * Initialize the library
     *
     * This call must be called before any other call is made
     *
     * @param identifier A unique identifier identifying the entity using this library. This is typically
     *                   an Activity or a Fragment. A typical value for this parameter is the fully qualified
     *                   class name
     * @param cb         An instance of {@code OpenSpatialServiceCallback}. Any interesting events, such as
     *                   new devices being connected, registration results etc. will be communicated via the
     *                   respective methods in this class.
     */
    public void initialize(String identifier, OpenSpatialServiceCallback cb) {
        mIdentifier = identifier;
        mServiceCallback = cb;

        // Register for registration event results
        IntentFilter filter = new IntentFilter();
        filter.addAction(OpenSpatialConstants.OPENSPATIAL_REGISTER_BUTTON_EVENT_RESULT);
        filter.addAction(OpenSpatialConstants.OPENSPATIAL_REGISTER_GESTURE_EVENT_RESULT);
        filter.addAction(OpenSpatialConstants.OPENSPATIAL_REGISTER_POINTER_EVENT_RESULT);
        filter.addAction(OpenSpatialConstants.OPENSPATIAL_REGISTER_POSE6D_EVENT_RESULT);
        filter.addAction(OpenSpatialConstants.OPENSPATIAL_REGISTER_MOTION6D_EVENT_RESULT);
        registerReceiver(mResultReceiver, filter);

        IntentFilter connectedDevicesfilter = new IntentFilter();
        connectedDevicesfilter.addAction(OpenSpatialConstants.OPENSPATIAL_DEVICE_CONNECTED_INTENT_ACTION);
        connectedDevicesfilter.addAction(OpenSpatialConstants.OPENSPATIAL_DEVICE_DISCONNECTED_INTENT_ACTION);
        registerReceiver(mEventReceiver, connectedDevicesfilter);

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
        synchronized (mButtonEventCallbacks) {
            registerCallback(mButtonEventCallbacks, device, listener);
        }

        IntentFilter filter = new IntentFilter(OpenSpatialConstants.OPENSPATIAL_BUTTON_EVENT_INTENT_ACTION);
        registerReceiver(mEventReceiver, filter);

        sendIntent(device, OpenSpatialConstants.OPENSPATIAL_REGISTER_BUTTON_EVENT_INTENT_ACTION);
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
        synchronized (mPointerEventCallbacks) {
            registerCallback(mPointerEventCallbacks, device, listener);
        }

        IntentFilter filter = new IntentFilter(OpenSpatialConstants.OPENSPATIAL_POINTER_EVENT_INTENT_ACTION);
        registerReceiver(mEventReceiver, filter);

        sendIntent(device, OpenSpatialConstants.OPENSPATIAL_REGISTER_POINTER_EVENT_INTENT_ACTION);
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
        synchronized (m3DRotationEventCallbacks) {
            registerCallback(m3DRotationEventCallbacks, device, listener);
        }

        IntentFilter filter = new IntentFilter(OpenSpatialConstants.OPENSPATIAL_POSE6D_EVENT_INTENT_ACTION);
        registerReceiver(mEventReceiver, filter);

        sendIntent(device, OpenSpatialConstants.OPENSPATIAL_REGISTER_POSE6D_EVENT_INTENT_ACTION);
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
                OpenSpatialEvent.EventListener listener)
            throws OpenSpatialException {
        Map<GestureEvent.GestureEventType, OpenSpatialEvent.EventListener> listenerMap;

        synchronized (mGestureEventCallbacks) {
            registerCallback(mGestureEventCallbacks, device, listener);
        }

        IntentFilter filter = new IntentFilter(OpenSpatialConstants.OPENSPATIAL_GESTURE_EVENT_INTENT_ACTION);
        registerReceiver(mEventReceiver, filter);

        sendIntent(device, OpenSpatialConstants.OPENSPATIAL_REGISTER_GESTURE_EVENT_INTENT_ACTION);
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
        synchronized (mMotion6DEventCallbacks) {
            registerCallback(mMotion6DEventCallbacks, device, listener);
        }

        IntentFilter filter = new IntentFilter(OpenSpatialConstants.OPENSPATIAL_MOTION6D_EVENT_INTENT_ACTION);
        registerReceiver(mEventReceiver, filter);

        sendIntent(device, OpenSpatialConstants.OPENSPATIAL_REGISTER_MOTION6D_EVENT_INTENT_ACTION);
    }

    /**
     * Unregister for {@link net.openspatial.ButtonEvent}s from the specified {@code device}
     * @param device The device to stop listening for {@code ButtonEvent}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">
     *                   BluetoothDevice</a>}. Use null if you're using the emulator service.
     */
    public void unRegisterForButtonEvents(BluetoothDevice device) throws OpenSpatialException {
        sendIntent(device, OpenSpatialConstants.OPENSPATIAL_UNREGISTER_BUTTON_EVENT_INTENT_ACTION);

        synchronized (mButtonEventCallbacks) {
            unRegisterCallback(mButtonEventCallbacks, device);
        }
    }

    /**
     * Unregister for {@link net.openspatial.PointerEvent}s from the specified {@code device}
     * @param device The device to stop listening for {@code PointerEvent}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">
     *                   BluetoothDevice</a>}. Use null if you're using the emulator service.
     */
    public void unRegisterForPointerEvents(BluetoothDevice device) throws OpenSpatialException {
        sendIntent(device, OpenSpatialConstants.OPENSPATIAL_UNREGISTER_POINTER_EVENT_INTENT_ACTION);

        synchronized (mPointerEventCallbacks) {
            unRegisterCallback(mPointerEventCallbacks, device);
        }
    }

    /**
     * Unregister for {@link net.openspatial.Pose6DEvent}s from the specified {@code device}
     * @param device The device to stop listening for {@code Pose6DEvents}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">
     *                   BluetoothDevice</a>}. Use null if you're using the emulator service.
     */
    public void unRegisterForPose6DEvents(BluetoothDevice device) throws OpenSpatialException {
        sendIntent(device, OpenSpatialConstants.OPENSPATIAL_UNREGISTER_POSE6D_EVENT_INTENT_ACTION);

        synchronized (m3DRotationEventCallbacks) {
            unRegisterCallback(m3DRotationEventCallbacks, device);
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
        sendIntent(device, OpenSpatialConstants.OPENSPATIAL_UNREGISTER_GESTURE_EVENT_INTENT_ACTION);

        synchronized (mGestureEventCallbacks) {
            unRegisterCallback(mGestureEventCallbacks, device);
        }
    }

    /**
     * Unregister for {@link net.openspatial.Motion6DEvent}s from the specified {@code device}
     * @param device The device to stop listening for {@code Motion6DEvents}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">
     *                   BluetoothDevice</a>}. Use null if you're using the emulator service.
     */
    public void unRegisterForMotion6DEvents(BluetoothDevice device) throws OpenSpatialException {
        sendIntent(device, OpenSpatialConstants.OPENSPATIAL_UNREGISTER_MOTION6D_EVENT_INTENT_ACTION);

        synchronized (mMotion6DEventCallbacks) {
            unRegisterCallback(mMotion6DEventCallbacks, device);
        }
    }

    /**
     * Get a list of connected devices
     *
     * This call requests a list of connected devices. The result is communicated via the {@code deviceListUpdated}
     * callback in the {@link net.openspatial.OpenSpatialService.OpenSpatialServiceCallback} instance registered in
     * {@code initialize()}
     */
    public void getConnectedDevices() {
        sendIntent(null, OpenSpatialConstants.OPENSPATIAL_LIST_DEVICES_INTENT_ACTION);
    }

    // Package private because it is used in tests
    void processEventIntent(Intent i) {
        OpenSpatialEvent event = i.getParcelableExtra(OpenSpatialConstants.OPENSPATIAL_EVENT);

        if (event != null) {
            OpenSpatialEvent.EventListener listener = null;
            switch (event.eventType) {
                case EVENT_BUTTON:
                    synchronized (mButtonEventCallbacks) {
                        listener = mButtonEventCallbacks.get(event.device);
                    }
                    break;
                case EVENT_POINTER:
                    synchronized (mPointerEventCallbacks) {
                        listener = mPointerEventCallbacks.get(event.device);
                    }
                    break;
                case EVENT_POSE6D:
                    synchronized (m3DRotationEventCallbacks) {
                        listener = m3DRotationEventCallbacks.get(event.device);
                    }
                    break;
                case EVENT_GESTURE:
                    synchronized (mGestureEventCallbacks) {
                        listener = mGestureEventCallbacks.get(event.device);
                    }
                    break;
                case EVENT_MOTION6D:
                    synchronized (mMotion6DEventCallbacks) {
                        listener = mMotion6DEventCallbacks.get(event.device);
                        break;
                    }
                default:
                    Log.e(TAG, "Unsupported event type: " + event.eventType);
            }

            if (listener == null) {
                Log.e(TAG, "No listener registered for event type " +
                        event.eventType + " on device " + event.device);
            } else {
                listener.onEventReceived(event);
            }
        } else {
            Log.e(TAG, "OpenSpatial event not specified");
        }
    }

    private void processDeviceConnectionIntent(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(OpenSpatialConstants.BLUETOOTH_DEVICE);
        if (device == null) {
            Log.e(TAG, "Got device connected intent with no device");
            return;
        }

        if (intent.getAction().equals(OpenSpatialConstants.OPENSPATIAL_DEVICE_CONNECTED_INTENT_ACTION)) {
            mServiceCallback.deviceConnected(device);
        } else if (intent.getAction().equals(OpenSpatialConstants.OPENSPATIAL_DEVICE_DISCONNECTED_INTENT_ACTION)) {
            mServiceCallback.deviceDisconnected(device);
        }
    }

    public class OpenSpatialServiceBinder extends Binder {
        public OpenSpatialService getService() {
            return OpenSpatialService.this;
        }
    }

    /**
     * Bind to the OpenSpatial service
     * @param intent {@code Intent} specified when calling {@code bindService()}
     * @return {@code Binder} for this service
     *
     * @see <a href="http://developer.android.com/reference/android/app/Service.html">Services</a>
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void cleanup(Set<BluetoothDevice> devices, OpenSpatialEvent.EventType type) {
        for (BluetoothDevice device : devices) {
            try {
                Log.e(TAG, "Leaked " + type + " registration for " + device.getName());

                switch (type) {
                    case EVENT_POINTER:
                        unRegisterForPointerEvents(device);
                        break;
                    case EVENT_BUTTON:
                        unRegisterForButtonEvents(device);
                        break;
                    case EVENT_GESTURE:
                        unRegisterForGestureEvents(device);
                        break;
                    case EVENT_POSE6D:
                        unRegisterForPose6DEvents(device);
                        break;
                    case EVENT_MOTION6D:
                        unRegisterForMotion6DEvents(device);
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to unregister from PointerEvents for " +
                        device.getName() + " with error: " + e.getMessage());
            }
        }
    }

    private void cleanup() {
        Set<BluetoothDevice> devices;
        synchronized (mPointerEventCallbacks) {
            devices = new HashSet<BluetoothDevice>(mPointerEventCallbacks.keySet());
        }
        cleanup(devices, OpenSpatialEvent.EventType.EVENT_POINTER);

        synchronized (mButtonEventCallbacks) {
            devices = new HashSet<BluetoothDevice>(mButtonEventCallbacks.keySet());
        }
        cleanup(devices, OpenSpatialEvent.EventType.EVENT_BUTTON);

        synchronized (mGestureEventCallbacks) {
            devices = new HashSet<BluetoothDevice>(mGestureEventCallbacks.keySet());
        }
        cleanup(devices, OpenSpatialEvent.EventType.EVENT_GESTURE);

        synchronized (m3DRotationEventCallbacks) {
            devices = new HashSet<BluetoothDevice>(m3DRotationEventCallbacks.keySet());
        }
        cleanup(devices, OpenSpatialEvent.EventType.EVENT_POSE6D);

        synchronized (mMotion6DEventCallbacks) {
            devices = new HashSet<BluetoothDevice>(mMotion6DEventCallbacks.keySet());
        }
        cleanup(devices, OpenSpatialEvent.EventType.EVENT_MOTION6D);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mEventReceiver);
        unregisterReceiver(mResultReceiver);

        // Cleanup any missing unregisters
        cleanup();
    }

    public interface OpenSpatialServiceCallback {
        public void deviceConnected(BluetoothDevice device);
        public void deviceDisconnected(BluetoothDevice device);
        public void buttonEventRegistrationResult(BluetoothDevice device, int status);
        public void pointerEventRegistrationResult(BluetoothDevice device, int status);
        public void pose6DEventRegistrationResult(BluetoothDevice device, int status);
        public void gestureEventRegistrationResult(BluetoothDevice device, int status);
        public void motion6DEventRegistrationResult(BluetoothDevice device, int status);
    }
}
