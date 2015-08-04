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

package net.openspatial;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import java.util.HashSet;

/**
 * The callbacks used by the {@link OpenSpatialService} to communicate with clients.
 */
public interface OpenSpatialInterface {
    /**
     * After {@link OpenSpatialService#getConnectedDevices()} is called this will trigger once per
     * available OpenSpatial device.
     * @param device An available OpenSpatial device.
     */
    void deviceConnected(BluetoothDevice device);
    /**
     * Triggers when an OpenSpatial device disconnects.
     * @param device The OpenSpatial device that has disconnected.
     */
    void deviceDisconnected(BluetoothDevice device);

    /**
     * Reports the outcome of a call to
     * {@link OpenSpatialService#registerForData(BluetoothDevice, HashSet, OpenSpatialDataListener)}
     * .
     *
     * @param device The device that registration was attempted on.
     * @param status The outcome of the attempt. Will be one of the values defined within
     *               {@link OpenSpatialErrorCodes}.
     */
    void registrationResult(BluetoothDevice device, int status);

    /**
     * Reports the response returned from a call to
     * {@link OpenSpatialService#queryDeviceInfo(BluetoothDevice, String)}.
     *
     * @param device The OpenSpatialDevice that was queried.
     * @param infoType The type of inquiry.
     * @param infoData The data returned.
     */
    void deviceInfoReceived(BluetoothDevice device, String infoType, Bundle infoData);
}
