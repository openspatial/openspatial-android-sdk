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
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.*;

/**
 * This service provides clients with OpenSpatialEvents that they are interested in. Clients bind
 * with this service and call
 * {@link #registerForData(BluetoothDevice, HashSet, OpenSpatialDataListener)}.
 * Once data is received from the specified device the service calls the
 * {@link net.openspatial.OpenSpatialDataListener#onDataReceived(OpenSpatialData)} of the listener
 * specified in the {@link #registerForData(BluetoothDevice, HashSet, OpenSpatialDataListener)}
 * function.
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
            } else if (action.equals(
                    OpenSpatialConstants.OPENSPATIAL_DEVICE_INFO_INTENT_ACTION)) {
                processDeviceInfoReceipt(intent);
            } else if (action.equals(OpenSpatialConstants.OPENSPATIAL_DATA_INTENT_ACTION)) {
                processInboundData(intent);
            } else {
                processEventIntent(intent);
            }
        }
    };

    private final BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mServiceCallback == null && mServiceInterface == null) {
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

            BluetoothDevice device =
                    intent.getParcelableExtra(OpenSpatialConstants.BLUETOOTH_DEVICE);
            if (device == null) {
                Log.e(TAG, "Device not set in result");
                return;
            }

            int status = intent.getIntExtra(OpenSpatialConstants.STATUS, -1);
            if (status == -1) {
                Log.e(TAG, "Status not set in result");
                return;
            }

            OpenSpatialEvent.EventType eventType =
                    (OpenSpatialEvent.EventType) intent.getSerializableExtra(
                            OpenSpatialConstants.EVENT_TYPE);

            if (action.equals(
                    OpenSpatialConstants.OPENSPATIAL_REGISTRATION_CHANGE_ATTEMPT_EVENT_RESULT)) {
                if (mServiceCallback != null) {
                    mServiceCallback.eventRegistrationResult(device, eventType, status);
                }
                if (mServiceInterface != null) {
                    mServiceInterface.registrationResult(device, status);
                }
            } else {
                Log.e(TAG, "Got unknown action: " + action);
            }
        }
    };

    private final OpenSpatialServiceBinder mBinder = new OpenSpatialServiceBinder();

    private final HashMap<BluetoothDevice, EventCallbacks> mEventCallbacks =
            new HashMap<BluetoothDevice, EventCallbacks>();

    private String mIdentifier;
    private OpenSpatialServiceCallback mServiceCallback;

    private OpenSpatialInterface mServiceInterface;

    private static final String TAG = OpenSpatialService.class.getSimpleName();

    @Deprecated
    private void registerCallback(HashMap<BluetoothDevice, EventCallbacks> map,
                                  BluetoothDevice device,
                                  OpenSpatialEvent.EventType eventType,
                                  OpenSpatialEvent.EventListener listener) throws OpenSpatialException {
        EventCallbacks callbacks = map.get(device);

        if (device != null && callbacks == null) {
            callbacks = new EventCallbacks();
            map.put(device, callbacks);
        }

        if (callbacks.getCallback(eventType) != null) {
            throw new OpenSpatialException(OpenSpatialException.ErrorCode.DEVICE_ALREADY_REGISTERED,
                    "Bluetooth device " + device.getName() + " (" + device.getAddress()
                            + ") already registered");
        }

        callbacks.setCallback(eventType, listener);
    }

    private void registerCallback(HashMap<BluetoothDevice, EventCallbacks> map,
                                  BluetoothDevice device,
                                  OpenSpatialDataListener listener) throws OpenSpatialException {
        EventCallbacks callbacks = map.get(device);

        if (device != null && callbacks == null) {
            callbacks = new EventCallbacks();
            map.put(device, callbacks);
        }

        if (callbacks.getCallback() != null) {
            throw new OpenSpatialException(OpenSpatialException.ErrorCode.DEVICE_ALREADY_REGISTERED,
                    "Bluetooth device " + device.getName() + " (" + device.getAddress()
                            + ") already registered");
        }

        callbacks.setCallback(listener);
    }

    private void sendIntent(BluetoothDevice device, String action,
                            HashSet<DataType> types, boolean setRegistered) {
        Intent intent = new Intent(action);
        intent.putExtra(OpenSpatialConstants.BLUETOOTH_DEVICE, device);
        intent.putExtra(OpenSpatialConstants.IDENTIFIER, mIdentifier);
        intent.putExtra(OpenSpatialConstants.DATA_TYPES, types);
        intent.putExtra(OpenSpatialConstants.SET_REGISTRATION_STATUS, setRegistered);

        sendBroadcast(intent);
    }

    private void sendIntent(BluetoothDevice device, String action) {
        Intent intent = new Intent(action);
        intent.putExtra(OpenSpatialConstants.BLUETOOTH_DEVICE, device);
        intent.putExtra(OpenSpatialConstants.IDENTIFIER, mIdentifier);

        sendBroadcast(intent);
    }

    private void sendIntent(BluetoothDevice device, String action, String infoType) {
        Intent intent = new Intent(action);
        intent.putExtra(OpenSpatialConstants.BLUETOOTH_DEVICE, device);
        intent.putExtra(OpenSpatialConstants.IDENTIFIER, mIdentifier);
        intent.putExtra(OpenSpatialConstants.INFO_TYPE, infoType);

        sendBroadcast(intent);
    }

    @Deprecated
    private void sendIntent(BluetoothDevice device, String action,
                            OpenSpatialEvent.EventType eventType, String extendedEventCategory,
                            boolean setRegistered) {
        Intent intent = new Intent(action);
        intent.putExtra(OpenSpatialConstants.BLUETOOTH_DEVICE, device);
        intent.putExtra(OpenSpatialConstants.IDENTIFIER, mIdentifier);
        intent.putExtra(OpenSpatialConstants.EVENT_TYPE, eventType);
        intent.putExtra(OpenSpatialConstants.EXTENDED_EVENT_CATEGORY, extendedEventCategory);
        intent.putExtra(OpenSpatialConstants.EVENT_UUID,
                OpenSpatialEvent.EVENT_UUID_MAP.get(eventType));
        intent.putExtra(OpenSpatialConstants.SET_REGISTRATION_STATUS, setRegistered);

        sendBroadcast(intent);
    }

    // Must *ONLY* be called when map is synchronized
    @Deprecated
    private void unRegisterCallback(HashMap<BluetoothDevice, EventCallbacks> map,
                                    BluetoothDevice device,
                                    OpenSpatialEvent.EventType eventType)
            throws OpenSpatialException {
        EventCallbacks callbacks = map.get(device);
        if (callbacks == null || callbacks.getCallback(eventType) == null) {
            throw new OpenSpatialException(OpenSpatialException.ErrorCode.DEVICE_NOT_REGISTERED,
                    "Bluetooth device " + device.getName() + " (" + device.getAddress() +
                            ") is not registered");
        }

        callbacks.setCallback(eventType, null);
    }

    private void unRegisterCallback(HashMap<BluetoothDevice,
            EventCallbacks> map, BluetoothDevice device) throws OpenSpatialException {
        EventCallbacks callbacks = map.get(device);
        if (callbacks == null || callbacks.getCallback() == null) {
            throw new OpenSpatialException(OpenSpatialException.ErrorCode.DEVICE_NOT_REGISTERED,
                    "Bluetooth device " + device.getName() + " (" + device.getAddress() +
                            ") is not registered");
        }

        callbacks.setCallback(null);
    }

    /**
     * Initialize the library
     *
     * This call must be called before any other call is made.
     *
     * @param identifier A unique identifier identifying the entity using this library. This is typically
     *                   an Activity or a Fragment. A typical value for this parameter is the fully qualified
     *                   class name.
     * @param cb         An instance of {@code OpenSpatialServiceCallback}. Any interesting events, such as
     *                   new devices being connected, registration results etc. will be communicated via the
     *                   respective methods in this class.
     *
     * @deprecated use {@link #initialize(String, OpenSpatialInterface)} instead.
     */
    @Deprecated
    public void initialize(String identifier, OpenSpatialServiceCallback cb) {
        mIdentifier = identifier;
        mServiceCallback = cb;

        setupFilters();
    }

    /**
     * Initialize the library
     *
     * This call must be called before any other call is made
     *
     * @param identifier A unique identifier identifying the entity using this library. This is
     *                   typically an Activity or a Fragment. A typical value for this parameter
     *                   is the fully qualified class name.
     * @param openSpatialInterface The means by with the service will communicate with the client.
     */
    public void initialize(String identifier, OpenSpatialInterface openSpatialInterface) {
        mIdentifier = identifier;
        mServiceInterface = openSpatialInterface;

        setupFilters();
    }

    private void setupFilters() {
        // Register for registration event results
        IntentFilter filter = new IntentFilter();
        filter.addAction(OpenSpatialConstants.OPENSPATIAL_REGISTRATION_CHANGE_ATTEMPT_EVENT_RESULT);
        registerReceiver(mResultReceiver, filter);

        IntentFilter connectedDevicesfilter = new IntentFilter();
        connectedDevicesfilter.addAction(
                OpenSpatialConstants.OPENSPATIAL_DEVICE_CONNECTED_INTENT_ACTION);
        connectedDevicesfilter.addAction(
                OpenSpatialConstants.OPENSPATIAL_DEVICE_DISCONNECTED_INTENT_ACTION);
        connectedDevicesfilter.addAction(
                OpenSpatialConstants.OPENSPATIAL_EVENT_INTENT_ACTION);
        connectedDevicesfilter.addAction(
                OpenSpatialConstants.OPENSPATIAL_DATA_INTENT_ACTION);
        connectedDevicesfilter.addAction(
                OpenSpatialConstants.OPENSPATIAL_DEVICE_INFO_INTENT_ACTION);
        registerReceiver(mEventReceiver, connectedDevicesfilter);
    }

    /**
     * Register for {@link net.openspatial.OpenSpatialData} from the specified {@code device}.
     * @param device The device to listen for {@code OpenSpatialData} from. This is an instance of
     *               {@link BluetoothDevice}.
     * @param dataTypes The Set of {@link net.openspatial.DataType}s you want to receive.
     * @param listener An instance of {@link net.openspatial.OpenSpatialDataListener}. When an
     *                 {@link OpenSpatialData} is received, the {@code onDataReceived} method is
     *                 called.
     * @throws OpenSpatialException Thrown when a device is already registered for data.
     */
    public void registerForData(BluetoothDevice device,
                                  HashSet<DataType> dataTypes,
                                  OpenSpatialDataListener listener) throws OpenSpatialException {
        synchronized (mEventCallbacks) {
            registerCallback(mEventCallbacks, device, listener);
        }

        sendIntent(device, OpenSpatialConstants.OPENSPATIAL_CHANGE_REGISTRATION_STATE_INTENT_ACTION,
                dataTypes, true);
    }

    /**
     * Register for {@link net.openspatial.OpenSpatialEvent}s from the specified {@code device}
     * @param device The device to listen for {@code OpenSpatialEvent}s from. This is an instance of
     *               {@link BluetoothDevice}. Use null if you're using the emulator service.
     * @param eventType The OpenSpatial event type that you are interested in receiving
     * @param listener An instance of {@link net.openspatial.OpenSpatialEvent.EventListener}. When an
     *                 {@link net.openspatial.OpenSpatialEvent} is received, the {@code onEventReceived} method is
     *                 called
     *
     * @deprecated use {@link #registerForData(BluetoothDevice, HashSet, OpenSpatialDataListener)}
     *             instead.
     */
    @Deprecated
    public void registerForEvents(BluetoothDevice device, OpenSpatialEvent.EventType eventType,
                                        OpenSpatialEvent.EventListener listener)
            throws OpenSpatialException {
        synchronized (mEventCallbacks) {
            registerCallback(mEventCallbacks, device, eventType, listener);
        }

        sendIntent(device, OpenSpatialConstants.OPENSPATIAL_CHANGE_REGISTRATION_STATE_INTENT_ACTION,
                eventType, null, true);
    }

    /**
     * Register for {@link net.openspatial.OpenSpatialEvent}s from the specified {@code device}
     * @param device The device to listen for {@code OpenSpatialEvent}s from. This is an instance of
     *               {@link BluetoothDevice}. Use null if you're using the emulator service.
     * @param eventType The OpenSpatial event type that you are interested in receiving
     * @param listener An instance of {@link net.openspatial.OpenSpatialEvent.EventListener}. When an
     *                 {@link net.openspatial.OpenSpatialEvent} is received, the {@code onEventReceived} method is
     *                 called
     * @deprecated use {@link #registerForData(BluetoothDevice, HashSet, OpenSpatialDataListener)}
     *             instead.
     */
    @Deprecated
    public void registerForEvents(BluetoothDevice device, OpenSpatialEvent.EventType eventType,
                                  String extendedEventCategory,
                                  OpenSpatialEvent.EventListener listener)
            throws OpenSpatialException {
        synchronized (mEventCallbacks) {
            registerCallback(mEventCallbacks, device, eventType, listener);
        }

        sendIntent(device, OpenSpatialConstants.OPENSPATIAL_CHANGE_REGISTRATION_STATE_INTENT_ACTION,
                eventType, extendedEventCategory, true);
    }

    /**
     * Query information from the specified {@code device}.
     * @param device The device to request info for. This is an instance of
     *               {@link BluetoothDevice}.
     * @param infoType The kind of device information you are interested in.
     *
     * @throws OpenSpatialException Thrown when {@code device} is not registered with the
     *         {@code OpenSpatialService}.
     */
    public void queryDeviceInfo(BluetoothDevice device, String infoType)
            throws OpenSpatialException {

        if (!mEventCallbacks.containsKey(device)) {
            throw new OpenSpatialException(OpenSpatialException.ErrorCode.DEVICE_NOT_REGISTERED,
                    "Attempted to query info for a device that is not registered!");
        }
        sendIntent(device, OpenSpatialConstants.OPENSPATIAL_QUERY_DEVICE_INFO_INTENT_ACTION,
                infoType);
    }

    /**
     * Unregister for {@link net.openspatial.OpenSpatialEvent}s from the specified {@code device}
     * @param device The device to stop listening for {@code OpenSpatialEvent}s from.
     *               This is an instance of {@link BluetoothDevice}.
     * @deprecated use {@link #unregisterForData(BluetoothDevice)} instead.
     */
    @Deprecated
    public void unregisterForEvents(BluetoothDevice device, OpenSpatialEvent.EventType eventType)
            throws OpenSpatialException {
        sendIntent(device, OpenSpatialConstants.OPENSPATIAL_CHANGE_REGISTRATION_STATE_INTENT_ACTION,
                eventType, null, false);

        synchronized (mEventCallbacks) {
            unRegisterCallback(mEventCallbacks, device, eventType);
        }
    }

    /**
     * Unregister for {@link OpenSpatialData}s from the specified {@code device}.
     * @param device The device to stop listening for {@code OpenSpatialData} from.
     *               This is an instance of {@link BluetoothDevice}.
     * @throws OpenSpatialException Thrown when {@code device} is not registered.
     */
    public void unregisterForData(BluetoothDevice device)
            throws OpenSpatialException {

        sendIntent(device, OpenSpatialConstants.OPENSPATIAL_CHANGE_REGISTRATION_STATE_INTENT_ACTION,
                null, false);

        synchronized (mEventCallbacks) {
            unRegisterCallback(mEventCallbacks, device);
        }
    }

    /**
     * Unregister for {@link net.openspatial.OpenSpatialEvent}s from the specified {@code device}
     * @param device The device to stop listening for {@code OpenSpatialEvent}s from. This is an instance of
     *               {@link BluetoothDevice}.
     * @deprecated use {@link #unregisterForData(BluetoothDevice)} instead.
     */
    @Deprecated
    public void unregisterForEvents(BluetoothDevice device, OpenSpatialEvent.EventType eventType,
                                    String extendedEventCategory)
            throws OpenSpatialException {
        sendIntent(device, OpenSpatialConstants.OPENSPATIAL_CHANGE_REGISTRATION_STATE_INTENT_ACTION,
                eventType, extendedEventCategory, false);

        synchronized (mEventCallbacks) {
            unRegisterCallback(mEventCallbacks, device, eventType);
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

        if (event != null && mEventCallbacks.get(event.device) != null) {
            OpenSpatialEvent.EventListener listener
                    = mEventCallbacks.get(event.device).getCallback(event.eventType);

            if (listener == null) {
                Log.e(TAG, "No listener registered for event type " +
                        event.eventType + " on device " + event.device);
            } else {
                listener.onEventReceived(event);
            }
        } else {
            Log.e(TAG, "Received an OpenSpatial intent that was not supposed to be sent!");
        }
    }

    void processInboundData(Intent i) {
        OpenSpatialData event = i.getParcelableExtra(OpenSpatialConstants.OPENSPATIAL_EVENT);

        if (event == null) {
            Log.e(TAG, "Got null event!");
            return;
        }

        BluetoothDevice device = event.device;

        if (device == null) {
            Log.e(TAG, "Got null device!");
            return;
        }

        EventCallbacks callbacks = mEventCallbacks.get(device);

        if (callbacks == null) {
            Log.e(TAG, "No callbacks created for device!");
            return;
        }

        OpenSpatialDataListener listener = callbacks.getCallback();

        if (listener == null) {
            Log.e(TAG, "No listener for OpenSpatialData found!");
            return;
        }

        listener.onDataReceived(event);
    }

    private void processDeviceInfoReceipt(Intent i) {
        BluetoothDevice device = i.getParcelableExtra(OpenSpatialConstants.BLUETOOTH_DEVICE);
        String infoType = i.getStringExtra(OpenSpatialConstants.INFO_TYPE);
        Bundle data = i.getExtras();

        if (mServiceCallback != null) {
            mServiceCallback.deviceInfoReceived(device, infoType, data);
        }

        if (mServiceInterface != null) {
            mServiceInterface.deviceInfoReceived(device, infoType, data);
        }
    }

    private void processDeviceConnectionIntent(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(OpenSpatialConstants.BLUETOOTH_DEVICE);
        if (device == null) {
            Log.e(TAG, "Got device connected intent with no device");
            return;
        }

        if (intent.getAction().equals(OpenSpatialConstants.OPENSPATIAL_DEVICE_CONNECTED_INTENT_ACTION)) {
            if (!mEventCallbacks.containsKey(device)) {
                mEventCallbacks.put(device, null);
            }

            if (mServiceCallback != null) {
                mServiceCallback.deviceConnected(device);
            }

            if (mServiceInterface != null) {
                mServiceInterface.deviceConnected(device);
            }
        } else if (intent.getAction().equals(OpenSpatialConstants.OPENSPATIAL_DEVICE_DISCONNECTED_INTENT_ACTION)) {
            if (!mEventCallbacks.containsKey(device)) {
                mEventCallbacks.remove(device);
            }

            if (mServiceCallback != null) {
                mServiceCallback.deviceDisconnected(device);
            }

            if (mServiceInterface != null) {
                mServiceInterface.deviceDisconnected(device);
            }
        }
    }

    public class OpenSpatialServiceBinder extends Binder {
        public OpenSpatialService getService() {
            return OpenSpatialService.this;
        }
    }

    /**
     * Bind to the OpenSpatial service.
     * @param intent {@code Intent} specified when calling {@code bindService()}.
     * @return {@code Binder} for this service.
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
                if(mEventCallbacks.get(device).getCallback(type) != null)
                    Log.e(TAG, "Leaked " + type + " registration for " + device.getName());

                    unregisterForEvents(device, type);
            } catch (Exception e) {
                Log.e(TAG, "Cleanup failed to unregister from " + type.name() + " events for " +
                        device.getName() + " with error: " + e.getMessage());
            }
        }
    }

    private void cleanup() {
        Set<BluetoothDevice> devices;
        synchronized (mEventCallbacks) {
            devices = new HashSet<BluetoothDevice>(mEventCallbacks.keySet());
        }
        for(OpenSpatialEvent.EventType type : OpenSpatialEvent.EventType.values()) {
            cleanup(devices, type);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mEventReceiver);
        unregisterReceiver(mResultReceiver);

        // Cleanup any missing unregisters
        cleanup();
    }

    /**
     * The callbacks used by the service to communicate with clients.
     *
     * @deprecated use {@link OpenSpatialInterface} instead.
     */
    @Deprecated
    public interface OpenSpatialServiceCallback {
        /**
         * After {@link #getConnectedDevices()} is called this will trigger once per available
         * OpenSpatial device.
         * @param device An available OpenSpatial device.
         */
        void deviceConnected(BluetoothDevice device);
        /**
         * Triggers when an OpenSpatial device disconnects.
         * @param device The OpenSpatial device that has disconnected.
         */
        void deviceDisconnected(BluetoothDevice device);

        /**
         * Reports the outcome of a registration attempt.
         * @param device The device that registration was attempted on.
         * @param eventType The event type that registration was attempted on.
         * @param status The outcome of the attempt. Will be one of the values defined within
         *               {@link OpenSpatialErrorCodes}.
         */
        void eventRegistrationResult(BluetoothDevice device,
                                            OpenSpatialEvent.EventType eventType, int status);

        /**
         * Reports the response returned from a call to
         * {@link #queryDeviceInfo(BluetoothDevice, String)}.
         *
         * @param device The OpenSpatialDevice that was queried.
         * @param infoType The type of inquiry.
         * @param infoData The data returned.
         */
        void deviceInfoReceived(BluetoothDevice device, String infoType, Bundle infoData);
    }
}
