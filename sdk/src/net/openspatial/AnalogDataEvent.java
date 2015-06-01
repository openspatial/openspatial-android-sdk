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
 * This event contains raw accelerometer and gyroscope readings along the 3 axes.
 */
public class AnalogDataEvent extends OpenSpatialEvent {
    /**
     * The joystick reading along the X axis
     */
    public int joystickX;

    /**
     * The joystick reading along the Y axis
     */
    public int joystickY;

    /**
     * The trigger reading
     */
    public int trigger;

    /**
     * Construct an AnalogDataEvent based on joystick and trigger readings
     */
    public AnalogDataEvent(BluetoothDevice device, int joystickX, int joystickY, int trigger) {
        super(device, EventType.EVENT_ANALOGDATA);

        this.joystickX = joystickX;
        this.joystickY = joystickY;
        this.trigger = trigger;
    }

    // Methods to make this class Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(joystickX);
        out.writeInt(joystickY);
        out.writeInt(trigger);
    }

    private AnalogDataEvent(Parcel in) {
        super(in);
        joystickX = in.readInt();
        joystickY = in.readInt();
        trigger = in.readInt();
    }

    public static final Creator<AnalogDataEvent> CREATOR = new Creator<AnalogDataEvent>() {
        @Override
        public AnalogDataEvent createFromParcel(Parcel in) {
            return new AnalogDataEvent(in);
        }

        @Override
        public AnalogDataEvent[] newArray(int size) {
            return new AnalogDataEvent[size];
        }
    };
}
