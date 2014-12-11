package com.nod_labs.unityplugin;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.unity3d.player.UnityPlayerNativeActivity;

import net.openspatial.ButtonEvent;
import net.openspatial.GestureEvent;
import net.openspatial.OpenSpatialEvent;
import net.openspatial.OpenSpatialException;
import net.openspatial.OpenSpatialService;
import net.openspatial.PointerEvent;
import net.openspatial.Pose6DEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class UnityPluginActivity extends UnityPlayerNativeActivity {

    OpenSpatialService mOpenSpatialService;
    private static String TAG = UnityPluginActivity.class.getSimpleName();

    private static Map<String, BluetoothDevice> addressMap = new HashMap<String, BluetoothDevice>();
    private static Map<BluetoothDevice, float[]> rotationMap = new HashMap<BluetoothDevice, float[]>();
    private static Map<BluetoothDevice, boolean[]> buttonMap = new HashMap<BluetoothDevice, boolean[]>();
    private static Map<BluetoothDevice, int[]> pointerMap = new HashMap<BluetoothDevice, int[]>();
    private static ArrayList<String> addressList= new ArrayList<String>();

    public static int getNumDevices(){
        return addressList.size();
    }

    public static String getDeviceAddress(int deviceIndex){
        return "TEST STRING HERE";
    }

    // Call from Unity to set a button's values to false
    public static void unityButtonCallback(String deviceAddress, int buttonType){
        if(buttonType == 0){
//          buttonMap.get(addressMap.get(deviceAddress))[0] = buttonMap.get(addressMap.get(deviceAddress))[1] = false;
        }
        else{
//          buttonMap.get(addressMap.get(deviceAddress))[2] = buttonMap.get(addressMap.get(deviceAddress))[3] = false;
        }
    }

    // Returns a boolean array of form [touch0, touch1, touch2]
    public static boolean[] getButtonData(String deviceAddress){
        return buttonMap.get(addressMap.get(deviceAddress));
    }

    // Returns an integer array of form [x, y]
    public static int[] getPointerData(String deviceAddress){
        return pointerMap.get(addressMap.get(deviceAddress));
    }

    // Returns a float array of form [pitch, roll, yaw]
    public static float[] getRotationData(String deviceAddress){
        return rotationMap.get(addressMap.get(deviceAddress));
    }

    private ServiceConnection mOpenSpatialServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service Connected");
            mOpenSpatialService = ((OpenSpatialService.OpenSpatialServiceBinder)service).getService();
            mOpenSpatialService.initialize(TAG, new OpenSpatialService.OpenSpatialServiceCallback() {

                @Override
                public void deviceConnected(BluetoothDevice device) {
                    Log.d(TAG, "Registering Device" + device);

                    rotationMap.put(device, new float[3]);
                    buttonMap.put(device, new boolean[3]);
                    pointerMap.put(device, new int[2]);
                    addressMap.put(device.getAddress(), device);
                    addressList.add(device.getAddress());

                    registerForEvents(device);
                }

                @Override
                public void deviceDisconnected(BluetoothDevice device) {
                    rotationMap.remove(device);
                    buttonMap.remove(device);
                    pointerMap.remove(device);
                    addressMap.remove(device.getAddress());
                    addressList.remove(device.getAddress());
                }

                @Override
                public void buttonEventRegistrationResult(BluetoothDevice device, int status) {
                }
                @Override
                public void pointerEventRegistrationResult(BluetoothDevice device, int status) {
                }
                @Override
                public void pose6DEventRegistrationResult(BluetoothDevice device, int status) {
                }
                @Override
                public void gestureEventRegistrationResult(BluetoothDevice device, int status) {
                }
                @Override
                public void motion6DEventRegistrationResult(BluetoothDevice device, int i) {
                }
            });

            mOpenSpatialService.getConnectedDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mOpenSpatialService = null;
        }
    };

    private void registerForEvents(final BluetoothDevice device) {
        try {
            mOpenSpatialService.registerForPointerEvents(device, new OpenSpatialEvent.EventListener() {
                @Override
                public void onEventReceived(OpenSpatialEvent event) {
                    PointerEvent pEvent = (PointerEvent) event;
                    pointerMap.get(device)[0] = pEvent.x;
                    pointerMap.get(device)[1] = pEvent.y;
                }
            });
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Error registering for PointerEvent " + e);
        }

        try {
            mOpenSpatialService.registerForButtonEvents(device, new OpenSpatialEvent.EventListener() {
                @Override
                public void onEventReceived(OpenSpatialEvent event) {
                    ButtonEvent bEvent = (ButtonEvent) event;
                    Log.d(TAG, device.getName() + ": received ButtonEvent of type " + bEvent.buttonEventType);
                    if (bEvent.buttonEventType == ButtonEvent.ButtonEventType.TOUCH0_DOWN) {
                        buttonMap.get(device)[0] = true;
                    } else if (bEvent.buttonEventType == ButtonEvent.ButtonEventType.TOUCH0_UP) {
                        buttonMap.get(device)[0] = false;
                    }else if (bEvent.buttonEventType == ButtonEvent.ButtonEventType.TOUCH1_DOWN) {
                        buttonMap.get(device)[1] = true;
                    } else if (bEvent.buttonEventType == ButtonEvent.ButtonEventType.TOUCH1_UP) {
                        buttonMap.get(device)[1] = false;
                    }
                    else if (bEvent.buttonEventType == ButtonEvent.ButtonEventType.TOUCH2_DOWN){
                        buttonMap.get(device)[2] = true;
                    }
                    else if (bEvent.buttonEventType == ButtonEvent.ButtonEventType.TOUCH2_UP){
                        buttonMap.get(device)[2] = false;
                    }
                    Log.d(TAG, "buttons: " + buttonMap.get(device)[0] + "  " + buttonMap.get(device)[1]);
                }
            });
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Error registering for Button Event " + e);
        }
        try {
            mOpenSpatialService.registerForPose6DEvents(device, new OpenSpatialEvent.EventListener() {
                @Override
                public void onEventReceived(OpenSpatialEvent event) {
                    Pose6DEvent pose6DEvent = (Pose6DEvent) event;
                    float[] pry = rotationMap.get(device);
                    pry[0] = pose6DEvent.pitch;
                    pry[1] = pose6DEvent.roll;
                    pry[2] = pose6DEvent.yaw;
                }
            });
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Error registering for Pose6DEvent " + e);
        }


        OpenSpatialEvent.EventListener gestureEventListener = new OpenSpatialEvent.EventListener() {
            @Override
            public void onEventReceived(OpenSpatialEvent event) {
                GestureEvent gesture = (GestureEvent)event;
                Log.d(TAG, device.getName() + ": got GestureEvent of type " + gesture.gestureEventType +
                        " with magnitude " + gesture.magnitude);
            }
        };

        try {
            mOpenSpatialService.registerForGestureEvents(device, gestureEventListener);
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Error registering for GestureEvent " + e);
        }
    }

    private void unregisterFromEvents(BluetoothDevice device) {
        try {
            mOpenSpatialService.unRegisterForPointerEvents(device);
            mOpenSpatialService.unRegisterForButtonEvents(device);
            mOpenSpatialService.unRegisterForPose6DEvents(device);
            mOpenSpatialService.unRegisterForGestureEvents(device);
        } catch (OpenSpatialException e) {
            Log.e(TAG, "Could not unregister from events: " + e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, OpenSpatialService.class),
                mOpenSpatialServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mOpenSpatialServiceConnection);
    }
}
