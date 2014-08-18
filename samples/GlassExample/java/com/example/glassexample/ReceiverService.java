package com.example.glassexample;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import net.openspatial.OpenSpatialEvent;
import net.openspatial.OpenSpatialException;
import net.openspatial.OpenSpatialSingleUserService;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ReceiverService  extends Service
        implements OpenSpatialSingleUserService.OpenSpatialSingleUserServiceCallback {
    private static final String TAG = "ReceiverService";

    private BluetoothDevice mDevice;
    private OpenSpatialSingleUserService mOpenSpatialService;
    private boolean mBinding = false;

    private ServiceConnection mOpenSpatialServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mOpenSpatialService = ((OpenSpatialSingleUserService.OpenSpatialSingleUserServiceBinder)service).getService();

            try {
                mOpenSpatialService.initialize(ReceiverService.this);
                mOpenSpatialService.connectToDevice(mDevice, false);
            } catch (OpenSpatialException e) {
                Log.e(TAG, "Failed to initialize OpenSpatialSingleUserService");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mOpenSpatialService = null;
        }
    };

    private void handleIntent(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "Received null intent");
            return;
        }

        String action = intent.getAction();
        if (action == null) {
            Log.e(TAG, "Action is null");
            return;
        }

        if (action.equals(Constants.ACTION_START_SERVICE)) {
            if (mBinding) {
                return;
            }

            mBinding = true;
            mDevice = intent.getParcelableExtra(Constants.EXTRA_DEVICE);
            if (mDevice == null) {
                Log.e(TAG, "EXTRA_DEVICE is null");
                return;
            }

            bindService(new Intent(this, OpenSpatialSingleUserService.class),
                    mOpenSpatialServiceConnection,
                    BIND_AUTO_CREATE);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendOpenSpatialEvent(String action, OpenSpatialEvent event) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(Constants.EXTRA_OPENSPATIAL_EVENT, event);

        sendBroadcast(intent);
    }

    private void sendConnectedIntent(BluetoothDevice device) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_DEVICE_CONNECTED);
        intent.putExtra(Constants.EXTRA_DEVICE, device);

        sendBroadcast(intent);
    }

    public void deviceConnected(BluetoothDevice device) {
        Log.d(TAG, mDevice.getName() + " connected.");

        sendConnectedIntent(device);

        try {
            mOpenSpatialService.registerForButtonEvents(mDevice, new OpenSpatialEvent.EventListener() {
                @Override
                public void onEventReceived(OpenSpatialEvent event) {
                    sendOpenSpatialEvent(Constants.ACTION_BUTTON_EVENT, event);
                }
            });

        } catch (OpenSpatialException e) {
            Log.e(TAG, "Registering ButtonEvents failed with error: " + e.getMessage());
        }
    }

    public void deviceConnectFailed(BluetoothDevice device, int status) {
        Log.e(TAG, "Failed to connect to " + device.getName() + " with status " + status);
    }

    public void deviceDisconnected(BluetoothDevice device) {
        Log.e(TAG, device.getName() + " disconnected");
    }

    public void buttonEventRegistrationResult(BluetoothDevice device, int status) {
        Log.e(TAG, "ButtonEvent registration result: " + status);

        try {
            mOpenSpatialService.registerForPointerEvents(mDevice, new OpenSpatialEvent.EventListener() {
                @Override
                public void onEventReceived(OpenSpatialEvent event) {
                    sendOpenSpatialEvent(Constants.ACTION_POINTER_EVENT, event);
                }
            });
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Registering for PointerEvents failed with error: " + e.getMessage());
        }
    }

    public void pointerEventRegistrationResult(BluetoothDevice device, int status) {
        Log.e(TAG, "PointerEvent registration result: " + status);
    }

    public void pose6DEventRegistrationResult(BluetoothDevice device, int status) {
    }

    public void gestureEventRegistrationResult(BluetoothDevice device, int status) {
    }

    public void motion6DEventRegistrationResult(BluetoothDevice device, int status) {
    }
}
