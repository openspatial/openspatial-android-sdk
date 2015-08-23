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

import java.util.HashMap;
import java.util.Map;

/**
 * Describes all available types of OpenSpatial data.
 */
public enum DataType {
    RAW_ACCELEROMETER(0x20),
    RAW_GYRO(0x21),
    RAW_COMPASS(0x22),
    EULER_ANGLES(0x23),
    TRANSLATIONS(0x24),
    ANALOG(0x25),
    RELATIVE_XY(0x10),
    GESTURE(0xa0),
    SLIDER(0xa1),
    BUTTON(0xa2),
    GENERAL_DEVICE_INFORMATION(0xff);

    private final byte id;
    DataType(int id) { this.id = (byte) id; }

    /**
     * Returns the tag byte used by the {@link OpenSpatialEventFactory} to indicate the start of a
     * data packet of this type.
     * @return The value of the tag byte for this {@code DataType}.
     */
    public byte getValue() { return id; }

    private static Map<Byte, DataType> map = new HashMap<Byte, DataType>();

    static {
        for (DataType type : DataType.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Returns a {@code DataType} that has a tag byte equal to {@code id}.
     * @param id The tag byte value.
     * @return a {@code DataType} value that has a tag value equal to {@code id}
     */
    public static DataType valueOf(byte id) {
        return map.get(id);
    }
}
