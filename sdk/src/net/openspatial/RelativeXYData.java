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
 * A {@code RelativeXYEvent} provides a consumer with planar deltas about the the x and y
 * axis. It is similar in nature to the output of pointing devices such as computer mice or
 * trackpads.
 */
public class RelativeXYData extends OpenSpatialData {
    /**
     * A relative translation in the x axis
     */
    public int x;

    /**
     * A relative translation in the y axis
     */
    public int y;

    /**
     * Create a new {@code RelativeXYData}
     * @param device The device that emitted the relative x and y values
     * @param x The rotation about the x axis
     * @param y The rotation about the y axis
     */
    public RelativeXYData(BluetoothDevice device, int x, int y) {
        super(device, DataType.RELATIVE_XY);
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", RelativeXY Event: [ " + this.x + ", " + this.y + " ]";
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
    }

    private RelativeXYData(Parcel in) {
        super(in);
        this.x = in.readInt();
        this.y = in.readInt();
    }

    public static final Parcelable.Creator<RelativeXYData> CREATOR
            = new Parcelable.Creator<RelativeXYData>() {
        @Override
        public RelativeXYData createFromParcel(Parcel in) {
            return new RelativeXYData(in);
        }

        @Override
        public RelativeXYData[] newArray(int size) {
            return new RelativeXYData[size];
        }
    };
}
