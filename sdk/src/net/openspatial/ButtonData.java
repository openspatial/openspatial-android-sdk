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
 * Contains a report of a button entering a new state.
 */
public class ButtonData extends OpenSpatialData {

    private final int buttonId;

    private final ButtonState buttonState;

    /**
     * @return Button ID
     */
    public int getButtonId() {
        return buttonId;
    }

    /**
     * @return The {@link ButtonState} the button has entered
     */
    public ButtonState getButtonState() {
        return buttonState;
    }

    /**
     * Create a new {@code ButtonData} of the specified type
     * @param device {@link BluetoothDevice} that sent this data
     * @param buttonId Identifier of a specific button on a device
     * @param buttonState Type of the {@code ButtonData}
     */
    protected ButtonData(BluetoothDevice device, int buttonId, ButtonState buttonState) {
        super(device, DataType.BUTTON);
        this.buttonId = buttonId;
        this.buttonState = buttonState;
    }

    @Override
    public String toString() {
        return super.toString() + ", Button " + getButtonId() + " " + getButtonState().name();
    }
}
