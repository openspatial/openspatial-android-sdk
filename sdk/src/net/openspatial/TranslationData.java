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
 * Contains the delta translation reported by an OpenSpatial device.
 */
public class TranslationData extends OpenSpatialData {
    /**
     * A Translation reading in the x axis
     */
    public float x;

    /**
     * A Translation reading in the y axis
     */
    public float y;

    /**
     * A Translation reading in the z axis
     */
    public float z;

    /**
     * Create a new {@code TranslationData} object
     *
     * @param device The {@link BluetoothDevice} reporting the translation data.
     * @param x The reported translation in the x direction.
     * @param y The reported translation in the y direction.
     * @param z The reported translation in the z direction.
     */
    public TranslationData(BluetoothDevice device, float x, float y, float z) {
        super(device, DataType.TRANSLATIONS);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", Translation Event: [ " + this.x + ", " + this.y + ", " + this.z + " ]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(z);
    }

    private TranslationData(Parcel in) {
        super(in);
        this.x = in.readFloat();
        this.y = in.readFloat();
        this.z = in.readFloat();
    }

    public static final Parcelable.Creator<TranslationData> CREATOR
            = new Parcelable.Creator<TranslationData>() {
        @Override
        public TranslationData createFromParcel(Parcel in) {
            return new TranslationData(in);
        }

        @Override
        public TranslationData[] newArray(int size) {
            return new TranslationData[size];
        }
    };
}
