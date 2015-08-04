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
 * Reports a slider action.
 */
public class SliderData extends OpenSpatialData {
    /**
     * The type of slider event that is being reported
     */
    public SliderType sliderType;

    /**
     * Create a new {@code SliderData} object
     * @param device The {@link BluetoothDevice} reporting the slider data.
     * @param sliderType The {@link SliderType} reported.
     */
    public SliderData(BluetoothDevice device, SliderType sliderType) {
        super(device, DataType.SLIDER);
        this.sliderType = sliderType;
    }

    @Override
    public String toString() {
        return super.toString() +  ", RelativeXY Event: " + sliderType.name();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeSerializable(sliderType);
    }

    private SliderData(Parcel in) {
        super(in);
        this.sliderType = (SliderType) in.readSerializable();
    }

    public static final Parcelable.Creator<SliderData> CREATOR
            = new Parcelable.Creator<SliderData>() {
        @Override
        public SliderData createFromParcel(Parcel in) {
            return new SliderData(in);
        }

        @Override
        public SliderData[] newArray(int size) {
            return new SliderData[size];
        }
    };
}
