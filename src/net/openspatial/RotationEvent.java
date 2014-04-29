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

import android.os.Parcel;

/**
 * This event represents a rotation in 3D space. The rotation can be obtained as a quaternion, as Euler angles or as
 * a rotation matrix.
 *
 * @see net.openspatial.Quaternion
 * @see net.openspatial.EulerAngle
 * @see <a href="http://en.wikipedia.org/wiki/Rotation_matrix#In_three_dimensions">Rotation Matrix</a>
 */
public class RotationEvent extends OpenSpatialEvent {
    private Quaternion mQuaternion;

    /**
     * Construct an instance of RotationEvent based on a quaternion
     * @param quaternion The quaternion representing this rotation
     */
    public RotationEvent(Quaternion quaternion) {
        super(EventType.EVENT_3D_ROTATION);
        mQuaternion = quaternion;
    }

    /**
     * Return the rotation as a Quaternion
     * @return The Quaternion representing the rotation
     */
    public Quaternion getQuaternion() {
        return mQuaternion;
    }

    /**
     * Get the rotation as a rotation matrix
     * @return A 3x3 rotation matrix representing the rotation
     */
    public double[][] getRotationMatrix() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the rotation as a Euler angle
     * @return The EulerAngle representing the rotation
     */
    public EulerAngle getEulerAngle() {
        throw new UnsupportedOperationException();
    }

    // Methods to make this class Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeDouble(mQuaternion.x);
        out.writeDouble(mQuaternion.y);
        out.writeDouble(mQuaternion.z);
        out.writeDouble(mQuaternion.w);
    }

    private RotationEvent(Parcel in) {
        super(in);
        mQuaternion = new Quaternion();
        mQuaternion.x = in.readDouble();
        mQuaternion.y = in.readDouble();
        mQuaternion.z = in.readDouble();
        mQuaternion.w = in.readDouble();
    }

    public static final Creator<RotationEvent> CREATOR = new Creator<RotationEvent>() {
        @Override
        public RotationEvent createFromParcel(Parcel in) {
            return new RotationEvent(in);
        }

        @Override
        public RotationEvent[] newArray(int size) {
            return new RotationEvent[size];
        }
    };
}
