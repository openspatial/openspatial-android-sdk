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

import android.bluetooth.BluetoothDevice;

public class OpenSpatialConstants {
    public static final String OPENSPATIAL_SERVICE_UUID = "00000002-0000-1000-8000-a0e5e9000000";

    public static final String OPENSPATIAL_POSE_6D_CHARACTERISTIC = "00000205-0000-1000-8000-A0E5E9000000";
    public static final String OPENSPATIAL_POSITION_2D_CHARACTERISTIC = "00000206-0000-1000-8000-A0E5E9000000";
    public static final String OPENSPATIAL_BUTTONSTATE_CHARACTERISTIC = "00000207-0000-1000-8000-A0E5E9000000";
    public static final String OPENSPATIAL_GESTURE_CHARACTERISTIC = "00000208-0000-1000-8000-A0E5E9000000";
    public static final String OPENSPATIAL_MOTION_6D_CHARACTERISTIC = "00000209-0000-1000-8000-A0E5E9000000";
    public static final String OPENSPATIAL_ANALOGDATA_CHARACTERISTIC = "0000020C-0000-1000-8000-A0E5E9000000";

    public static final BluetoothDevice EMULATOR_DEVICE = null;

    public static final String BLUETOOTH_DEVICE = "net.openspatial.BLUETOOTH_DEVICE";
    public static final String OPENSPATIAL_EVENT = "net.openspatial.OPENSPATIAL_EVENT";
    public static final String IDENTIFIER = "net.openspatial.IDENTIFIER";
    public static final String STATUS = "net.openspatial.STATUS";
    public static final String EVENT_TYPE = "net.openspatial.EVENT_TYPE";
    public static final String EVENT_UUID = "net.openspatial.EVENT_UUID";
    public static final String SET_REGISTRATION_STATUS = "net.openspatial.SET_REGISTRATION_STATUS";
    public static final String INFO_TYPE = "net.openspatial.INFO_TYPE";

    // Actions for intents sent by the service
    public static final String OPENSPATIAL_LIST_DEVICES_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_LIST_DEVICES_INTENT_ACTION";
    public static final String OPENSPATIAL_CHANGE_REGISTRATION_STATE_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_CHANGE_REGISTRATION_STATE_INTENT_ACTION";
    public static final String OPENSPATIAL_QUERY_DEVICE_INFO_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_QUERY_DEVICE_INFO_INTENT_ACTION";

    // Actions for events received by the service
    public static final String OPENSPATIAL_EVENT_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_EVENT_INTENT_ACTION";
    public static final String OPENSPATIAL_DEVICE_INFO_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_DEVICE_INFO_INTENT_ACTION";
    public static final String OPENSPATIAL_DEVICE_CONNECTED_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_DEVICE_CONNECTED_INTENT_ACTION";
    public static final String OPENSPATIAL_DEVICE_DISCONNECTED_INTENT_ACTION =
            "net.openspatial.OPENSPATIAL_DEVICE_DISCONNECTED_INTENT_ACTION";

    public static final String OPENSPATIAL_REGISTRATION_CHANGE_ATTEMPT_EVENT_RESULT =
            "net.openspatial.OPENSPATIAL_REGISTER_BUTTON_EVENT_RESULT";

    public static final String SKELETAL_LEFT_HAND = "skeletal.LEFT_HAND";
    public static final String SKELETAL_RIGHT_HAND = "skeletal.RIGHT_HAND";
    public static final String SKELETAL_LEFT_FOREARM = "skeletal.LEFT_FOREARM";
    public static final String SKELETAL_RIGHT_FOREARM = "skeletal.RIGHT_FOREARM";
    public static final String SKELETAL_LEFT_UPPER_ARM = "skeletal.LEFT_UPPER_ARM";
    public static final String SKELETAL_RIGHT_UPPER_ARM = "skeletal.RIGHT_UPPER_ARM";

    public static final String INFO_BATTERY_LEVEL = "net.openspatial.INFO_BATTERY_LEVEL";
}
