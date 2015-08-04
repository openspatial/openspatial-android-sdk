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
 * Contains the raw, unprocessed readings from the gyroscope of an OpenSpatial device in
 * radians/second.
 */
public class GyroscopeData extends OpenSpatialData {

    /**
     * A Gyroscope reading in the x axis
     */
    public float x;

    /**
     * A Gyroscope reading in the y axis
     */
    public float y;

    /**
     * A Gyroscope reading in the z axis
     */
    public float z;

    /**
     * Create a new {@code GyroscopeData} of the specified type
     * @param device The device reporting the gyroscopic data.
     * @param x The raw gyroscope reading about the x axis.
     * @param y The raw gyroscope reading about the y axis.
     * @param z The raw gyroscope reading about the z axis.
     */
    public GyroscopeData(BluetoothDevice device, float x, float y, float z) {
        super(device, DataType.RAW_GYRO);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", Gyroscope Event: [ " + this.x + ", " + this.y + ", " + this.z + " ]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(z);
    }

    private GyroscopeData(Parcel in) {
        super(in);
        this.x = in.readFloat();
        this.y = in.readFloat();
        this.z = in.readFloat();
    }

    public static final Parcelable.Creator<GyroscopeData> CREATOR
            = new Parcelable.Creator<GyroscopeData>() {
        @Override
        public GyroscopeData createFromParcel(Parcel in) {
            return new GyroscopeData(in);
        }

        @Override
        public GyroscopeData[] newArray(int size) {
            return new GyroscopeData[size];
        }
    };
}
