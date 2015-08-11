/*
 * Copyright 2015, Nod Labs
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

package net.openspatial.openspatialexample;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import net.openspatial.DataType;
import net.openspatial.OpenSpatialConstants;
import net.openspatial.OpenSpatialData;
import net.openspatial.OpenSpatialDataListener;
import net.openspatial.OpenSpatialErrorCodes;
import net.openspatial.OpenSpatialException;
import net.openspatial.OpenSpatialInterface;
import net.openspatial.OpenSpatialService;

import java.util.HashSet;
import java.util.Set;

public class OpenSpatialExampleActivity extends AppCompatActivity implements OpenSpatialInterface {

    private static final String TAG = OpenSpatialExampleActivity.class.getSimpleName();

    OpenSpatialService mOpenSpatialService = null;

    Set<BluetoothDevice> mConnectedDevices = new HashSet<>();
    HashSet<DataType> mDataTypes = new HashSet<>();

    private ServiceConnection mOpenSpatialServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "OpenSpatial service connected!");
            OpenSpatialService.OpenSpatialServiceBinder  binder =
                    (OpenSpatialService.OpenSpatialServiceBinder) iBinder;

            mOpenSpatialService = binder.getService();

            mOpenSpatialService.initialize(TAG, OpenSpatialExampleActivity.this);
            mOpenSpatialService.getConnectedDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "OpenSpatial service disconnected!");
        }
    };

    private OpenSpatialDataListener mOpenSpatialDataListener = new OpenSpatialDataListener() {
        @Override
        public void onDataReceived(OpenSpatialData openSpatialData) {
            Log.d(TAG, "Received data from " + openSpatialData.device.getName() + "!");
            Log.d(TAG, openSpatialData.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDataTypes.add(DataType.BUTTON);
        mDataTypes.add(DataType.SLIDER);
        mDataTypes.add(DataType.GESTURE);

        bindService(new Intent(this, OpenSpatialService.class),
                mOpenSpatialServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mOpenSpatialService != null) {
            mOpenSpatialService.getConnectedDevices();
        }
    }

    @Override
    protected void onPause() {
        if (mOpenSpatialService != null) {
            for (BluetoothDevice d : mConnectedDevices) {
                try {
                    mOpenSpatialService.unregisterForData(d);
                    Log.d(TAG, "Un-registered " + d.getName() + ".");
                } catch (OpenSpatialException e) {
                    Log.d(TAG, "Un-registration failed! Reason: " + e.getMessage());
                }
            }
        }

        super.onPause();
    }

    @Override
    public void onDestroy() {
        unbindService(mOpenSpatialServiceConnection);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void deviceConnected(BluetoothDevice bluetoothDevice) {
        Log.d(TAG, bluetoothDevice.getName() + " is connected!");

        try {
            mOpenSpatialService.registerForData(bluetoothDevice,
                    mDataTypes, mOpenSpatialDataListener);
            mConnectedDevices.add(bluetoothDevice);
        } catch (OpenSpatialException e) {
            Log.d(TAG, "Couldn't register " + bluetoothDevice.getName()
                    + "! Reason: " + e.getMessage());
        }

        try {
            mOpenSpatialService.queryDeviceInfo(bluetoothDevice,
                    OpenSpatialConstants.INFO_BATTERY_LEVEL);
        } catch (OpenSpatialException e) {
            Log.d(TAG, "Failed to query battery level! Reason: " + e.getMessage());
        }
    }

    @Override
    public void deviceDisconnected(BluetoothDevice bluetoothDevice) {
        Log.d(TAG, bluetoothDevice.getName() + " has disconnected.");
        mConnectedDevices.remove(bluetoothDevice);
    }

    @Override
    public void registrationResult(BluetoothDevice bluetoothDevice, int status) {
        if (status == OpenSpatialErrorCodes.SUCCESS) {
            Log.d(TAG, bluetoothDevice.getName() + " successfully registered!");
        } else {
            Log.d(TAG, bluetoothDevice.getName() + " failed to register.");
        }
    }

    @Override
    public void deviceInfoReceived(BluetoothDevice bluetoothDevice,
                                   String infoType, Bundle receivedInfo) {
        if (infoType.equals(OpenSpatialConstants.INFO_BATTERY_LEVEL)) {
            Integer batteryLevel = receivedInfo.getInt(OpenSpatialConstants.INFO_BATTERY_LEVEL);

            Log.d(TAG, bluetoothDevice.getName()
                    + " reports a battery level of " + batteryLevel + "%.");
        }
    }
}
