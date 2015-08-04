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
 * Contains a report of a button entering a new state.
 */
public class ButtonData extends OpenSpatialData {

    /**
     * Button ID
     */
    public int buttonId;

    /**
     * The {@link ButtonState} the button has entered
     */
    public ButtonState buttonState;

    /**
     * Create a new {@code ButtonData} of the specified type
     * @param device {@link BluetoothDevice} that sent this data
     * @param buttonId Identifier of a specific button on a device
     * @param buttonState Type of the {@code ButtonData}
     */
    public ButtonData(BluetoothDevice device, int buttonId, ButtonState buttonState) {
        super(device, DataType.BUTTON);
        this.buttonId = buttonId;
        this.buttonState = buttonState;
    }

    @Override
    public String toString() {
        return super.toString() + ", Button Event: Button " + buttonId + " " + buttonState.name();
    }

    // Methods for making the class Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(buttonId);
        out.writeSerializable(buttonState);
    }

    private ButtonData(Parcel in) {
        super(in);
        this.buttonId = in.readInt();
        this.buttonState = (ButtonState) in.readSerializable();
    }

    public static final Parcelable.Creator<ButtonData> CREATOR
            = new Parcelable.Creator<ButtonData>() {
        @Override
        public ButtonData createFromParcel(Parcel in) {
            return new ButtonData(in);
        }

        @Override
        public ButtonData[] newArray(int size) {
            return new ButtonData[size];
        }
    };
}
