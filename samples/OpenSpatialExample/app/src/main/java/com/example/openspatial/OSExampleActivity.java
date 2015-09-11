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

package com.example.openspatial;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.openspatial.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class OSExampleActivity extends Activity implements OpenSpatialInterface {

    private static String TAG = OSExampleActivity.class.getSimpleName();

    private static final int MAX_LINES = 50;

    OpenSpatialService mOpenSpatialService;

    TextView mTextView;
    List<BluetoothDevice> mDevices;
    BluetoothDevice mCurrentDevice = null;
    DataType mCurrentDataType = null;

    private ArrayAdapter<BluetoothDevice> mDeviceSpinnerAdapter = null;

    private ArrayList<DataType> mDataList =
            new ArrayList<DataType>(
                    Arrays.asList(DataType.values()));

    private ServiceConnection mOpenSpatialServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mOpenSpatialService = ((OpenSpatialService.OpenSpatialServiceBinder)service).getService();

            /* Now that the service is connected we initialize it and then see what devices are
             * connected.
             */
            mOpenSpatialService.initialize(TAG, OSExampleActivity.this);
            mOpenSpatialService.getConnectedDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mOpenSpatialService = null;
        }
    };

    private void initDeviceSpinner() {
        Spinner deviceSpinner = (Spinner)findViewById(R.id.device_spinner);
        mDeviceSpinnerAdapter = new ArrayAdapter<BluetoothDevice>(this,
                android.R.layout.simple_spinner_item, mDevices);
        mDeviceSpinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        deviceSpinner.setAdapter(mDeviceSpinnerAdapter);
        deviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    mCurrentDevice = (BluetoothDevice) parent.getItemAtPosition(position);
                } catch (Exception e) {
                    Toast.makeText(OSExampleActivity.this.getApplicationContext(),
                            R.string.no_connected_devices, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //No-op
            }
        });
    }

    private void initDataSpinner() {
        Spinner eventSpinner = (Spinner)findViewById(R.id.data_type_spinner);
        ArrayAdapter<DataType> mDataSpinnerAdapter = new ArrayAdapter<DataType>(this,
                android.R.layout.simple_spinner_item,
                mDataList);
        mDataSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventSpinner.setAdapter(mDataSpinnerAdapter);
        eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentDataType = (DataType)
                        parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No-op
            }
        });
    }

    private void initOnButton() {
        Button onButton = (Button) findViewById(R.id.on_button);
        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentDevice != null
                        && mCurrentDataType != null
                        && mOpenSpatialService != null) {

                    /* This sends a request to mCurrentDevice to begin reporting data of the
                     * type specified by mCurrentDataType.
                     */
                    mOpenSpatialService.enableData(mCurrentDevice, mCurrentDataType);
                }
            }
        });
    }

    private void initOffButton() {
        Button offButton = (Button) findViewById(R.id.off_button);
        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentDevice != null
                        && mCurrentDataType != null
                        && mOpenSpatialService != null) {

                    /* This sends a request to mCurrentDevice to stop reporting data of the type
                     * specified by mCurrentDataType.
                     */
                    mOpenSpatialService.disableData(mCurrentDevice,
                            mCurrentDataType);
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.openspatial_example_layout);
        mTextView = (TextView)findViewById(R.id.data_text_view);
        mTextView.setMovementMethod(new ScrollingMovementMethod());

        mDevices = new ArrayList<BluetoothDevice>();

        initDeviceSpinner();
        initDataSpinner();

        initOnButton();
        initOffButton();

        bindService(new Intent(this, OpenSpatialService.class),
                mOpenSpatialServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbindService(mOpenSpatialServiceConnection);
    }

    private void log(String message) {
        mTextView.append(message + "\n");

        if (mTextView.getLineCount() > MAX_LINES) {
            Editable editable = mTextView.getEditableText();
            editable.delete(0, editable.length()/2);
        }
        Log.d(TAG, message);
    }

    /* The following callback will be triggered once per connected device after the OpenSpatial
     * services getConnectedDevices() method is called.
     */
    @Override
    public void onDeviceConnected(BluetoothDevice device) {
        log(device.getName() + " connected");
        mDeviceSpinnerAdapter.add(device);
        mDeviceSpinnerAdapter.notifyDataSetChanged();

        // Ask the service to report how many buttons the device has.
        mOpenSpatialService.getParameter(device, DataType.BUTTON, DeviceParameter.SENSOR_QUANTITY);

        // Ask the service to report the minimum and maximum data sample rate of the device
        mOpenSpatialService.getParameterRange(device, DataType.GENERAL_DEVICE_INFORMATION,
                DeviceParameter.DEVICE_SAMPLE_FREQUENCY);

        /* Request that the amount of time a device will sit idle before entering a low power state
         * be set to 180 seconds.
         */
        mOpenSpatialService.setParameter(device, DataType.GENERAL_DEVICE_INFORMATION,
                DeviceParameter.DEVICE_IDLE_TIMEOUT, 180);

        // Ask for the human readable string identifier of the button with ID 1.
        mOpenSpatialService.getIdentifier(device, DataType.BUTTON, (byte) 1);
    }

    /*
     * This is called whenever the OpenSpatial service detects a device has disconnected.
     */
    @Override
    public void onDeviceDisconnected(BluetoothDevice device) {
        log(device.getName() + "disconnected");
        mDeviceSpinnerAdapter.remove(device);
        mDeviceSpinnerAdapter.notifyDataSetChanged();
    }

    /*
     * This is invoked whenever a device responds to a call to OpenSpatial's getParameter() method.
     */
    @Override
    public void onGetParameterResponse(BluetoothDevice bluetoothDevice, DataType dataType,
                                       DeviceParameter deviceParameter, ResponseCode responseCode,
                                       short[] values) {
        log("GET: " + bluetoothDevice.getName() + " " + dataType.name() + " "
                + deviceParameter.name() + " " + responseCode.name() + Arrays.toString(values));
    }

    /*
     * This is invoked whenever a device responds to a call to OpenSpatial's setParameter() method.
     */
    @Override
    public void onSetParameterResponse(BluetoothDevice bluetoothDevice, DataType dataType,
                                       DeviceParameter deviceParameter, ResponseCode responseCode,
                                       short[] values) {
        log("SET: " +  bluetoothDevice.getName() + " " + dataType.name() + " "
                + deviceParameter.name() + " " + responseCode.name() + Arrays.toString(values));
    }

    /*
     * This is invoked whenever a device responds to a call to OpenSpatial's getIdentifier method.
     */
    @Override
    public void onGetIdentifierResponse(BluetoothDevice bluetoothDevice,
                                        DataType dataType, byte index,
                                        ResponseCode responseCode, String identifier) {
        log("ID: " + bluetoothDevice.getName() + " " + dataType.name() + " index: " + index + " "
                + responseCode.name() + " " + identifier);
    }

    /*
     * This is invoked whenever a device responds to a call to OpenSpatial's getParameterRange()
     * method.
     */
    @Override
    public void onGetParameterRangeResponse(BluetoothDevice bluetoothDevice,
                                            DataType dataType,
                                            DeviceParameter deviceParameter,
                                            ResponseCode responseCode,
                                            Number low, Number high) {
        log("RANGE: " + bluetoothDevice.getName() + " " + dataType.name() + " "
                + deviceParameter.name() + " " + responseCode.name()
                + " [" + low + "," + high + "]");
    }


    /*
     * This is invoked whenever a device responds to a call to OpenSpatial's enableData() method.
     */
    @Override
    public void onDataEnabledResponse(BluetoothDevice bluetoothDevice,
                                      DataType dataType,
                                      ResponseCode responseCode) {

        log(bluetoothDevice.getName() + ": " +
                dataType.name() + " enabled: " + responseCode.name());
    }

    /*
     * This is invoked whenever a device responds to a call to OpenSpatial's disableData() method.
     */
    @Override
    public void onDataDisabledResponse(BluetoothDevice bluetoothDevice,
                                       DataType dataType,
                                       ResponseCode responseCode) {
        log(bluetoothDevice.getName() + ": " +
                dataType.name() + " disabled: " + responseCode.name());
    }

    /*
     * This callback is fired every time a device reports data requested by OpenSpatial's
     * enableData() method.
     */
    @Override
    public void onDataReceived(OpenSpatialData openSpatialData) {
        log(openSpatialData.toString());
    }
}
