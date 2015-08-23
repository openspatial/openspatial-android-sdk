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
 * Contains the offset values of any analog buttons or actuators an OpenSpatial device may have.
 */
public class AnalogData extends OpenSpatialData {

    private final int[] analogData;

    /**
     * Provides the analog sensor reading reported by a sensor with a given index.
     * @param index The analog sensor whose value the caller is interested in.
     * @return The last reported analog sensor value.
     */
    public int getAnalogValue(int index) {
        return analogData[index];
    }

    /**
     * Create a new {@code AnalogData} of the specified type
     * @param device {@link BluetoothDevice} that sent this data
     * @param analogData The data reported by the device's analog sensors.
     */
    protected AnalogData(BluetoothDevice device, int[] analogData) {
        super(device, DataType.ANALOG);
        this.analogData = analogData;
    }

    @Override
    public String toString() {
        return super.toString() + ", Analog Data: " + Arrays.toString(analogData);
    }
}
