/*
 * Copyright (C) 2014 Nod Labs
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

import android.os.Parcel;

/**
 * A {@code ButtonEvent} represents an instance of one of the touch sensitive surfaces or buttons on the device being
 * touched/pressed. A touch or button press produces the corresponding {@code _DOWN} event. The release produces the
 * corresponding {@code _UP} event.
 *
 */
public class ButtonEvent extends OpenSpatialEvent {
    /**
     * Various types of {@code _UP} and {@code _DOWN} events
     */
    public enum ButtonEventType {
        /**
         * TOUCH1 was touched
         */
        TOUCH1_DOWN,

        /**
         * TOUCH1 was released
         */
        TOUCH1_UP,

        /**
         * TOUCH2 was touched
         */
        TOUCH2_DOWN,

        /**
         * TOUCH2 was released
         */
        TOUCH2_UP,

        /**
         * TACTILE1 was pressed
         */
        TACTILE1_DOWN,

        /**
         * TACTILE1 was released
         */
        TACTILE1_UP,

        /**
         * TACTILE2 was pressed
         */
        TACTILE2_DOWN,

        /**
         * TACTILE2 was released
         */
        TACTILE2_UP,

        /**
         * Scroll down action was detected on the slider
         */
        SCROLL_DOWN,

        /**
         * Scroll up action was detected on the slider
         */
        SCROLL_UP,

        /**
         * The slider was touched. This is an instance of a touch and not a scroll. In the case of a scroll, no TOUCH
         * will be generated and vice versa
         */
        SCROLL_TOUCH_DOWN,

        /**
         * The slider was released
         */
        SCROLL_TOUCH_UP,
    }

    /**
     * Button event type
     */
    public ButtonEventType buttonEventType;

    /**
     * Create a new {@code ButtonEvent} of the specified type
     * @param type Type of the {@code ButtonEvent}
     */
    public ButtonEvent(ButtonEventType type) {
        super(EventType.EVENT_BUTTON);
        buttonEventType = type;
    }

    // Methods for making the class Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeSerializable(buttonEventType);
    }

    private ButtonEvent(Parcel in) {
        super(in);
        this.buttonEventType = (ButtonEventType)in.readSerializable();
    }

    public static final Creator<ButtonEvent> CREATOR = new Creator<ButtonEvent>() {
        @Override
        public ButtonEvent createFromParcel(Parcel in) {
            return new ButtonEvent(in);
        }

        @Override
        public ButtonEvent[] newArray(int size) {
            return new ButtonEvent[size];
        }
    };
}
