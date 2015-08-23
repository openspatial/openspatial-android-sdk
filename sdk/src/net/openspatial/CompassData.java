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

import java.util.Arrays;

/**
 * Contains the raw, unprocessed readings from the compass/magnetometer of an OpenSpatial
 * device in units of uT.
 */
public class CompassData extends OpenSpatialData {

    private final int[] compassData;

    /**
     * @return A compass reading in the x axis (in uT)
     */
    public int getX() {
        return compassData[X];
    }

    /**
     * @return A compass reading in the y axis (in uT)
     */
    public int getY() {
        return compassData[Y];
    }

    /**
     * @return A compass reading in the z axis (in uT)
     */
    public int getZ() {
        return compassData[Z];
    }

    /**
     * Create a new {@code CompassData} of the specified type
     * @param device {@link BluetoothDevice} that sent this data
     * @param compassData Reported compass values
     */
    protected CompassData(BluetoothDevice device, int[] compassData) {
        super(device, DataType.RAW_COMPASS);
        this.compassData = compassData;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", Compass Event: " + Arrays.toString(compassData);
    }
}
