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
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
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
import net.openspatial.*;

import javax.microedition.khronos.egl.EGLConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A Cardboard sample application.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer {

    private static final String TAG = "ChessExample";

    private static final float CAMERA_Z = 0.1f;

    // We keep the light always position just above the user.
    private final float[] mLightPosInWorldSpace = new float[] {0.0f, 2.0f, 0.0f, 1.0f};
    private final float[] mLightPosInEyeSpace = new float[4];

    private static final int COORDS_PER_VERTEX = 3;
    private static float MIN_Z_DISPLACEMENT = -1f;
    private static float FLOOR_DEPTH = -1.0f;
    private static float HAND_DEPTH = -0.5f;

    private static float PIECE_MASS = 0.2f;
    private static float HAND_MASS = 2.5f;

    private static final int EVENTS_PER_ITERATION = 10;

    private int mGlProgram;
    private int mPositionParam;
    private int mNormalParam;
    private int mColorParam;
    private int mModelViewProjectionParam;
    private int mLightPosParam;
    private int mModelViewParam;
    private int mModelParam;

    private float[] mCamera;
    private float[] mView;
    private float[] mHeadView;
    private float[] mModelViewProjection;
    private float[] mModelView;
    private float[] mPerspective;

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

    private float mHandXTranslation;
    private float mHandYTranslation;

    private Timer mPhysicsTimerThread;

    private final Map<Sprite, btRigidBody> mSpriteBodyMap = new HashMap<Sprite, btRigidBody>();
    private final Queue<Pose6DEvent> mHandTransforms = new ConcurrentLinkedQueue<Pose6DEvent>();

    private float[] mRingXAxis = new float[] {1, 0, 0, 0};
    private float[] mRingYAxis = new float[] {0, 1, 0, 0};
    private float[] mRingZAxis = new float[] {0, 0, 1, 0};

    private boolean mStart = false;

    OpenSpatialService mOpenSpatialService;
    private ServiceConnection mOpenSpatialServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mOpenSpatialService = ((OpenSpatialService.OpenSpatialServiceBinder)service).getService();

            mOpenSpatialService.initialize(TAG, new OpenSpatialService.OpenSpatialServiceCallback() {
                @Override
                public void deviceConnected(BluetoothDevice device) {
                    try {
                        mOpenSpatialService.registerForPose6DEvents(device, new OpenSpatialEvent.EventListener() {
                            @Override
                            public void onEventReceived(OpenSpatialEvent event) {
                                if (!mStart) {
                                    return;
                                }

                                mHandTransforms.add((Pose6DEvent) event);
                            }
                        });

                        mOpenSpatialService.registerForButtonEvents(device, new OpenSpatialEvent.EventListener() {
                            @Override
                            public void onEventReceived(OpenSpatialEvent event) {
                                ButtonEvent bEvent = (ButtonEvent)event;

                                if (bEvent.buttonEventType == ButtonEvent.ButtonEventType.TOUCH2_DOWN) {
                                    mStart = true;
                                } else if (bEvent.buttonEventType == ButtonEvent.ButtonEventType.TOUCH0_DOWN) {
                                    recenterHand();
                                }
                            }
                        });

                    } catch (OpenSpatialException e) {
                        Log.e(TAG, "Error registering for Pose6DEvent " + e);
                    }
                }

                @Override
                public void buttonEventRegistrationResult(BluetoothDevice device, int i) {
                }

                @Override
                public void pointerEventRegistrationResult(BluetoothDevice device, int i) {
                }

                @Override
                public void pose6DEventRegistrationResult(BluetoothDevice device, int i) {

                }

                @Override
                public void gestureEventRegistrationResult(BluetoothDevice device, int i) {
                }
            });

            mOpenSpatialService.getConnectedDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mOpenSpatialService = null;
        }
    };

    private float getDegrees(float radians) {
        return radians * 180 / (float)Math.PI;
    }

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

    private void addRigidBody(Sprite sprite, btCollisionShape shape, float mass, boolean kinematic) {
        btDefaultMotionState motionState = new btDefaultMotionState(new Matrix4(sprite.getModelMatrix()));

        Vector3 inertia = new Vector3();
        shape.calculateLocalInertia(mass, inertia);

        btRigidBody.btRigidBodyConstructionInfo info =
                new btRigidBody.btRigidBodyConstructionInfo(mass, motionState, shape, inertia);
        btRigidBody body = new btRigidBody(info);

        if (kinematic) {
            body.setCollisionFlags(body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
            body.setActivationState(Collision.DISABLE_DEACTIVATION);
        }

        mDynamicsWorld.addRigidBody(body);

        if (mass != 0) {
            // Add only if dynamic body since static bodies won't be transformed
            mSpriteBodyMap.put(sprite, body);
        }
    }

    private void addBoxShapeRigidBody(Sprite sprite, float mass, boolean kinematic) {
        btCollisionShape shape = new btBoxShape(new Vector3(sprite.getHalfExtents()));

        addRigidBody(sprite, shape, mass, kinematic);
    }

    private void addConvexHullRigidBody(Sprite sprite, float mass, boolean kinematic) {
        btCollisionShape shape = new btConvexHullShape(sprite.getVertices());

        addRigidBody(sprite, shape, mass, kinematic);
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

                float[] modelMatrix = tile.getModelMatrix();
                Matrix.setIdentityM(modelMatrix, 0);
                Matrix.translateM(modelMatrix,
                        0,
                        xTrans,
                        FLOOR_DEPTH,
                        zTrans);
                tile.setModelMatrix(modelMatrix);

                addBoxShapeRigidBody(tile, 0, false); // Tiles are not dynamic, so 0 mass

                if (square.piece != null) {
                    Sprite piece = loadSprite(square.piece.id,
                            Constants.getResourceIdForPiece(square.piece.type),
                            Constants.getPieceColor(square.piece.color));

                    modelMatrix = piece.getModelMatrix();
                    float[] halfExtents = piece.getHalfExtents();
                    Matrix.setIdentityM(modelMatrix, 0);
                    Matrix.translateM(modelMatrix,
                            0,
                            xTrans,
                            FLOOR_DEPTH + halfExtents[1],
                            zTrans);

                    piece.setModelMatrix(modelMatrix);
                    addBoxShapeRigidBody(piece, PIECE_MASS, false);
                }
            }

            zTrans -= mZTranslationPerRow;
        }

        addPlaneRigidBody(new float[]{0, 1, 0}, FLOOR_DEPTH);
    }

    private float[] getInitialHandPositionMatrix() {
        float[] origin = mHand.getOrigin();
        float[] modelMatrix = mHand.getModelMatrix();
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix,
                0,
                mHandXTranslation,
                mHandYTranslation,
                MIN_Z_DISPLACEMENT - origin[2]);

        return modelMatrix;
    }

    private void loadHand() {
        mHand = loadSprite(77, R.raw.hand_withtexture, new float[]{0.4f, 0.4f, 0.4f, 1.0f});
        float[] origin = mHand.getOrigin();

        mHandXTranslation = 0 - origin[0];
        mHandYTranslation = HAND_DEPTH - origin[1];

        float[] modelMatrix = getInitialHandPositionMatrix();
        mHand.setModelMatrix(modelMatrix);
        addBoxShapeRigidBody(mHand, HAND_MASS, true);
    }

    private void transformAxis(float[] axis, float[] matrix) {
        float[] copy = new float[4];

        System.arraycopy(axis, 0, copy, 0, copy.length);

        Matrix.multiplyMV(axis, 0, matrix, 0, copy, 0);
    }

    private void recenterHand() {
        float[] matrix = getInitialHandPositionMatrix();

        btRigidBody body = mSpriteBodyMap.get(mHand);
        synchronized (mHand) {
            // reset axis
            mRingXAxis[0] = 1;
            mRingXAxis[1] = mRingXAxis[2] = mRingXAxis[3] = 0;

            mRingYAxis[1] = 1;
            mRingYAxis[0] = mRingYAxis[2] = mRingYAxis[3] = 0;

            mRingZAxis[2] = 1;
            mRingZAxis[0] = mRingZAxis[1] = mRingZAxis[3] = 0;

            // Clear transforms
            mHandTransforms.clear();

            body.getMotionState().setWorldTransform(new Matrix4(matrix));
            body.setActivationState(Collision.ACTIVE_TAG);
        }
    }

    private void moveHand(Pose6DEvent event) {
        EulerAngle eulerAngle = event.getEulerAngle();
        float xrot = -getDegrees((float)eulerAngle.pitch);
        float yrot = getDegrees((float)eulerAngle.yaw);
        float zrot = -getDegrees((float)eulerAngle.roll);

        btRigidBody body = mSpriteBodyMap.get(mHand);
        body.getWorldTransform();
        Matrix4 currentTransform = new Matrix4();
        body.getMotionState().getWorldTransform(currentTransform);
        float[] values = currentTransform.getValues();
        float[] transform = new float[16];

        Matrix.setIdentityM(transform, 0);
        Matrix.translateM(transform, 0, mHandXTranslation, mHandYTranslation, 0);
        Matrix.rotateM(transform, 0, xrot, mRingXAxis[0], mRingXAxis[1], mRingXAxis[2]);
        Matrix.rotateM(transform, 0, yrot, mRingYAxis[0], mRingYAxis[1], mRingYAxis[2]);

        // Rotate the axes before applying roll since the ring gives us pitch and yaw
        // regardless of which way is up
        transformAxis(mRingXAxis, transform);
        transformAxis(mRingYAxis, transform);
        transformAxis(mRingZAxis, transform);

        Matrix.rotateM(transform, 0, zrot, mRingZAxis[0], mRingZAxis[1], mRingZAxis[2]);
        Matrix.translateM(transform, 0, -mHandXTranslation, -mHandYTranslation, 0);

        float[] result = new float[16];
        Matrix.multiplyMM(result, 0, transform, 0, values, 0);
        body.getMotionState().setWorldTransform(new Matrix4(result));
        body.setActivationState(Collision.ACTIVE_TAG);
    }

    private void processHandUpdates() {
        int numProcessed = 0;

        while (numProcessed++ < EVENTS_PER_ITERATION && !mHandTransforms.isEmpty()) {
            Pose6DEvent event = mHandTransforms.remove();
            moveHand(event);
        }
    }

    private void loadWorld() {
        loadBoard();
        loadHand();

        mPhysicsTimerThread.scheduleAtFixedRate(new TimerTask() {
            private long lastRun = System.currentTimeMillis();

            @Override
            public void run() {
                long curTime = System.currentTimeMillis();

                mDynamicsWorld.stepSimulation(((float) (curTime - lastRun)) / 1000);
                lastRun = curTime;

                for (Sprite s : mSpriteBodyMap.keySet()) {
                    btRigidBody body = mSpriteBodyMap.get(s);
                    Matrix4 bodyTransform = new Matrix4();
                    body.getMotionState().getWorldTransform(bodyTransform);

                    synchronized (s) {
                        s.setModelMatrix(bodyTransform.getValues());
                    }
                }

                // Process any hand updates
                processHandUpdates();
            }
        }, 2000, 17); // 60Hz
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

        mCamera = new float[16];
        mView = new float[16];
        mModelViewProjection = new float[16];
        mModelView = new float[16];
        mHeadView = new float[16];

        mBoard = new Board();
        mBoard.init();

        bulletInit();

        mPhysicsTimerThread = new Timer();

        bindService(new Intent(this, OpenSpatialService.class), mOpenSpatialServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mOpenSpatialServiceConnection);
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
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    /**
     * Draw the cube. We've set all of our transformation matrices. Now we simply pass them into
     * the shader.
     */
    public void drawObject(Sprite sprite) {
        float[] modelMatrix;

        synchronized (sprite) {
            modelMatrix = sprite.getModelMatrix();
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
