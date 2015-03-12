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

package com.example.openspatial;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.nod_labs.pointer.PointerService;
import net.openspatial.*;

import java.util.Set;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainScreenActivity extends Activity implements OpenSpatialService.OpenSpatialServiceCallback {
    OpenSpatialService mOpenSpatialService;
    PointerService mPointerService;

    private static String TAG = MainScreenActivity.class.getSimpleName();
    private static final int RADIUS = 20;
    private static final int mWhiteColor = Color.WHITE;
    private static final int mRedColor = Color.RED;

    private ServiceConnection mOpenSpatialServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mOpenSpatialService = ((OpenSpatialService.OpenSpatialServiceBinder)service).getService();

            mOpenSpatialService.initialize(TAG, MainScreenActivity.this);
            mOpenSpatialService.getConnectedDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mOpenSpatialService = null;
        }
    };

    private ServiceConnection mPointerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPointerService = ((PointerService.PointerServiceBinder)service).getService();
            bindService(new Intent(MainScreenActivity.this,
                    OpenSpatialService.class),
                    mOpenSpatialServiceConnection,
                    BIND_AUTO_CREATE);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPointerService = null;
        }
    };

    private void registerForEvents(final BluetoothDevice device) {
        try {
            mOpenSpatialService.registerForPointerEvents(device, new OpenSpatialEvent.EventListener() {
                @Override
                public void onEventReceived(OpenSpatialEvent event) {
                    PointerEvent pEvent = (PointerEvent) event;
                    mPointerService.updatePointerPosition(device.getAddress(), pEvent.x, pEvent.y);
                }
            });
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Error registering for PointerEvent " + e);
        }

        try {
            mOpenSpatialService.registerForButtonEvents(device, new OpenSpatialEvent.EventListener() {
                @Override
                public void onEventReceived(OpenSpatialEvent event) {
                    ButtonEvent bEvent = (ButtonEvent) event;

                    Log.d(TAG, device.getName() + ": received ButtonEvent of type " + bEvent.buttonEventType);

                    if (bEvent.buttonEventType == ButtonEvent.ButtonEventType.TOUCH0_DOWN) {
                        mPointerService.updatePointerColor(device.getAddress(),
                                Color.alpha(mRedColor),
                                Color.red(mRedColor),
                                Color.green(mRedColor),
                                Color.blue(mRedColor));
                        mPointerService.updatePointerRadius(device.getAddress(), 2 * RADIUS);
                    } else if (bEvent.buttonEventType == ButtonEvent.ButtonEventType.TOUCH0_UP) {
                        mPointerService.updatePointerColor(device.getAddress(),
                                Color.alpha(mWhiteColor),
                                Color.red(mWhiteColor),
                                Color.green(mWhiteColor),
                                Color.blue(mWhiteColor));
                        mPointerService.updatePointerRadius(device.getAddress(), RADIUS);
                    }
                }
            });
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Error registering for PointerEvent " + e);
        }

        try {
            mOpenSpatialService.registerForPose6DEvents(device, new OpenSpatialEvent.EventListener() {
                @Override
                public void onEventReceived(OpenSpatialEvent event) {
                    Pose6DEvent pose6DEvent = (Pose6DEvent) event;
                }
            });
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Error registering for Pose6DEvent " + e);
        }

        try {
            mOpenSpatialService.registerForMotion6DEvents(device, new OpenSpatialEvent.EventListener() {
                @Override
                public void onEventReceived(OpenSpatialEvent event) {
                    Motion6DEvent motion6DEvent = (Motion6DEvent) event;
                    Log.d(TAG, "Motion6D: " +
                            motion6DEvent.accelX + ", " +
                            motion6DEvent.accelY + ", " +
                            motion6DEvent.accelZ + ", " +
                            motion6DEvent.gyroX + ", " +
                            motion6DEvent.gyroY + ", " +
                            motion6DEvent.gyroZ);
                }
            });
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Error registering for Pose6DEvent " + e);
        }

        OpenSpatialEvent.EventListener gestureEventListener = new OpenSpatialEvent.EventListener() {
            @Override
            public void onEventReceived(OpenSpatialEvent event) {
                GestureEvent gestureEvent = (GestureEvent)event;
                Log.d(TAG, device.getName() + ": got GestureEvent of type " + gestureEvent.gestureEventType +
                        " with magnitude " + gestureEvent.magnitude);
            }
        };

        try {
            mOpenSpatialService.registerForGestureEvents(device, gestureEventListener);
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Error registering for GestureEvent " + e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        bindService(new Intent(this, PointerService.class), mPointerServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbindService(mOpenSpatialServiceConnection);
        unbindService(mPointerServiceConnection);
    }

    @Override
    public void deviceConnected(BluetoothDevice device) {
        Log.d(TAG, device.getName() + " connected");
        mPointerService.addPointer(device.getAddress(),
                RADIUS,
                Color.alpha(mWhiteColor),
                Color.red(mWhiteColor),
                Color.green(mWhiteColor),
                Color.blue(mWhiteColor),
                device.getName());

        Log.d(TAG, "Registering " + device.getName());
        registerForEvents(device);
    }

    @Override
    public void deviceDisconnected(BluetoothDevice device) {
        Toast.makeText(this, device.getName() + "disconnected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void buttonEventRegistrationResult(BluetoothDevice device, int status) {
        Toast.makeText(this, "ButtonEvent registration status: " + status, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void pointerEventRegistrationResult(BluetoothDevice device, int status) {
        Toast.makeText(this, "PointerEvent registration status: " + status, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void pose6DEventRegistrationResult(BluetoothDevice device, int status) {
        Toast.makeText(this, "Pose6DEvent registration status: " + status, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void gestureEventRegistrationResult(BluetoothDevice device, int status) {
        Toast.makeText(this, "GestureEvent registration status: " + status, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void motion6DEventRegistrationResult(BluetoothDevice device, int status) {
        Toast.makeText(this, "Motion6D registration status: " + status, Toast.LENGTH_SHORT).show();
    }
}
