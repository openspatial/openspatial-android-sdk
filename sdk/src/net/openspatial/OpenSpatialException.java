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

/**
 * Exception thrown for failure cases in the OpenSpatialService
 */
public class OpenSpatialException extends Exception {
    /**
     * Error codes for the different error conditions
     */
    public enum ErrorCode {
        /**
         * The device is not currently registered
         */
        DEVICE_NOT_REGISTERED,

        /**
         * The device is already registered for the given callback
         */
        DEVICE_ALREADY_REGISTERED,

        /**
         * Bluetooth not supported
         */
        BLUETOOTH_NOT_SUPPORTED,

        /**
         * Bluetooth not on
         */
        BLUETOOTH_OFF,

        /**
         * An invalid parameter was passed to an OpenSpatial method
         */
        INVALID_PARAMETER,
    }

    private ErrorCode mErrorCode;

    public OpenSpatialException(ErrorCode code, String message) {
        super(message);

        mErrorCode = code;
    }

    public ErrorCode getErrorCode() {
        return mErrorCode;
    }
}
