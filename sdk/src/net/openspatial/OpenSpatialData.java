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
 * The base class for all data delivered over the {@link net.openspatial.OpenSpatialService}.
 */
abstract public class OpenSpatialData {

    protected static final int X = 0;
    protected static final int Y = 1;
    protected static final int Z = 2;

    /**
     * The type of OpenSpatial contained within.
     */
    public final DataType dataType;

    /**
     * The time value returned by {@code System.currentTimeMillis()} at object creation.
     */
    public final long timestamp;

    /**
     * The OpenSpatial device that reported the data.
     */
    public final BluetoothDevice device;

    /**
     * Create an {@code OpenSpatialEvent} with a given type.
     * @param device The source of the data being reported.
     * @param type The type of the data.
     */
    protected OpenSpatialData(BluetoothDevice device, DataType type) {
        if (device == null) {
            throw new IllegalArgumentException("Null device!");
        }
        this.device = device;
        this.dataType = type;
        timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "Device: " + device.getName();
    }
}
