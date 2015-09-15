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

import java.util.Arrays;

/**
 * Contains raw accelerometer LSB readings. Use the FSR value obtained from
 * {@link OpenSpatialService#getParameter(BluetoothDevice, DataType, DeviceParameter)} to convert
 * the values in to units of G.
 */
public class AccelerometerData extends OpenSpatialData {

    private final short[] accelData;

    /**
     * @return Accelerometer reading about x axis.
     */
    public short getX() {
        return accelData[X];
    }

    /**
     * @return Accelerometer reading about y axis.
     */
    public short getY() {
        return accelData[Y];
    }

    /**
     * @return Accelerometer reading about z axis.
     */
    public short getZ() {
        return accelData[Z];
    }

    /**
     * Create a new {@code AccelerometerData} of the specified type
     * @param device {@link BluetoothDevice} that sent this data
     * @param accelData Reported accelerometer values.
     */
    protected AccelerometerData(BluetoothDevice device, short[] accelData) {
        super(device, DataType.RAW_ACCELEROMETER);
        this.accelData = accelData;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", Accelerometer Data: " + Arrays.toString(accelData);
    }
}
