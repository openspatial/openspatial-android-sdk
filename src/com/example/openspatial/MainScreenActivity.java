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

package com.example.openspatial;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import net.openspatial.*;

public class MainScreenActivity extends Activity {
    OpenSpatialService mService;

    private static String TAG = MainScreenActivity.class.getSimpleName();

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((OpenSpatialService.OpenSpatialServiceBinder)service).getService();

            try {
                mService.registerForPointerEvents(null, new OpenSpatialEvent.EventListener() {
                    @Override
                    public void onEventReceived(OpenSpatialEvent event) {
                        PointerEvent pEvent = (PointerEvent)event;
                        Log.d(TAG, "Received PointerEvent of type " + pEvent.pointerEventType +
                        " with x: " + pEvent.x + " and y: " + pEvent.y);
                    }
                });
            } catch (OpenSpatialException e) {
                Log.e(TAG, "Error registering for PointerEvent " + e);
            }

            try {
                mService.registerForButtonEvents(null, new OpenSpatialEvent.EventListener() {
                    @Override
                    public void onEventReceived(OpenSpatialEvent event) {
                        ButtonEvent bEvent = (ButtonEvent)event;

                        Log.d(TAG, "Received ButtonEvent of type " + bEvent.buttonEventType);
                    }
                });
            } catch (OpenSpatialException e) {
                Log.e(TAG, "Error registering for PointerEvent " + e);
            }

            try {
                mService.registerForRotationEvents(null, new OpenSpatialEvent.EventListener() {
                    @Override
                    public void onEventReceived(OpenSpatialEvent event) {
                        RotationEvent rotationEvent = (RotationEvent) event;
                        Quaternion quaternion = rotationEvent.getQuaternion();
                        Log.d(TAG,
                                "Got rotationEvent with value (x=" + quaternion.x + ", y=" + quaternion.y +
                                ", z=" + quaternion.z + ", w=" + quaternion.w +")");

                    }
                });
            } catch (OpenSpatialException e) {
                Log.e(TAG, "Error registering for PointerEvent " + e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        bindService(new Intent(this, OpenSpatialService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbindService(mServiceConnection);
    }
}
