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

/**
 * Contains the offset values of any analog buttons or actuators an OpenSpatial device may have.
 */
public class AnalogData extends OpenSpatialData {
    /**
     * The reading for analog0
     */
    public int analog0;

    /**
     * The reading for analog1
     */
    public int analog1;

    /**
     * The reading for analog2
     */
    public int analog2;

    /**
     * Create a new {@code AnalogData} of the specified type
     * @param device {@link BluetoothDevice} that sent this data
     * @param analog0 Value reported by analog0
     * @param analog1 Value reported by analog1
     * @param analog2 Value reported by analog2
     */
    public AnalogData(BluetoothDevice device, int analog0, int analog1, int analog2) {
        super(device, DataType.ANALOG);
        this.analog0 = analog0;
        this.analog1 = analog1;
        this.analog2 = analog2;
    }

    @Override
    public String toString() {
        return super.toString() + ", Analog Event: ["
                + this.analog0 + ", " + this.analog1 + ", " + this.analog2 + "]";
    }
    @Override
    public int describeContents() {
        return 0;
    }
}
