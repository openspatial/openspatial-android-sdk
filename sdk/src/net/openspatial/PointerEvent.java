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

/**
 * A {@code PointerEvent} represents an instance of the pointer being moved. There are two types of pointer events:
 * {@code RELATIVE} and {@code ABSOLUTE}. In a {@code RELATIVE} {@code PointerEvent}, {@code x} and {@code y}
 * specify the change in the position of the pointer relative to its previous position. In an {@code ABSOLUTE}
 * {@code PointerEvent}, {@code x} and {@code y} specify the current position of the pointer irrespective of its
 * previous position.
 */
public class PointerEvent extends OpenSpatialEvent {
    /**
     * The type of the {@code PointerEvent}
     */
    public enum PointerEventType {
        /**
         * The event specifies translation along the x and y axis relative to the previous position of the pointer
         */
        RELATIVE,

        /**
         * The event specifies the x and y coordinates of the absolute position of the pointer
         */
        ABSOLUTE,
    }

    /**
     * The {@code PointerEvent} type
     */
    public PointerEventType pointerEventType;

    /**
     * The relative or absolute position on the x axis
     */
    public int x;

    /**
     * The relative or absolute position on the y axis
     */
    public int y;

    /**
     * Create a new {@code PointerEvent}
     * @param type Type of the {@code PointerEvent}
     * @param x The x value
     * @param y The y value
     */
    public PointerEvent(BluetoothDevice device, PointerEventType type, int x, int y) {
        super(device, EventType.EVENT_POINTER);

        this.pointerEventType = type;
        this.x = x;
        this.y = y;
    }

    // Methods to make the class Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeSerializable(pointerEventType);
        out.writeInt(x);
        out.writeInt(y);
    }

    private PointerEvent(Parcel in) {
        super(in);
        this.pointerEventType = (PointerEventType)in.readSerializable();
        this.x = in.readInt();
        this.y = in.readInt();
    }

    public static final Creator<PointerEvent> CREATOR = new Creator<PointerEvent>() {
        @Override
        public PointerEvent createFromParcel(Parcel in) {
            return new PointerEvent(in);
        }

        @Override
        public PointerEvent[] newArray(int size) {
            return new PointerEvent[size];
        }
    };
}
