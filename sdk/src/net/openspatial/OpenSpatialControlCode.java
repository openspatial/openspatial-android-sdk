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
 * Describes the available OpenSpatial control codes.
 */
public enum OpenSpatialControlCode {
    GET_PARAMETER(0x0),
    SET_PARAMETER(0x1),
    GET_ID(0x2),
    GET_PARAMETER_VALUE_RANGE(0x3),
    ENABLE_DATA_STREAM(0x4),
    DISABLE_DATA_STREAM(0x5);

    private final byte id;
    OpenSpatialControlCode(int id) { this.id = (byte) id; }

    /**
     * @return The byte value of the control code.
     */
    public byte getValue() { return id; }

    private static Map<Byte, OpenSpatialControlCode> map
            = new HashMap<Byte, OpenSpatialControlCode>();

    static {
        for (OpenSpatialControlCode code  : OpenSpatialControlCode.values()) {
            map.put(code.id, code);
        }
    }

    /**
     * Returns an {@code OpenSpatialControlCode} that has a byte value equal to {@code id}.
     * @param id The control code byte value.
     * @return a {@code OpenSpatialcontrolCode} value that has a byte value equal to {@code id}
     */
    public static OpenSpatialControlCode valueOf(byte id) {
        return map.get(id);
    }
}
