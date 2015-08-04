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
 * Describes the set of available gestures.
 */
public enum GestureType {
    GESTURE_RIGHT(0x1),
    GESTURE_LEFT(0x2),
    GESTURE_DOWN(0x3),
    GESTURE_UP(0x4),
    GESTURE_CLOCKWISE(0x5),
    GESTURE_COUNTERCLOCKWISE(0x6);

    private final byte id;

    GestureType(int id) { this.id = (byte) id; }

    /**
     * Provides a byte corresponding to the representation of the gesture as defined by the
     * OpenSpatial specification.
     *
     * @return A byte corresponding to a gesture's byte representation.
     */
    public byte getValue() { return id; }

    private static Map<Byte, GestureType> map = new HashMap<Byte, GestureType>();

    static {
        for (GestureType type : GestureType.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Returns a {@code GestureType} corresponding to the id provided or null if no match exists.
     * @param id The byte corresponding to a {@code GestureType} id.
     * @return The {@code GestureType} corresponding to the {@code id} provided, or null if there
     * isn't one.
     */
    public static GestureType valueOf(byte id) {
        return map.get(id);
    }
}
