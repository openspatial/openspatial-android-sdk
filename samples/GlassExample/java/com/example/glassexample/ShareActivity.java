/*
 * Copyright 2014, Nod Labs.
 */

package com.example.glassexample;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;
import net.openspatial.*;

public class ShareActivity extends Activity {
    private boolean mShouldShare = true;
    private Uri mPicUri;

    private OpenSpatialSingleUserService mOpenSpatialService;
    BluetoothDevice mDevice;

    private static final String TAG = "ShareActivity";

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ButtonEvent event = intent.getParcelableExtra(Constants.EXTRA_OPENSPATIAL_EVENT);
            handleButtonEvent(event);
        }
    };

    private void share() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, mPicUri);
        startActivity(Intent.createChooser(shareIntent, "How do you want to share?"));
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.share_layout);

        Intent intent = getIntent();
        mPicUri = intent.getParcelableExtra(Constants.SHARE_PIC_EXTRA);
        mDevice = intent.getParcelableExtra(Constants.EXTRA_DEVICE);

        ImageView imageView = (ImageView)findViewById(R.id.img_view);
        imageView.setImageURI(mPicUri);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_BUTTON_EVENT);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putParcelable(Constants.EXTRA_DEVICE, mDevice);
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(mReceiver);
    }

    private void handleButtonEvent(ButtonEvent event) {
        if (mShouldShare) {
            mShouldShare = false;
            share();
        }
    }
}
