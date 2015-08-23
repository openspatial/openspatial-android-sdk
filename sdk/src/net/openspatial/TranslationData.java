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
 * Contains the delta translation reported by an OpenSpatial device.
 */
public class TranslationData extends OpenSpatialData {

    private final float[] translationData;

    /**
     * @return A Translation reading in the x axis
     */
    public float getX() {
        return translationData[X];
    }

    /**
     * @return A Translation reading in the y axis
     */
    public float getY() {
        return translationData[Y];
    }

    /**
     * @return A Translation reading in the z axis
     */
    public float getZ() {
        return translationData[Z];
    }

    /**
     * Create a new {@code TranslationData} object
     *
     * @param device The {@link BluetoothDevice} reporting the translation data.
     * @param translationData The translation data reprted by the device.
     */
    protected TranslationData(BluetoothDevice device, float[] translationData) {
        super(device, DataType.TRANSLATIONS);
        this.translationData = translationData;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", Translation Data: " + Arrays.toString(translationData);
    }
}
