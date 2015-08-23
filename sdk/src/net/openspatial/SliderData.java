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
 * Reports a slider action.
 */
public class SliderData extends OpenSpatialData {

    private final SliderType sliderType;

    /**
     * @return The type of slider event that is being reported
     */
    public SliderType getSliderType() {
        return sliderType;
    }

    /**
     * Create a new {@code SliderData} object
     * @param device The {@link BluetoothDevice} reporting the slider data.
     * @param sliderType The {@link SliderType} reported.
     */
    protected SliderData(BluetoothDevice device, SliderType sliderType) {
        super(device, DataType.SLIDER);
        this.sliderType = sliderType;
    }

    @Override
    public String toString() {
        return super.toString() +  ", Slider: " + getSliderType().name();
    }
}
