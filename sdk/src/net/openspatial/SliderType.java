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
 * An enumeration of the possible slider values an OpenSpatial device can report
 */
public enum SliderType {
    SLIDE_DOWN(0x1),
    SLIDE_UP(0x2);

    private final byte id;
    SliderType(int id) { this.id = (byte) id; }
    public byte getValue() { return id; }

    private static Map<Byte, SliderType> map = new HashMap<Byte, SliderType>();

    static {
        for (SliderType type : SliderType.values()) {
            map.put(type.id, type);
        }
    }

    public static SliderType valueOf(byte id) {
        return map.get(id);
    }
}
