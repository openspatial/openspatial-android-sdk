/*
 * Copyright 2014 Google Inc. All Rights Reserved.

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

import android.annotation.TargetApi;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.google.vrtoolkit.cardboard.*;

import javax.microedition.khronos.egl.EGLConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A Cardboard sample application.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer {

    private static final String TAG = "ChessExample";

    private static final float CAMERA_Z = 0.1f;
    private static final float TIME_DELTA = 0.3f;

    // We keep the light always position just above the user.
    private final float[] mLightPosInWorldSpace = new float[] {0.0f, 2.0f, 0.0f, 1.0f};
    private final float[] mLightPosInEyeSpace = new float[4];

    private static final int COORDS_PER_VERTEX = 3;
    private static float MIN_Z_DISPLACEMENT = -1f;
    private static float FLOOR_DEPTH = -1.0f;
    private static float HAND_DEPTH = -1.0f;

    private static float PIECE_MASS = 0.2f;
    private static float HAND_MASS = 2.5f;

    private int mGlProgram;
    private int mPositionParam;
    private int mNormalParam;
    private int mColorParam;
    private int mModelViewProjectionParam;
    private int mLightPosParam;
    private int mModelViewParam;
    private int mModelParam;

    //private float[] mModelCube;
    private float[] mCamera;
    private float[] mView;
    private float[] mHeadView;
    private float[] mModelViewProjection;
    private float[] mModelView;
    private float[] mPerspective;

    private float[] mModelFloor;

    private Board mBoard;
    private final Map<Integer, Sprite> mIdSpriteMap = new HashMap<Integer, Sprite>();
    private float mXTranslationPerCol;
    private float mZTranslationPerRow;
    private Sprite mHand;

    private btCollisionConfiguration mCollisionConfiguration;
    private btCollisionDispatcher mDispatcher;
    private btBroadphaseInterface mBroadphase;
    private btConstraintSolver mSolver;
    private btDiscreteDynamicsWorld mDynamicsWorld;

    private Timer mPhysicsTimerThread;

    private Map<Sprite, btRigidBody> mSpriteBodyMap = new HashMap<Sprite, btRigidBody>();
    /**
     * Converts a raw text file, saved as a resource, into an OpenGL ES shader
     * @param type The type of shader we will be creating.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return
     */
    private int loadGLShader(int type, int resId) {
        String code = readRawTextFile(resId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     * @param func
     */
    private static void checkGLError(String func) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, func + ": glError " + error);
            throw new RuntimeException(func + ": glError " + error);
        }
    }

    private void addRigidBody(Sprite sprite, btCollisionShape shape, float mass) {
        btDefaultMotionState motionState = new btDefaultMotionState(new Matrix4(sprite.getModelMatrix()));

        Vector3 inertia = new Vector3();
        shape.calculateLocalInertia(mass, inertia);

        btRigidBody.btRigidBodyConstructionInfo info =
                new btRigidBody.btRigidBodyConstructionInfo(mass, motionState, shape, inertia);
        btRigidBody body = new btRigidBody(info);

        mDynamicsWorld.addRigidBody(body);

        if (mass != 0) {
            // Add only if dynamic body since static bodies won't be transformed
            mSpriteBodyMap.put(sprite, body);
        }
    }

    private void addBoxShapeRigidBody(Sprite sprite, float mass) {
        btCollisionShape shape = new btBoxShape(new Vector3(sprite.getHalfExtents()));

        addRigidBody(sprite, shape, mass);
    }

    private void addConvexHullRigidBody(Sprite sprite, float mass) {
        btCollisionShape shape = new btConvexHullShape(sprite.getVertices());

        addRigidBody(sprite, shape, mass);
    }

    private void addPlaneRigidBody(final float[] normal, final float constant) {
        btCollisionShape shape = new btStaticPlaneShape(new Vector3(normal[0], normal[1], normal[2]), constant);

        btDefaultMotionState motionState = new btDefaultMotionState(new Matrix4());

        btRigidBody.btRigidBodyConstructionInfo info =
                new btRigidBody.btRigidBodyConstructionInfo(0, motionState, shape, new Vector3(0, 0, 0));
        btRigidBody body = new btRigidBody(info);

        mDynamicsWorld.addRigidBody(body);
    }

    private Sprite loadSprite(int id, int resource, float[] color) {
        Sprite sprite = new Sprite(this, resource);
        sprite.setColor(color);
        sprite.load();

        mIdSpriteMap.put(id, sprite);

        return sprite;
    }

    private void loadBoard() {
        mXTranslationPerCol = mZTranslationPerRow = 0;

        float zTrans = MIN_Z_DISPLACEMENT;
        for (int row = 0; row < Board.SIZE; ++row) {
            float xTrans = -mXTranslationPerCol * (Board.SIZE + 1) / 2;
            for (int col = 0; col < Board.SIZE; ++col) {
                Board.Square square = mBoard.getSquare(row, col);
                Sprite tile = loadSprite(square.tile.id, R.raw.tile, Constants.getSquareColor(square.tile.color));

                if (mXTranslationPerCol == 0) {
                    mXTranslationPerCol = tile.getMaxX() - tile.getMinX();
                    mZTranslationPerRow = tile.getMaxZ() - tile.getMinZ();
                    xTrans = -mXTranslationPerCol * (Board.SIZE + 1) / 2;
                }

                xTrans += mXTranslationPerCol;

                Matrix.setIdentityM(tile.getModelMatrix(), 0);
                Matrix.translateM(tile.getModelMatrix(), 0, xTrans, FLOOR_DEPTH, zTrans);

                addBoxShapeRigidBody(tile, 0); // Tiles are not dynamic, so 0 mass

                if (square.piece != null) {
                    Sprite piece = loadSprite(square.piece.id,
                            Constants.getResourceIdForPiece(square.piece.type),
                            Constants.getPieceColor(square.piece.color));

                    float[] modelMatrix = piece.getModelMatrix();
                    Matrix.setIdentityM(modelMatrix, 0);
                    Matrix.translateM(modelMatrix,
                            0,
                            xTrans,
                            FLOOR_DEPTH + Math.abs(piece.getMinY()) + Math.abs(tile.getMinY()),
                            zTrans);

                    addBoxShapeRigidBody(piece, PIECE_MASS);
                }
            }

            zTrans -= mZTranslationPerRow;
        }
    }

    private void loadWorld() {
        loadBoard();

        mHand = new Sprite(this, R.raw.hand_withtexture);
        float[] color = {0.4f, 0.4f, 0.4f, 1.0f};
        mHand.setColor(color);
        mHand.load();
        addConvexHullRigidBody(mHand, HAND_MASS);

        mPhysicsTimerThread.scheduleAtFixedRate(new TimerTask() {
            private long lastRun = System.currentTimeMillis();
            @Override
            public void run() {
                long curTime = System.currentTimeMillis();
                mDynamicsWorld.stepSimulation(((float)(curTime - lastRun)) / 1000);
                lastRun = curTime;

                for (Sprite s : mSpriteBodyMap.keySet()) {
                    btRigidBody body = mSpriteBodyMap.get(s);
                    Matrix4 bodyTransform = new Matrix4();
                    body.getMotionState().getWorldTransform(bodyTransform);

                    Vector3 translation = new Vector3();
                    bodyTransform.getTranslation(translation);

                    synchronized (s.getModelMatrix()) {
                        System.arraycopy(bodyTransform.val, 0, s.getModelMatrix(), 0, bodyTransform.val.length);
                    }
                }
            }
        }, 0, 17); // 60Hz
    }

    private void bulletInit() {
        Bullet.init();

        mCollisionConfiguration = new btDefaultCollisionConfiguration();
        mDispatcher = new btCollisionDispatcher(mCollisionConfiguration);
        mBroadphase = new btDbvtBroadphase();
        mSolver = new btSequentialImpulseConstraintSolver();

        mDynamicsWorld = new btDiscreteDynamicsWorld(
                mDispatcher,
                mBroadphase,
                mSolver,
                mCollisionConfiguration);

        // No gravity
        mDynamicsWorld.setGravity(new Vector3(0, -10, 0));
    }

    /**
     * Sets the view to our CardboardView and initializes the transformation matrices we will use
     * to render our scene.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.common_ui);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);

        //mModelCube = new float[16];
        mCamera = new float[16];
        mView = new float[16];
        mModelViewProjection = new float[16];
        mModelView = new float[16];
        mModelFloor = new float[16];
        mHeadView = new float[16];

        mBoard = new Board();
        mBoard.init();

        bulletInit();

        mPhysicsTimerThread = new Timer();
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
    }

    /**
     * Creates the buffers we use to store information about the 3D world. OpenGL doesn't use Java
     * arrays, but rather needs data in a format it can understand. Hence we use ByteBuffers.
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well

        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
        //int gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);

        //int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.simple_vertex);
        int gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.simple_fragment);

        mGlProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mGlProgram, vertexShader);
        GLES20.glAttachShader(mGlProgram, gridShader);
        GLES20.glLinkProgram(mGlProgram);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        checkGLError("onSurfaceCreated");

        loadWorld();
    }

    /**
     * Converts a raw text file into a string.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return
     */
    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        GLES20.glUseProgram(mGlProgram);

        mModelViewProjectionParam = GLES20.glGetUniformLocation(mGlProgram, "u_MVP");
        mLightPosParam = GLES20.glGetUniformLocation(mGlProgram, "u_LightPos");
        mModelViewParam = GLES20.glGetUniformLocation(mGlProgram, "u_MVMatrix");
        mModelParam = GLES20.glGetUniformLocation(mGlProgram, "u_Model");

        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(mCamera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        headTransform.getHeadView(mHeadView, 0);
        checkGLError("onReadyToDraw");
    }

    /**
     * Draws a frame for an eye. The transformation for that eye (from the camera) is passed in as
     * a parameter.
     * @param transform The transformations to apply to render this eye.
     */
    @Override
    public void onDrawEye(EyeTransform transform) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        mPositionParam = GLES20.glGetAttribLocation(mGlProgram, "a_Position");
        mNormalParam = GLES20.glGetAttribLocation(mGlProgram, "a_Normal");
        mColorParam = GLES20.glGetAttribLocation(mGlProgram, "a_Color");

        GLES20.glEnableVertexAttribArray(mPositionParam);
        GLES20.glEnableVertexAttribArray(mNormalParam);
        GLES20.glEnableVertexAttribArray(mColorParam);
        checkGLError("mColorParam");

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(mView, 0, transform.getEyeView(), 0, mCamera, 0);

        // Set the position of the light
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mView, 0, mLightPosInWorldSpace, 0);
        GLES20.glUniform3f(mLightPosParam, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1],
                mLightPosInEyeSpace[2]);

        mPerspective = transform.getPerspective();
        drawWorld();

        Matrix.setIdentityM(mHand.getModelMatrix(), 0);
        Matrix.translateM(mHand.getModelMatrix(), 0, 0, HAND_DEPTH, MIN_Z_DISPLACEMENT * 2);
        drawObject(mHand);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    /**
     * Draw the cube. We've set all of our transformation matrices. Now we simply pass them into
     * the shader.
     */
    public void drawObject(Sprite sprite) {
        float[] modelMatrix = new float[16];

        synchronized (sprite.getModelMatrix()) {
            System.arraycopy(sprite.getModelMatrix(), 0, modelMatrix, 0, modelMatrix.length);
        }

        // Set the Model in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(mModelParam, 1, false, modelMatrix, 0);

        // Set the ModelView in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(mModelViewParam, 1, false, mModelView, 0);

        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        Matrix.multiplyMM(mModelView, 0, mView, 0, modelMatrix, 0);
        Matrix.multiplyMM(mModelViewProjection, 0, mPerspective, 0, mModelView, 0);

        // Set the position of the cube
        GLES20.glVertexAttribPointer(mPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, sprite.getVertices());

        // Set the ModelViewProjection matrix in the shader.
        GLES20.glUniformMatrix4fv(mModelViewProjectionParam, 1, false, mModelViewProjection, 0);

        // Set the normal positions of the cube, again for shading
        GLES20.glVertexAttribPointer(mNormalParam, 3, GLES20.GL_FLOAT,
                false, 0, sprite.getNormals());

        GLES20.glVertexAttribPointer(mColorParam, 4, GLES20.GL_FLOAT, false,
                0, sprite.getColors());

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, sprite.getNumFaces() * 3);
        checkGLError("Drawing cube");
    }

    private void drawWorld() {
        for (int id : mIdSpriteMap.keySet()) {
            drawObject(mIdSpriteMap.get(id));
        }
    }
}
