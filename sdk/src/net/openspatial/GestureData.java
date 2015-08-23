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
 * Contains gesture information being detected by an OpenSpatial device.
 */
public class GestureData extends OpenSpatialData {

    private final GestureType gestureType;

    /**
     * @return The type of gesture that is being reported
     */
    public GestureType getGestureType() {
        return gestureType;
    }

    /**
     * Create a new {@code GestureData} of the specified type
     * @param device The {@link BluetoothDevice} reporting the gesture.
     * @param gestureType the type of gesture being reported.
     */
    protected GestureData(BluetoothDevice device, GestureType gestureType) {
        super(device, DataType.GESTURE);
        this.gestureType = gestureType;
    }

    @Override
    public String toString() {
        return super.toString() +  ", Gesture Data: " + getGestureType().name();
    }
}
