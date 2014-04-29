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
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;

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
            processIntent(intent);
        }
    };

    private final OpenSpatialServiceBinder mBinder = new OpenSpatialServiceBinder();

    private final HashMap<BluetoothDevice, OpenSpatialEvent.EventListener> mPointerEventCallbacks =
            new HashMap<BluetoothDevice, OpenSpatialEvent.EventListener>();
    private final HashMap<BluetoothDevice, OpenSpatialEvent.EventListener> mButtonEventCallbacks =
            new HashMap<BluetoothDevice, OpenSpatialEvent.EventListener>();
    private final HashMap<BluetoothDevice, OpenSpatialEvent.EventListener> m3DRotationEventCallbacks =
            new HashMap<BluetoothDevice, OpenSpatialEvent.EventListener>();

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
     * Register for {@link net.openspatial.ButtonEvent}s from the specified {@code device}
     * @param device The device to listen for {@code ButtonEvent}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">
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
    }

    /**
     * Register for {@link net.openspatial.PointerEvent}s from the specified {@code device}
     * @param device The device to listen for {@code PointerEvent}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">
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
    }

    /**
     * Register for {@link net.openspatial.RotationEvent}s from the specified {@code device}
     * @param device The device to listen for {@code RotationEvent}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">
     *                   BluetoothDevice</a>}. Use null if you're using the emulator service.
     * @param listener An instance of {@link net.openspatial.OpenSpatialEvent.EventListener}. When an
     *                 {@link net.openspatial.OpenSpatialEvent} is received, the {@code onEventReceived} method is
     *                 called
     */
    public void registerForRotationEvents(BluetoothDevice device, OpenSpatialEvent.EventListener listener)
        throws OpenSpatialException {
            synchronized (m3DRotationEventCallbacks) {
                registerCallback(m3DRotationEventCallbacks, device, listener);
            }

        IntentFilter filter = new IntentFilter(OpenSpatialConstants.OPENSPATIAL_ROTATION_EVENT_INTENT_ACTION);
        registerReceiver(mEventReceiver, filter);
    }

    /**
     * Unregister for {@link net.openspatial.ButtonEvent}s from the specified {@code device}
     * @param device The device to listen for {@code ButtonEvent}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">
     *                   BluetoothDevice</a>}. Use null if you're using the emulator service.
     */
    public void unRegisterForButtonEvents(BluetoothDevice device) throws OpenSpatialException {
        synchronized (mButtonEventCallbacks) {
            unRegisterCallback(mButtonEventCallbacks, device);
        }
    }

    /**
     * Unregister for {@link net.openspatial.PointerEvent}s from the specified {@code device}
     * @param device The device to listen for {@code PointerEvent}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">
     *                   BluetoothDevice</a>}. Use null if you're using the emulator service.
     */
    public void unRegisterForPointerEvents(BluetoothDevice device) throws OpenSpatialException {
        synchronized (mPointerEventCallbacks) {
            unRegisterCallback(mPointerEventCallbacks, device);
        }
    }

    /**
     * Unregister for {@link net.openspatial.RotationEvent}s from the specified {@code device}
     * @param device The device to listen for {@code RotationEvent}s from. This is an instance of
     *               {@link <a href="http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html">
     *                   BluetoothDevice</a>}. Use null if you're using the emulator service.
     */
    public void unRegisterForRotationEvents(BluetoothDevice device) throws OpenSpatialException {
        synchronized (m3DRotationEventCallbacks) {
            unRegisterCallback(m3DRotationEventCallbacks, device);
        }
    }

    // Package private because it is used in tests
    void processIntent(Intent i) {
        OpenSpatialEvent event = i.getParcelableExtra(OpenSpatialConstants.OPENSPATIAL_EVENT);
        BluetoothDevice device = i.getParcelableExtra(OpenSpatialConstants.BLUETOOTH_DEVICE);

        if (event != null) {
            OpenSpatialEvent.EventListener listener = null;
            switch (event.eventType) {
                case EVENT_BUTTON:
                    synchronized (mButtonEventCallbacks) {
                        listener = mButtonEventCallbacks.get(device);
                    }
                    break;
                case EVENT_POINTER:
                    synchronized (mPointerEventCallbacks) {
                        listener = mPointerEventCallbacks.get(device);
                    }
                    break;
                case EVENT_3D_ROTATION:
                    synchronized (m3DRotationEventCallbacks) {
                        listener = m3DRotationEventCallbacks.get(device);
                    }
                    break;
                default:
                    Log.e(TAG, "Unsupported event type: " + event.eventType);
            }

            if (listener == null) {
                Log.e(TAG, "No listener registered for event type " +
                        event.eventType + " on device " + device);
            } else {
                listener.onEventReceived(event);
            }
        } else {
            Log.e(TAG, "OpenSpatial event not specified");
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mEventReceiver);
    }
}
