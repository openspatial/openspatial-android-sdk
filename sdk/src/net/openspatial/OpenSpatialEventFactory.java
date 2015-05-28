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
import net.openspatial.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OpenSpatialEventFactory {
    private static final short SCROLL_OPCODE            = 0x1;
    private static final short DIRECTIONS_OPCODE        = 0x2;

    private static final byte GESTURE_RIGHT             = 0x1;
    private static final byte GESTURE_LEFT              = 0x2;
    private static final byte GESTURE_DOWN              = 0x3;
    private static final byte GESTURE_UP                = 0x4;
    private static final byte GESTURE_CLOCKWISE         = 0x5;
    private static final byte GESTURE_COUNTERCLOCKWISE  = 0x6;

    private static final byte SCROLL_DOWN               = 0x1;
    private static final byte SCROLL_UP                 = 0x2;

    private static final int DEFAULT_MAGNITUDE          = 1;

    private static final int TOUCH0_DOWN                = (0x1);
    private static final int TOUCH0_UP                  = (0x1 << 1);

    private static final int TOUCH1_DOWN                = (0x1 << 2);
    private static final int TOUCH1_UP                  = (0x1 << 3);

    private static final int TOUCH2_DOWN                = (0x1 << 4);
    private static final int TOUCH2_UP                  = (0x1 << 5);

    private static final int TACTILE0_DOWN              = (0x1 << 6);
    private static final int TACTILE0_UP                = (0x1 << 7);

    private static final int TACTILE1_DOWN              = (0x1 << 8);
    private static final int TACTILE1_UP                = (0x1 << 9);

    private static final HashMap<Integer, ButtonEvent.ButtonEventType> BUTTON_EVENT_MAP =
            new HashMap<Integer, ButtonEvent.ButtonEventType>() {{
                put(TOUCH0_DOWN, ButtonEvent.ButtonEventType.TOUCH0_DOWN);
                put(TOUCH0_UP, ButtonEvent.ButtonEventType.TOUCH0_UP);

                put(TOUCH1_DOWN, ButtonEvent.ButtonEventType.TOUCH1_DOWN);
                put(TOUCH1_UP, ButtonEvent.ButtonEventType.TOUCH1_UP);

                put(TOUCH2_DOWN, ButtonEvent.ButtonEventType.TOUCH2_DOWN);
                put(TOUCH2_UP, ButtonEvent.ButtonEventType.TOUCH2_UP);

                put(TACTILE0_DOWN, ButtonEvent.ButtonEventType.TACTILE0_DOWN);
                put(TACTILE0_UP, ButtonEvent.ButtonEventType.TACTILE0_UP);

                put(TACTILE1_DOWN, ButtonEvent.ButtonEventType.TACTILE1_DOWN);
                put(TACTILE1_UP, ButtonEvent.ButtonEventType.TACTILE1_UP);
            }};

    private static final HashMap<Byte, GestureEvent.GestureEventType> GESTURE_DIRECTION_MAP =
            new HashMap<Byte, GestureEvent.GestureEventType>() {{
                put(GESTURE_RIGHT, GestureEvent.GestureEventType.SWIPE_RIGHT);
                put(GESTURE_LEFT, GestureEvent.GestureEventType.SWIPE_LEFT);
                put(GESTURE_UP, GestureEvent.GestureEventType.SWIPE_UP);
                put(GESTURE_DOWN, GestureEvent.GestureEventType.SWIPE_DOWN);
                put(GESTURE_CLOCKWISE, GestureEvent.GestureEventType.CLOCKWISE_ROTATION);
                put(GESTURE_COUNTERCLOCKWISE, GestureEvent.GestureEventType.COUNTERCLOCKWISE_ROTATION);
            }};

    private static final HashMap<Byte, GestureEvent.GestureEventType> GESTURE_SCROLL_MAP =
            new HashMap<Byte, GestureEvent.GestureEventType>() {{
                put(SCROLL_UP, GestureEvent.GestureEventType.SCROLL_UP);
                put(SCROLL_DOWN, GestureEvent.GestureEventType.SCROLL_DOWN);
            }};

    public PointerEvent getPointerEventFromCharacteristic(BluetoothDevice device, byte[] value) {
        ByteBuffer buffer = ByteBuffer.wrap(value);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        short x = buffer.getShort();
        short y = buffer.getShort();

        if (x == 0 && y == 0) {
            return null;
        }

        return new PointerEvent(device, PointerEvent.PointerEventType.RELATIVE, x, y);
    }

    public List<ButtonEvent> getButtonEventsFromCharacteristic(BluetoothDevice device, byte[] bytes) {
        List<ButtonEvent> buttonEvents = new ArrayList<ButtonEvent>();

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        short value = buffer.getShort();

        for (int key : BUTTON_EVENT_MAP.keySet()) {
            if ((value & key) != 0) {
                buttonEvents.add(new ButtonEvent(device, BUTTON_EVENT_MAP.get(key)));
            }
        }

        return buttonEvents;
    }

    private GestureEvent.GestureEventType getScrollGestureType(byte value) {
        return GESTURE_SCROLL_MAP.get(value);
    }

    private GestureEvent.GestureEventType getDirectionGestureType(byte value) {
        return GESTURE_DIRECTION_MAP.get(value);
    }

    public GestureEvent getGestureEventFromCharacteristic(BluetoothDevice device, byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        short opcode = buffer.getShort();
        byte value = buffer.get();

        GestureEvent.GestureEventType type;

        switch (opcode) {
            case SCROLL_OPCODE:
                type = getScrollGestureType(value);
                break;
            case DIRECTIONS_OPCODE:
                type = getDirectionGestureType(value);
                break;
            default:
                type = null;
        }

        if (type == null) {
            return null;
        }

        return new GestureEvent(device, type, DEFAULT_MAGNITUDE);
    }

    float getFloatFromInt16(short codedValue) {
        int intVal = new Short(codedValue).intValue();
        intVal <<= 16;

        return ((float)intVal) / (1 << 29);
    }

    public Pose6DEvent getPose6DEventFromCharacteristic(BluetoothDevice device, byte[] value) {
        ByteBuffer buffer = ByteBuffer.wrap(value);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        short x = buffer.getShort();
        short y = buffer.getShort();
        short z = buffer.getShort();

        short codedRoll = buffer.getShort();
        short codedPitch = buffer.getShort();
        short codedYaw = buffer.getShort();

        return new Pose6DEvent(device,
                x,
                y,
                z,
                getFloatFromInt16(codedRoll),
                getFloatFromInt16(codedPitch),
                getFloatFromInt16(codedYaw));
    }

    float getAccelReadingFromInt16(short value) {
        return ((float)value) / 8192;
    }

    float getGyroReadingFromInt16(short value) {
        return (float)((value / 16.4) * Math.PI / 180);
    }

    public Motion6DEvent getMotion6DEventFromCharacteristic(BluetoothDevice device, byte[] value) {
        ByteBuffer buffer = ByteBuffer.wrap(value);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        short accelX = buffer.getShort();
        short accelY = buffer.getShort();
        short accelZ = buffer.getShort();

        short gyroX = buffer.getShort();
        short gyroY = buffer.getShort();
        short gyroZ = buffer.getShort();

        return new Motion6DEvent(device,
                getAccelReadingFromInt16(accelX),
                getAccelReadingFromInt16(accelY),
                getAccelReadingFromInt16(accelZ),
                getGyroReadingFromInt16(gyroX),
                getGyroReadingFromInt16(gyroY),
                getGyroReadingFromInt16(gyroZ));
    }

    public List<OpenSpatialEvent> getOpenSpatialEventsFromCharacteristic(
            BluetoothDevice device, OpenSpatialEvent.EventType eventType, byte[] value) {
        List<OpenSpatialEvent> result = new ArrayList<OpenSpatialEvent>();
        switch (eventType) {
            case EVENT_BUTTON:
                List<ButtonEvent> buttonEvents = getButtonEventsFromCharacteristic(device, value);
                for(ButtonEvent b : buttonEvents) {
                    result.add(b);
                }
                break;
            case EVENT_GESTURE:
                result.add(getGestureEventFromCharacteristic(device, value));
                break;
            case EVENT_POSE6D:
                result.add(getPose6DEventFromCharacteristic(device, value));
                break;
            case EVENT_MOTION6D:
                result.add(getMotion6DEventFromCharacteristic(device, value));
                break;
            case EVENT_POINTER:
                result.add(getPointerEventFromCharacteristic(device, value));
                break;
        }

        return result;
    }
}
