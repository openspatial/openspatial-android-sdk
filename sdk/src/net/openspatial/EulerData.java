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
 * Contains the rotation of the Openspatial device in 3D space. The rotation axis used is x-y-z. The
 * coordinate orientation is right handed with z facing up. All angles are in radians.
 */
public class EulerData extends OpenSpatialData {
    /**
     * Roll value in radians
     */
    public float roll;

    /**
     * Pitch value in radians
     */
    public float pitch;

    /**
     * Yaw value in radians
     */
    public float yaw;

    /**
     * Create a new {@code EulerData} object
     *
     * @param device The device that emitted the Euler values.
     * @param roll The rotation about the x axis
     * @param pitch The rotation about the y axis
     * @param yaw The rotation about the z axis
     */
    public EulerData(BluetoothDevice device, float roll, float pitch, float yaw) {
        super(device, DataType.EULER_ANGLES);
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", Gyroscope Event: [ " + this.roll + ", " + this.pitch + ", " + this.yaw + " ]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeFloat(roll);
        out.writeFloat(pitch);
        out.writeFloat(yaw);
    }

    private EulerData(Parcel in) {
        super(in);
        this.roll = in.readFloat();
        this.pitch = in.readFloat();
        this.yaw = in.readFloat();
    }

    public static final Parcelable.Creator<EulerData> CREATOR
            = new Parcelable.Creator<EulerData>() {
        @Override
        public EulerData createFromParcel(Parcel in) {
            return new EulerData(in);
        }

        @Override
        public EulerData[] newArray(int size) {
            return new EulerData[size];
        }
    };
}
