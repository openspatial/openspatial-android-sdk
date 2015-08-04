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

/**
 * This event is used to describe vendor specific or proprietary extensions to the OpenSpatial API
 */
@Deprecated
public class ExtendedEvent extends OpenSpatialEvent {
    /**
     * The identifier used to specify what extended event took place
     */
    public int eventId;

    /**
     * A text value used to indicate the category of event to which this event belongs
     */
    public String category;

    /**
     * Construct an ExtendedEvent based on proprietary or vendor specific output
     */
    public ExtendedEvent(BluetoothDevice device, int eventId, String category) {
        super(device, EventType.EVENT_EXTENDED);

        this.eventId = eventId;
        this.category = category;
    }

    @Override
    public String toString() {
        return super.toString() + ", eventId: " + this.eventId + ", category: " + this.category;
    }

    // Methods to make this class Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(eventId);
        out.writeString(category);
    }

    private ExtendedEvent(Parcel in) {
        super(in);
        eventId = in.readInt();
        category = in.readString();
    }

    public static final Creator<ExtendedEvent> CREATOR = new Creator<ExtendedEvent>() {
        @Override
        public ExtendedEvent createFromParcel(Parcel in) {
            return new ExtendedEvent(in);
        }

        @Override
        public ExtendedEvent[] newArray(int size) {
            return new ExtendedEvent[size];
        }
    };
}
