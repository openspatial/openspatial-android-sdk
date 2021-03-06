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
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * The base class for all events delivered over the {@link net.openspatial.OpenSpatialService}
 *
 * @deprecated use {@link OpenSpatialData} instead.
 */

@Deprecated
abstract public class OpenSpatialEvent implements Parcelable {
    /**
     * Type of the event.
     * @deprecated use {@link DataType} instead.
     */
    @Deprecated
    public enum EventType {
        /**
         * Button event
         * @see net.openspatial.ButtonEvent
         */
        EVENT_BUTTON,

        /**
         * Pointer event
         * @see net.openspatial.PointerEvent
         */
        EVENT_POINTER,

        /**
         * 3D rotation event
         * @see net.openspatial.Pose6DEvent
         */
        EVENT_POSE6D,

        /**
         * Gesture event
         * @see net.openspatial.GestureEvent
         */
        EVENT_GESTURE,

        /**
         * Motion6D event
         * @see net.openspatial.Motion6DEvent
         */
        EVENT_MOTION6D,

        /**
         * AnalogData event
         * @see net.openspatial.AnalogDataEvent
         */
        EVENT_ANALOGDATA,

        /**
         * Extended event
         * @see net.openspatial.ExtendedEvent
         */
        EVENT_EXTENDED,
    }

    public static final Map<EventType, String> EVENT_UUID_MAP = new HashMap<EventType, String>();

    static {
        EVENT_UUID_MAP.put(EventType.EVENT_BUTTON,
                OpenSpatialConstants.OPENSPATIAL_BUTTONSTATE_CHARACTERISTIC);
        EVENT_UUID_MAP.put(EventType.EVENT_POINTER,
                OpenSpatialConstants.OPENSPATIAL_POSITION_2D_CHARACTERISTIC);
        EVENT_UUID_MAP.put(EventType.EVENT_POSE6D,
                OpenSpatialConstants.OPENSPATIAL_POSE_6D_CHARACTERISTIC);
        EVENT_UUID_MAP.put(EventType.EVENT_GESTURE,
                OpenSpatialConstants.OPENSPATIAL_GESTURE_CHARACTERISTIC);
        EVENT_UUID_MAP.put(EventType.EVENT_MOTION6D,
                OpenSpatialConstants.OPENSPATIAL_MOTION_6D_CHARACTERISTIC);
        EVENT_UUID_MAP.put(EventType.EVENT_ANALOGDATA,
                OpenSpatialConstants.OPENSPATIAL_ANALOGDATA_CHARACTERISTIC);

    }

    /**
     * The event type
     * @see net.openspatial.OpenSpatialEvent.EventType
     */
    public EventType eventType;

    /**
     * The time in milliseconds at which this event occurred measured since epoch
     */
    public long timestamp;

    /**
     * Bluetooth device that sent the event
     */
    public BluetoothDevice device;

    /**
     * A listener interface for clients interested in {@link net.openspatial.OpenSpatialEvent}s
     *
     * @deprecated use {@link OpenSpatialInterface} instead.
     */
    @Deprecated
    public interface EventListener {
        /**
         * Callback method that is called when a new {@link net.openspatial.OpenSpatialEvent} is received
         *
         * @param event The {@code OpenSpatialEvent} that was received
         *
         */
        void onEventReceived(OpenSpatialEvent event);
    }

    @Override
    public String toString() {
        return "Device: " + this.device.getName() + ", Event Type: " + this.eventType.name();
    }

    /**
     * Create an {@code OpenSpatialEvent} with a given type
     * @param type The type of the event
     */
    public OpenSpatialEvent(BluetoothDevice bDevice, EventType type) {
        device = bDevice;
        eventType = type;
        timestamp = System.currentTimeMillis();
    }

    // Methods to make the class Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeSerializable(eventType);
        out.writeLong(timestamp);
        out.writeParcelable(device, flags);
    }

    protected OpenSpatialEvent(Parcel in) {
        this.eventType = (EventType)in.readSerializable();
        this.timestamp = in.readLong();
        this.device = (BluetoothDevice)in.readParcelable(BluetoothDevice.class.getClassLoader());
    }
}
