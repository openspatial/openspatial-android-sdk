package com.example.openspatial.targeteuler;

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
import net.openspatial.DeviceParameter;
import net.openspatial.EulerData;
import net.openspatial.OpenSpatialData;
import net.openspatial.OpenSpatialInterface;
import net.openspatial.OpenSpatialService;
import net.openspatial.ResponseCode;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "TargetEuler";
    OpenSpatialService mOpenSpatialService;

    private ServiceConnection mOpenSpatialServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mOpenSpatialService =
                    ((OpenSpatialService.OpenSpatialServiceBinder)service).getService();
            mOpenSpatialService.initialize(TAG, new OpenSpatialInterface() {
                @Override
                public void onDeviceConnected(BluetoothDevice bluetoothDevice) {
                    mOpenSpatialService.enableData(bluetoothDevice, DataType.EULER_ANGLES);
                }

                @Override
                public void onDeviceDisconnected(BluetoothDevice bluetoothDevice) {

                }

                @Override
                public void onGetParameterResponse(BluetoothDevice bluetoothDevice, DataType dataType, DeviceParameter deviceParameter, ResponseCode responseCode, short[] shorts) {

                }

                @Override
                public void onSetParameterResponse(BluetoothDevice bluetoothDevice, DataType dataType, DeviceParameter deviceParameter, ResponseCode responseCode, short[] shorts) {

                }

                @Override
                public void onGetIdentifierResponse(BluetoothDevice bluetoothDevice, DataType dataType, byte b, ResponseCode responseCode, String s) {

                }

                @Override
                public void onGetParameterRangeResponse(BluetoothDevice bluetoothDevice, DataType dataType, DeviceParameter deviceParameter, ResponseCode responseCode, Number number, Number number1) {

                }

                @Override
                public void onDataEnabledResponse(BluetoothDevice bluetoothDevice, DataType dataType, ResponseCode responseCode) {

                }

                @Override
                public void onDataDisabledResponse(BluetoothDevice bluetoothDevice, DataType dataType, ResponseCode responseCode) {

                }

                @Override
                public void onDataReceived(OpenSpatialData openSpatialData) {
                    String deviceName = openSpatialData.device.getName();
                    if (openSpatialData.dataType.equals(DataType.EULER_ANGLES)) {
                        EulerData eulerData = (EulerData) openSpatialData;
                        updateAngle(deviceName, eulerData);
                    }
                }
            });
            mOpenSpatialService.getConnectedDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mOpenSpatialService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindService(new Intent(this, OpenSpatialService.class),
                mOpenSpatialServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        unbindService(mOpenSpatialServiceConnection);
        super.onDestroy();
    }

    public void updateAngle(String device, EulerData angle) {
        String logline = device + " " + angle.getRoll()
                + " " + angle.getPitch()
                + " " + angle.getYaw();
        Log.d(TAG, logline);
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
}
