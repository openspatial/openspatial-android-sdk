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
 * Contains raw accelerometer in units of G.
 */
public class AccelerometerData extends OpenSpatialData {

    /**
     * Accelerometer reading about x axis.
     */
    public float x;

    /**
     * Accelerometer reading about x axis.
     */
    public float y;

    /**
     * Accelerometer reading about x axis.
     */
    public float z;

    /**
     * Create a new {@code AccelerometerData} of the specified type
     * @param device {@link BluetoothDevice} that sent this data
     * @param x accel reading in the x direction (in G's)
     * @param y accel reading in the y direction (in G's)
     * @param z accel reading in the z direction (in G's)
     */
    public AccelerometerData(BluetoothDevice device, float x, float y, float z) {
        super(device, DataType.RAW_ACCELEROMETER);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", Accelerometer Event: [ " + this.x + ", " + this.y + ", " + this.z + " ]";
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

    private AccelerometerData(Parcel in) {
        super(in);
        this.x = in.readFloat();
        this.y = in.readFloat();
        this.z = in.readFloat();
    }

    public static final Parcelable.Creator<AccelerometerData> CREATOR
            = new Parcelable.Creator<AccelerometerData>() {
        @Override
        public AccelerometerData createFromParcel(Parcel in) {
            return new AccelerometerData(in);
        }

        @Override
        public AccelerometerData[] newArray(int size) {
            return new AccelerometerData[size];
        }
    };
}
