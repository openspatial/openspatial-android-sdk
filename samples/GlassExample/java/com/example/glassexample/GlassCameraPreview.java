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
