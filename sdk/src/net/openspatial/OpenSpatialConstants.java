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

import android.bluetooth.BluetoothDevice;

public class OpenSpatialConstants {
    public static final String OPENSPATIAL_SERVICE_UUID = "00000002-0000-1000-8000-a0e5e9000000";

    public static final String OPENSPATIAL_POSE_6D_CHARACTERISTIC = "00000205-0000-1000-8000-A0E5E9000000";
    public static final String OPENSPATIAL_POSITION_2D_CHARACTERISTIC = "00000206-0000-1000-8000-A0E5E9000000";
    public static final String OPENSPATIAL_BUTTONSTATE_CHARACTERISTIC = "00000207-0000-1000-8000-A0E5E9000000";
    public static final String OPENSPATIAL_GESTURE_CHARACTERISTIC = "00000208-0000-1000-8000-A0E5E9000000";
    public static final String OPENSPATIAL_MOTION6D_CHARACTERISTIC = "00000209-0000-1000-8000-A0E5E9000000";

    public static final BluetoothDevice EMULATOR_DEVICE = null;

    public static final String BLUETOOTH_DEVICE = "net.openspatial.BLUETOOTH_DEVICE";
    public static final String OPENSPATIAL_EVENT = "net.openspatial.OPENSPATIAL_EVENT";
    public static final String IDENTIFIER = "net.openspatial.IDENTIFIER";
    public static final String STATUS = "net.openspatial.STATUS";

    // Actions for intents sent by the service
    public static final String OPENSPATIAL_LIST_DEVICES_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_LIST_DEVICES_INTENT_ACTION";
    public static final String OPENSPATIAL_REGISTER_BUTTON_EVENT_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_REGISTER_BUTTON_EVENT_INTENT_ACTION";
    public static final String OPENSPATIAL_REGISTER_POINTER_EVENT_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_REGISTER_POINTER_EVENT_INTENT_ACTION";
    public static final String OPENSPATIAL_REGISTER_GESTURE_EVENT_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_REGISTER_GESTURE_EVENT_INTENT_ACTION";
    public static final String OPENSPATIAL_REGISTER_POSE6D_EVENT_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_REGISTER_POSE6D_EVENT_INTENT_ACTION";
    public static final String OPENSPATIAL_REGISTER_MOTION6D_EVENT_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_REGISTER_MOTION6D_EVENT_INTENT_ACTION";

    public static final String OPENSPATIAL_UNREGISTER_BUTTON_EVENT_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_UNREGISTER_BUTTON_EVENT_INTENT_ACTION";
    public static final String OPENSPATIAL_UNREGISTER_POINTER_EVENT_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_UNREGISTER_POINTER_EVENT_INTENT_ACTION";
    public static final String OPENSPATIAL_UNREGISTER_GESTURE_EVENT_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_UNREGISTER_GESTURE_EVENT_INTENT_ACTION";
    public static final String OPENSPATIAL_UNREGISTER_POSE6D_EVENT_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_UNREGISTER_POSE6D_EVENT_INTENT_ACTION";
    public static final String OPENSPATIAL_UNREGISTER_MOTION6D_EVENT_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_UNREGISTER_MOTION6D_EVENT_INTENT_ACTION";

    // Actions for events received by the service
    public static final String OPENSPATIAL_BUTTON_EVENT_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_BUTTON_EVENT_INTENT_ACTION";
    public static final String OPENSPATIAL_POINTER_EVENT_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_POINTER_EVENT_INTENT_ACTION";
    public static final String OPENSPATIAL_MOTION6D_EVENT_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_MOTION6D_EVENT_INTENT_ACTION";
    public static final String OPENSPATIAL_POSE6D_EVENT_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_POSE6D_EVENT_INTENT_ACTION";
    public static final String OPENSPATIAL_GESTURE_EVENT_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_GESTURE_EVENT_INTENT_ACTION";
    public static final String OPENSPATIAL_DEVICE_CONNECTED_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_DEVICE_CONNECTED_INTENT_ACTION";
    public static final String OPENSPATIAL_DEVICE_DISCONNECTED_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_DEVICE_DISCONNECTED_INTENT_ACTION";

    public static final String OPENSPATIAL_REGISTER_BUTTON_EVENT_RESULT =
            "net.openspatial.OPENSPATIAL_REGISTER_BUTTON_EVENT_RESULT";
    public static final String OPENSPATIAL_REGISTER_POINTER_EVENT_RESULT =
            "net.openspatial.OPENSPATIAL_REGISTER_POINTER_EVENT_RESULT";
    public static final String OPENSPATIAL_REGISTER_GESTURE_EVENT_RESULT =
            "net.openspatial.OPENSPATIAL_REGISTER_GESTURE_EVENT_RESULT";
    public static final String OPENSPATIAL_REGISTER_POSE6D_EVENT_RESULT =
            "net.openspatial.OPENSPATIAL_REGISTER_POSE6D_EVENT_RESULT";
    public static final String OPENSPATIAL_REGISTER_MOTION6D_EVENT_RESULT =
            "net.openspatial.OPENSPATIAL_REGISTER_MOTION6D_EVENT_RESULT";
}
