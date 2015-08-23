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

import java.util.HashSet;
import java.util.Set;

/**
 * OpenSpatial device parameters whose values can be set or queried.
 */
public enum DeviceParameter {

    /**
     * The frequency setting of an OpenSpatial device's motion processor (in Hz).
     */
    DEVICE_SAMPLE_FREQUENCY(0x0),

    /**
     * The full scale range of any available accelerometers.
     */
    DEVICE_ACCELEROMETER_FSR(0x1),

    /**
     * The full scale range of any available gyroscopes.
     */
    DEVICE_GYROSCOPE_FSR(0x2),

    /**
     * The full scale range of any available compasses.
     */
    DEVICE_COMPASS_FSR(0x3),

    /**
     * The time (in seconds) that the device will sit idle before going in to power saving mode.
     */
    DEVICE_IDLE_TIMEOUT(0x4),

    /**
     * The reported handedness of the device (left or right).
     */
    DEVICE_HANDEDNESS(0x5),

    /**
     * The reported orientation of the device.
     */
    DEVICE_ORIENTATION(0x6),

    /**
     * The frequency with which a sensor reports detected values.
     */
    SENSOR_REPORT_FREQUENCY(0x0),

    /**
     * The full scale range of a sensor provided in units that are sensor dependent.
     */
    SENSOR_FULL_SCALE_RANGE(0x1),

    /**
     * The number of sensors an OpenSpatial device has for a given {@link DataType}.
     */
    SENSOR_QUANTITY(0x2),

    /**
     * The scale factors for {@link RelativeXYData} which are provided in the form of high and low
     * values for both {@code X} and {@code Y} axes.
     */
    RELATIVE_XY_SCALE_FACTORS(0x3);

    private final byte id;

    DeviceParameter(int id) { this.id = (byte) id; }

    /**
     * Returns the byte used by the {@link OpenSpatialEventFactory} to indicate the parameter a
     * command or response is in reference to.
     * @return The value of the tag byte for this {@code DataType}.
     */
    protected byte getValue() { return id; }

    protected static Set<DeviceParameter> generalDeviceParameters = new HashSet<DeviceParameter>();

    static {
        generalDeviceParameters.add(DEVICE_SAMPLE_FREQUENCY);
        generalDeviceParameters.add(DEVICE_ACCELEROMETER_FSR);
        generalDeviceParameters.add(DEVICE_GYROSCOPE_FSR);
        generalDeviceParameters.add(DEVICE_COMPASS_FSR);
        generalDeviceParameters.add(DEVICE_IDLE_TIMEOUT);
        generalDeviceParameters.add(DEVICE_HANDEDNESS);
        generalDeviceParameters.add(DEVICE_ORIENTATION);
    }

    protected static Set<DeviceParameter> sensorParameters = new HashSet<DeviceParameter>();

    static {
        sensorParameters.add(SENSOR_REPORT_FREQUENCY);
        sensorParameters.add(SENSOR_FULL_SCALE_RANGE);
        sensorParameters.add(SENSOR_QUANTITY);
        sensorParameters.add(RELATIVE_XY_SCALE_FACTORS);
    }
}
