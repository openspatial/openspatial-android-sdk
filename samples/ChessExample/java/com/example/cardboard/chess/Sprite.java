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

package com.example.cardboard.chess;

import android.opengl.Matrix;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;

public class Sprite {
    private ObjectModel mObjectModel;
    private float[] mModelMatrix;
    private btRigidBody mRigidBody;
    private boolean mKinematic;

    private float[] mLocalXAxis = new float[] {1, 0, 0, 0};
    private float[] mLocalYAxis = new float[] {0, 1, 0, 0};
    private float[] mLocalZAxis = new float[] {0, 0, 1, 0};

    Sprite(ObjectModel obj) {
        mObjectModel = obj;
        mModelMatrix = new float[16];
    }

    float[] getOrigin() {
        return mObjectModel.getOrigin();
    }

    float[] getHalfExtents() {
        return mObjectModel.getHalfExtents();
    }

    private void makeRigidBody(btCollisionShape shape, float mass, boolean kinematic, float[] initialOrientation) {
        btDefaultMotionState motionState = new btDefaultMotionState(new Matrix4(initialOrientation));

        Vector3 inertia = new Vector3();
        shape.calculateLocalInertia(mass, inertia);

        btRigidBody.btRigidBodyConstructionInfo info =
                new btRigidBody.btRigidBodyConstructionInfo(mass, motionState, shape, inertia);
        btRigidBody body = new btRigidBody(info);

        if (kinematic) {
            body.setCollisionFlags(body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
            body.setActivationState(Collision.DISABLE_DEACTIVATION);
        }

        mRigidBody = body;
        mKinematic = kinematic;
    }

    void makeBoxShapeRigidBody(float mass, boolean kinematic, float[] initialOrientation) {
        btCollisionShape shape = new btBoxShape(new Vector3(mObjectModel.getHalfExtents()));

        makeRigidBody(shape, mass, kinematic, initialOrientation);
    }

    btRigidBody getRigidBody() {
        return mRigidBody;
    }

    ObjectModel getObjectModel() {
        return mObjectModel;
    }

    float[] getOrientation() {
        return mRigidBody.getWorldTransform().getValues();
    }

    float getWidth() {
        return Math.abs(mObjectModel.getMaxX() - mObjectModel.getMinX());
    }

    float getHeight() {
        return Math.abs(mObjectModel.getMaxY() - mObjectModel.getMinY());
    }

    float getDepth() {
        return Math.abs(mObjectModel.getMaxZ() - mObjectModel.getMinZ());
    }

    private void transformAxis(float[] axis, float[] matrix) {
        float[] copy = new float[4];

        System.arraycopy(axis, 0, copy, 0, copy.length);

        Matrix.multiplyMV(axis, 0, matrix, 0, copy, 0);
    }

    void rotate(float roll, float pitch, float yaw) {
        rotateAboutPoint(0, 0, 0, roll, pitch, yaw);
    }

    void rotateAboutPoint(float x, float y, float z, float roll, float pitch, float yaw) {
        Matrix4 currentTransform = new Matrix4();
        mRigidBody.getMotionState().getWorldTransform(currentTransform);
        float[] values = currentTransform.getValues();
        float[] transform = new float[16];

        Matrix.setIdentityM(transform, 0);
        Matrix.translateM(transform, 0, x, y, z);
        Matrix.rotateM(transform, 0, pitch, mLocalXAxis[0], mLocalXAxis[1], mLocalXAxis[2]);
        Matrix.rotateM(transform, 0, yaw, mLocalYAxis[0], mLocalYAxis[1], mLocalYAxis[2]);

        // Rotate the axes before applying roll since the ring gives us pitch and yaw
        // regardless of which way is up
        transformAxis(mLocalXAxis, transform);
        transformAxis(mLocalYAxis, transform);
        transformAxis(mLocalZAxis, transform);

        Matrix.rotateM(transform, 0, roll, mLocalZAxis[0], mLocalZAxis[1], mLocalZAxis[2]);
        Matrix.translateM(transform, 0, -x, -y, -z);

        float[] result = new float[16];
        Matrix.multiplyMM(result, 0, transform, 0, values, 0);
        mRigidBody.getMotionState().setWorldTransform(new Matrix4(result));

        if (mKinematic) {
            mRigidBody.setActivationState(Collision.ACTIVE_TAG);
        }
    }

    void resetAxes() {
        mLocalXAxis[0] = 1;
        mLocalXAxis[1] = mLocalXAxis[2] = mLocalXAxis[3] = 0;

        mLocalYAxis[1] = 1;
        mLocalYAxis[0] = mLocalYAxis[2] = mLocalYAxis[3] = 0;

        mLocalZAxis[2] = 1;
        mLocalZAxis[0] = mLocalZAxis[1] = mLocalZAxis[3] = 0;
    }

    void setOrientation(float[] transformMatrix) {
        mRigidBody.getMotionState().setWorldTransform(new Matrix4(transformMatrix));

        transformAxis(mLocalXAxis, transformMatrix);
        transformAxis(mLocalYAxis, transformMatrix);
        transformAxis(mLocalZAxis, transformMatrix);

        if (mKinematic) {
            mRigidBody.setActivationState(Collision.ACTIVE_TAG);
        }
    }
}
