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
     *
     * @param device An available OpenSpatial device.
     */
    void onDeviceConnected(BluetoothDevice device);
    /**
     * Triggers when an OpenSpatial device disconnects.
     *
     * @param device The OpenSpatial device that has disconnected.
     */
    void onDeviceDisconnected(BluetoothDevice device);

    /**
     * Triggers when a response to an attempt to get parameters is received.
     * @param device The OpenSpatial device that is responding.
     * @param dataType The {@link DataType} the response is in reference to.
     * @param deviceParameter The {@link DeviceParameter} being reported on.
     * @param responseCode The {@link ResponseCode} the device has replied with.
     * @param values The values (if any) reported by the device.
     */
    void onGetParameterResponse(BluetoothDevice device,
                                DataType dataType,
                                DeviceParameter deviceParameter,
                                ResponseCode responseCode,
                                short[] values);

    /**
     * Triggers when a response to an attempt to set parameters is received.
     * @param device The OpenSpatial device that is responding.
     * @param dataType The {@link DataType} the response is in reference to.
     * @param deviceParameter The {@link DeviceParameter} being reported on.
     * @param responseCode The {@link ResponseCode} the device has replied with.
     * @param values The values (if any) reported by the device.
     */
    void onSetParameterResponse(BluetoothDevice device,
                                DataType dataType,
                                DeviceParameter deviceParameter,
                                ResponseCode responseCode,
                                short[] values);

    /**
     * Triggers when a response to an attempt to get the human readable identifier of a sensor.
     * @param device The OpenSpatial device that is responding.
     * @param dataType The {@link DataType} the response is in reference to.
     * @param index The index of the sensor being reported on.
     * @param responseCode The {@link ResponseCode} the device has replied with.
     * @param identifier The human readable name of the sensor with the given index.
     */
    void onGetIdentifierResponse(BluetoothDevice device,
                                 DataType dataType,
                                 byte index,
                                 ResponseCode responseCode,
                                 String identifier);

    /**
     * Triggers when a response to an attempt to query the range of a {@link DeviceParameter} is
     * received.
     *
     * @param device The OpenSpatial device that is responding.
     * @param dataType The {@link DataType} the response is in reference to.
     * @param deviceParameter The {@link DeviceParameter} being reported on.
     * @param responseCode The {@link ResponseCode} reported by the device.
     * @param low If provided, the minimum value the {@link DeviceParameter} can hold.
     * @param high If provided, the maximum value the {@link DeviceParameter} can hold.
     */
    void onGetParameterRangeResponse(BluetoothDevice device,
                                     DataType dataType,
                                     DeviceParameter deviceParameter,
                                     ResponseCode responseCode,
                                     Number low,
                                     Number high);

    /**
     * The response provided after a request to enable data for a given {@link DataType} has been
     * made.
     * @param device The OpenSpatial device that is responding.
     * @param dataType The {@link DataType} that was requested to be enabled.
     * @param responseCode The {@link ResponseCode} reported by the device.
     */
    void onDataEnabledResponse(BluetoothDevice device,
                               DataType dataType,
                               ResponseCode responseCode);

    /**
     * The response provided after a request to disable data for a given {@link DataType} has been
     * made.
     * @param device The OpenSpatial device that is responding.
     * @param dataType The {@link DataType} that was requested to be disabled.
     * @param responseCode The {@link ResponseCode} reported by the device.
     */
    void onDataDisabledResponse(BluetoothDevice device,
                               DataType dataType,
                               ResponseCode responseCode);
    /**
     * Callback method that is called when new {@link OpenSpatialData}
     * is received.
     *
     * @param data The {@code OpenSpatialData} that was received.
     */
    void onDataReceived(OpenSpatialData data);
}
