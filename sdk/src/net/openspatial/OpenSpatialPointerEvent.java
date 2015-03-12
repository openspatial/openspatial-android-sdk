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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OpenSpatialPointerEvent {
    public static final String TAG = OpenSpatialPointerEvent.class.getSimpleName();

    public static final int LEFT_BUTTON_PRESSED = 0x2;
    public static final int RIGHT_BUTTON_PRESSED = 0x1;
    public static final int SLIDER_BUTTON_PRESSED = 0x4;
    public static final int TACTILE_BUTTON1_PRESSED = 0x10;
    public static final int TACTILE_BUTTON2_PRESSED = 0x01;

    // X coordinate
    public short x;

    // Y Coordinate
    public short y;

    // Button pressed state - a bit field with the OR'ed values of
    // LEFT_BUTTON_PRESSED and RIGHT_BUTTON_PRESSED
    public int buttonState;

    public int scrollState;

    // Bit field w/ OR'ed values of TACTILE_BUTTON1_PRESSED and
    // TACTILE_BUTTON2_PRESSED
    public int tactileButtonState;

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(2 + 2 + 1 + 1);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putShort(x);
        buffer.putShort(y);
        int touchScroll = ((scrollState & 0x0F) << 4) | (buttonState & 0x0F);

        buffer.put((byte)(touchScroll & 0xff));
        buffer.put((byte)(tactileButtonState & 0xff));

        return buffer.array();
    }

    public static OpenSpatialPointerEvent fromBytes(byte[] value) {
        OpenSpatialPointerEvent event = new OpenSpatialPointerEvent();
        ByteBuffer data = ByteBuffer.wrap(value);
        data.order(ByteOrder.LITTLE_ENDIAN);

        event.x = data.getShort();
        event.y = data.getShort();
        int touchScroll = data.get() & 0xFF;
        event.buttonState = touchScroll & 0x0F;
        event.scrollState = touchScroll & 0xF0;
        event.tactileButtonState = data.get();

        return event;
    }
}
