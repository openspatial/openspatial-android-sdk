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
 * This class represents a rotation in 3D space as a triplet of axes angles. The rotation axis used is x-y-z. The
 * coordinate orientation is right handed with z facing up. All angles are in radians.
 * @see <a href="http://en.wikipedia.org/wiki/Euler_angles">Euler Angles</a>
 *
 * @deprecated use {@link EulerData} instead.
 */

@Deprecated
public class EulerAngle {
    /**
     * Create an Euler angle with {@code roll}, {@code pitch} and {@code yaw} initialized to 0
     */
    public EulerAngle() {
        roll = pitch = yaw = 0.0;
    }

    /**
     * Create an Euler angle with the specified {@code roll}, {@code pitch} and {@code yaw} values
     * @param r The roll value
     * @param p The pitch value
     * @param y The yaw value
     */
    public EulerAngle(double r, double p, double y) {
        roll = r;
        pitch = p;
        yaw = y;
    }

    /**
     * Rotation about the y axis in radians
     */
    public double roll;

    /**
     * Rotation about the x axis in radians
     */
    public double pitch;

    /**
     * Rotation about the z axis in radians
     */
    public double yaw;
}
