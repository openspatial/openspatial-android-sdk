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

/**
 * Contains gesture information being detected by an OpenSpatial device.
 */
public class GestureData extends OpenSpatialData {

    /**
     * The type of gesture that is being reported
     */
    public GestureType gestureType;

    /**
     * Create a new {@code GestureData} of the specified type
     * @param device The {@link BluetoothDevice} reporting the gesture.
     * @param gestureType the type of gesture being reported.
     */
    public GestureData(BluetoothDevice device, GestureType gestureType) {
        super(device, DataType.GESTURE);
        this.gestureType = gestureType;
    }

    @Override
    public String toString() {
        return super.toString() +  ", Gesture Event: " + gestureType.name();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeSerializable(gestureType);
    }

    private GestureData(Parcel in) {
        super(in);
        this.gestureType = (GestureType) in.readSerializable();
    }

    public static final Parcelable.Creator<GestureData> CREATOR
            = new Parcelable.Creator<GestureData>() {
        @Override
        public GestureData createFromParcel(Parcel in) {
            return new GestureData(in);
        }

        @Override
        public GestureData[] newArray(int size) {
            return new GestureData[size];
        }
    };
}
