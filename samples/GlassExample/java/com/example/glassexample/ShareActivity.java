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
