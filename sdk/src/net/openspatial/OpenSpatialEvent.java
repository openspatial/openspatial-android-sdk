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
import android.os.Parcel;
import android.os.Parcelable;

/**
 * The base class for all events delivered over the {@link net.openspatial.OpenSpatialService}
 */
abstract public class OpenSpatialEvent implements Parcelable {
    /**
     * Type of the event.
     */
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
         */
        EVENT_GESTURE,

        /**
         * Motion6D event
         * @see net.openspatial.Motion6DEvent
         */
        EVENT_MOTION6D,
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
     */
    public interface EventListener {
        /**
         * Callback method that is called when a new {@link net.openspatial.OpenSpatialEvent} is received
         *
         * @param event The {@code OpenSpatialEvent} that was received
         *
         */
        public void onEventReceived(OpenSpatialEvent event);
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
