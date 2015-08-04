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
 * Contains the raw, unprocessed readings from the compass/magnetometer of an OpenSpatial
 * device in units of uT.
 */
public class CompassData extends OpenSpatialData {

    /**
     * A compass reading in the x axis (in uT)
     */
    public int x;

    /**
     * A compass reading in the y axis (in uT)
     */
    public int y;

    /**
     * A compass reading in the z axis (in uT)
     */
    public int z;

    /**
     * Create a new {@code CompassData} of the specified type
     * @param device {@link BluetoothDevice} that sent this data
     * @param x compass reading in the x axis
     * @param y compass reading in the y axis
     * @param z compass reading in the z axis
     */
    public CompassData(BluetoothDevice device, int x, int y, int z) {
        super(device, DataType.RAW_COMPASS);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", Compass Event: [ " + this.x + ", " + this.y + ", " + this.z + " ]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(z);
    }

    private CompassData(Parcel in) {
        super(in);
        this.x = in.readInt();
        this.y = in.readInt();
        this.z = in.readInt();
    }

    public static final Parcelable.Creator<CompassData> CREATOR
            = new Parcelable.Creator<CompassData>() {
        @Override
        public CompassData createFromParcel(Parcel in) {
            return new CompassData(in);
        }

        @Override
        public CompassData[] newArray(int size) {
            return new CompassData[size];
        }
    };
}
