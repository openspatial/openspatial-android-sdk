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

/**
 * This class represents a rotation in 3D space as a triplet of axes angles. The rotation axis used is x-y-z. The
 * coordinate orientation is right handed with z facing up. All angles are in radians.
 * @see <a href="http://en.wikipedia.org/wiki/Euler_angles">Euler Angles</a>
 */
public class EulerAngle {
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
