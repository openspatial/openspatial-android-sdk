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
 * the values in to units of degrees/second.
 */
public class GyroscopeData extends OpenSpatialData {

    private final short[] gyroData;

    /**
     * A Gyroscope reading in the x axis.
     * @return Gyroscopic sensor reading about the x axis.
     */
    public short getX() {
        return gyroData[X];
    }

    /**
     * A Gyroscope reading in the y axis.
     * @return Gyroscopic sensor reading about the y axis.
     */
    public short getY() {
        return gyroData[Y];
    }

    /**
     * A Gyroscope reading in the z axis
     * @return Gyroscopic sensor reading about the x axis.
     */
    public short getZ() {
        return gyroData[Z];
    }

    /**
     * Create a new {@code GyroscopeData} of the specified type
     * @param device The device reporting the gyroscopic data.
     * @param gyroData The reported Gyroscopic data.
     */
    protected GyroscopeData(BluetoothDevice device, short[] gyroData) {
        super(device, DataType.RAW_GYRO);
        this.gyroData = gyroData;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", Gyroscope Data: " + Arrays.toString(gyroData);
    }
}
