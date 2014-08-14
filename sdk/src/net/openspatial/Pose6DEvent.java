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

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import java.lang.Math;

/**
 * This event represents a transformation in 3D space as rotation and translation deltas across the three axes.
 * The rotation can be obtained as a quaternion, as Euler angles or as a rotation matrix.
 * TODO: Write a note about ring axes
 *
 * @see net.openspatial.Quaternion
 * @see net.openspatial.EulerAngle
 * @see <a href="http://en.wikipedia.org/wiki/Rotation_matrix#In_three_dimensions">Rotation Matrix</a>
 */
public class Pose6DEvent extends OpenSpatialEvent {
    /**
     * The delta translation along the X axis
     */
    public int x;

    /**
     * The delta translation along the Y axis
     */
    public int y;

    /**
     * The delta translation about the Z axis
     */
    public int z;

    /**
     * The delta rotation about the Y axis
     */
    public float roll;

    /**
     * The delta rotation about the X axis
     */
    public float pitch;

    /**
     * The delta rotation about the Z axis
     */
    public float yaw;


    private EulerAngle mEulerAngle;
    private Quaternion mQuaternion;

    /**
     * Construct a Pose6DEvent based on x, y, z, roll, pitch, yaw
     */
    public Pose6DEvent(BluetoothDevice device, int x, int y, int z, float roll, float pitch, float yaw) {
        super(device, EventType.EVENT_POSE6D);

        this.x = x;
        this.y = y;
        this.z = z;
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;

        mEulerAngle = null;
        mQuaternion = null;
    }

    /**
     * Return the rotation as a Quaternion
     * @return The Quaternion representing the rotation
     */
    public Quaternion getQuaternion() {
        if (mQuaternion == null) {
            double sinHalfYaw = Math.sin(yaw/2);
            double cosHalfYaw = Math.cos(yaw/2);
            double sinHalfPitch = Math.sin(pitch/2);
            double cosHalfPitch = Math.cos(pitch/2);
            double sinHalfRoll = Math.sin(roll/2);
            double cosHalfRoll = Math.cos(roll/2);

            double qX = - cosHalfRoll * sinHalfPitch * sinHalfYaw + cosHalfPitch * cosHalfYaw * sinHalfRoll;
            double qY = cosHalfRoll * cosHalfYaw * sinHalfPitch + sinHalfRoll * cosHalfPitch * sinHalfYaw;
            double qZ = cosHalfRoll * cosHalfPitch * sinHalfYaw - sinHalfRoll * cosHalfYaw * sinHalfPitch;
            double qW = cosHalfRoll * cosHalfPitch * cosHalfYaw + sinHalfRoll * sinHalfPitch * sinHalfYaw;

            mQuaternion = new Quaternion(qX, qY, qZ, qW);
        }

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
        if (mEulerAngle == null) {
            mEulerAngle = new EulerAngle(roll, pitch, yaw);
        }

        return mEulerAngle;
    }

    // Methods to make this class Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(z);

        out.writeFloat(roll);
        out.writeFloat(pitch);
        out.writeFloat(yaw);
    }

    private Pose6DEvent(Parcel in) {
        super(in);
        x = in.readInt();
        y = in.readInt();
        z = in.readInt();

        roll = in.readFloat();
        pitch = in.readFloat();
        yaw = in.readFloat();
    }

    public static final Creator<Pose6DEvent> CREATOR = new Creator<Pose6DEvent>() {
        @Override
        public Pose6DEvent createFromParcel(Parcel in) {
            return new Pose6DEvent(in);
        }

        @Override
        public Pose6DEvent[] newArray(int size) {
            return new Pose6DEvent[size];
        }
    };
}
