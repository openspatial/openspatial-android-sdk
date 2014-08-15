/**
 * Copyright 2014, Nod Labs
 */

package com.example.glassexample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.ConcurrentHashMap;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DeviceScanCardActivity extends Activity {
    private boolean mScanning = false;

    ProgressBar mProgressBar;
    TextView mScanTextView;
    BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;

    private final ConcurrentHashMap<BluetoothDevice, Integer> mDeviceRssiMap =
            new ConcurrentHashMap<BluetoothDevice, Integer>();

    private static final String TAG = "DeviceScanCardActivity";

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            mDeviceRssiMap.put(device, rssi);
        }
    };

    private void showDevices() {
        Intent intent = new Intent(DeviceScanCardActivity.this, DeviceScrollCardActivity.class);
        Parcelable[] parcelables = new Parcelable[mDeviceRssiMap.size()];
        int i = 0;
        for (BluetoothDevice device : mDeviceRssiMap.keySet()) {
            parcelables[i++] = device;
        }

        intent.putExtra(Constants.EXTRA_DEVICE_LIST, parcelables);
        startActivity(intent);
        mScanning = false;
        mScanTextView.setText(R.string.tap_to_scan);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private static final int SCAN_TIMEOUT = 5000;

    private void scan() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                showDevices();
            }
        }, SCAN_TIMEOUT);
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.glass_connector_scan_screen);
        mProgressBar = (ProgressBar)findViewById(R.id.glass_connector_scan_progress_bar);
        mScanTextView = (TextView)findViewById(R.id.glass_connector_scan_text_view);

        mHandler = new Handler(Looper.getMainLooper());

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (mScanning) {
                Log.d(TAG, "Scanning or service null");
                return super.onKeyDown(keycode, event);
            }

            mScanning = true;
            mScanTextView.setText(R.string.scanning);
            mProgressBar.setVisibility(View.VISIBLE);

            scan();

            return true;
        }

        return super.onKeyDown(keycode, event);
    }
}
