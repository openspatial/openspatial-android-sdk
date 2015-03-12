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

/**
 * This class represents a rotation in 3D space as a quaternion.
 * @see <a href="http://en.wikipedia.org/wiki/Quaternion">Quaternion</a>
 */
public class Quaternion {
    /**
     * x value
     */
    public double x;

    /**
     * y value
     */
    public double y;

    /**
     * z value
     */
    public double z;

    /**
     * w value
     */
    public double w;

    /**
     * Create a quaternion with {@code x}, {@code y}, {@code z} and {@code w} initialized to 0
     */
    public Quaternion() {
        x = y = z = w = 0;
    }

    /**
     * Create a quaternion with the specified {@code x}, {@code y}, {@code z}, {@code w} values
     * @param x The x value
     * @param y The y value
     * @param z The z value
     * @param w The w value
     */
    public Quaternion(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
}
