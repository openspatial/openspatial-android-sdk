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
 * Contains the rotation of the Openspatial device in 3D space. The rotation axis used is x-y-z. The
 * coordinate orientation is right handed with z facing up. All angles are in radians.
 */
public class EulerData extends OpenSpatialData {

    private final float[] eulerData;

    /**
     * @return Roll value in radians
     */
    public float getRoll() {
        return eulerData[X];
    }

    /**
     * @return Pitch value in radians
     */
    public float getPitch() {
        return eulerData[Y];
    }

    /**
     * @return Yaw value in radians
     */
    public float getYaw() {
        return eulerData[Z];
    }

    /**
     * Create a new {@code EulerData} object
     *
     * @param device The device that emitted the Euler values.
     * @param eulerData The reported 3-axis rotation.
     */
    protected EulerData(BluetoothDevice device, float[] eulerData) {
        super(device, DataType.EULER_ANGLES);
        this.eulerData = eulerData;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", Euler Data: " + Arrays.toString(eulerData);
    }
}
