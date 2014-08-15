/*
 * Copyright 2014, Nod Labs.
 */

package com.example.glassexample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;
import net.openspatial.ButtonEvent;
import net.openspatial.OpenSpatialEvent;
import net.openspatial.OpenSpatialException;
import net.openspatial.OpenSpatialSingleUserService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CameraPreviewActivity extends Activity {
    private FrameLayout mFrameLayout;
    private GlassCameraPreview mCameraPreview;
    private Camera mCamera;
    private boolean mCanTakePicture = false;
    BluetoothDevice mDevice;

    private static final String TAG = "CameraPreviewActivity";

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ButtonEvent event = intent.getParcelableExtra(Constants.EXTRA_OPENSPATIAL_EVENT);
            handleButtonEvent(event);
        }
    };

    private final Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            Log.d(TAG, "Pic taken!");
            try {
                Uri picFileUri = saveFile(data);
                Intent intent = new Intent(CameraPreviewActivity.this, ImagePreviewActivity.class);
                intent.putExtra(Constants.BITMAP_URI_EXTRA, picFileUri);
                intent.putExtra(Constants.EXTRA_DEVICE, mDevice);
                Log.d(TAG, "Saved file at " + picFileUri.toString());
                startActivity(intent);
            } catch (Exception e) {
                Log.d(TAG, "Error saving picture: " + e.getMessage());
            }
        }
    };

    private Uri saveFile(byte[] data) throws IOException {
        File picDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File picFile = new File(picDir.getPath() + File.separator + "IMG_" + System.currentTimeMillis() + ".jpg");

        FileOutputStream outputStream = new FileOutputStream(picFile);
        outputStream.write(data);
        outputStream.close();

        return Uri.fromFile(picFile);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        Intent intent = getIntent();
        mDevice = intent.getParcelableExtra(Constants.EXTRA_DEVICE);

        if (mDevice == null) {
            mDevice = savedInstance.getParcelable(Constants.EXTRA_DEVICE);
        }

        setContentView(R.layout.main);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Camera not available");
            return;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mCamera = Camera.open();
        mCameraPreview = new GlassCameraPreview(this, mCamera);
        mFrameLayout = (FrameLayout)findViewById(R.id.toplevel_container);
        mFrameLayout.addView(mCameraPreview, 0);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCanTakePicture = true;

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_BUTTON_EVENT);
        registerReceiver(mReceiver, filter);

        Intent intent = new Intent(getApplicationContext(), ReceiverService.class);
        intent.setAction(Constants.ACTION_START_SERVICE);
        intent.putExtra(Constants.EXTRA_DEVICE, mDevice);
        startService(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putParcelable(Constants.EXTRA_DEVICE, mDevice);
    }

    public void onPause() {
        super.onPause();

        unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCamera.release();
    }

    private void handleButtonEvent(ButtonEvent event) {
        Log.d(TAG, "Got button event of type " + event.buttonEventType);
        if (event.buttonEventType == ButtonEvent.ButtonEventType.TOUCH2_DOWN && mCanTakePicture) {
            mCanTakePicture = false;
            mCamera.takePicture(null, null, mPictureCallback);
        }
    }
}
