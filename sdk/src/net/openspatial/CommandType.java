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
 * Possible commands that can be issued to OpenSpatial devices.
 */
public enum CommandType {
    /**
     * Get the value of a parameter from an OpenSpatial device.
     */
    GET_PARAMETER(0x0),

    /**
     * Set the value of a parameter from an OpenSpatial device.
     */
    SET_PARAMETER(0x1),

    /**
     * Get the identifier (usually a human readable string) for a component of an OpenSpatial
     * device.
     */
    GET_IDENTIFIER(0x2),

    /**
     * Get the range of possible values for an OpenSpatial device's parameters.
     */
    GET_PARAMETER_RANGE(0x3),

    /**
     * Enable reporting of a given {@link DataType} from an OpenSpatial device.
     */
    ENABLE(0x4),

    /**
     * Disable reporting of a given {@link DataType} from an OpenSpatial device.
     */
    DISABLE(0x5);

    private final byte id;

    CommandType(int id) { this.id = (byte) id; }

    /**
     * Returns the byte level representation of the {@code CommandType}.
     * @return The byte representation of this {@code CommandType}.
     */
    protected byte getValue() { return id; }

    private static Map<Byte, CommandType> map = new HashMap<Byte, CommandType>();

    static {
        for (CommandType type : CommandType.values()) {
            map.put(type.id, type);
        }
    }

    /**
     * Returns a {@code CommandType} that has a byte representation equal to {@code id}.
     * @param id The tag byte value.
     * @return a {@code CommandType} value that has a tag value equal to {@code id}
     */
    protected static CommandType valueOf(byte id) {
        return map.get(id);
    }
}
