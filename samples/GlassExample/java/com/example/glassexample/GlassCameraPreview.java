/*
 * Copyright 2014, Nod Labs.
 */

package com.example.glassexample;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
public class GlassCameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private Camera mCamera;
    private static final String TAG = GlassCameraPreview.class.getSimpleName();

    public GlassCameraPreview(Context context, Camera camera) {
        super(context);

        mCamera = camera;
        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewFpsRange(30000, 30000);
        mCamera.setParameters(params);


        getHolder().addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surface created");
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't set preview display: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }
}
