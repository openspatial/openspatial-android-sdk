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
 * A {@code RelativeXYEvent} provides a consumer with planar deltas about the the x and y
 * axis. It is similar in nature to the output of pointing devices such as computer mice or
 * trackpads.
 */
public class RelativeXYData extends OpenSpatialData {

    private final int[] relativeXY;

    /**
     * @return The relative translation in the x axis
     */
    public int getX() {
        return relativeXY[X];
    }

    /**
     * @return the relative translation in the y axis
     */
    public int getY() {
        return relativeXY[Y];
    }

    /**
     * Create a new {@code RelativeXYData}
     * @param device The device that emitted the relative x and y values
     * @param relativeXY The reported planer offsets
     */
    protected RelativeXYData(BluetoothDevice device, int[] relativeXY) {
        super(device, DataType.RELATIVE_XY);
        this.relativeXY = relativeXY;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", RelativeXY Data: " + Arrays.toString(relativeXY);
    }
}
