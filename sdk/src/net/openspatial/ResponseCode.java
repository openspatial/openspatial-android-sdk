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
 * Possible responses to commands that OpenSpatial devices can report.
 */
public enum ResponseCode {
    /**
     * The command sent successfully executed.
     */
    OK,

    /**
     * The OpenSpatial device does not have support for the command it was sent.
     */
    UNSUPPORTED,

    /**
     * The OpenSpatial device failed to execute the command it was issued.
     */
    FAILED,

    /**
     * The OpenSpatial device does not have support for the {@link DataType} the command referred
     * to.
     */
    INVALID_DATATYPE,

    /**
     * The provided parameter does not exist for or is not supported by the {@link DataType}
     * referenced in the command.
     */
    INVALID_PARAMETER,

    /**
     * The value(s) passed as arguments were rejected by the OpenSpatial device as invalid.
     */
    INVALID_VALUE,

    /**
     * The command has resulted in a parameter of an OpenSpatial device changing to a different
     * value than the one that was previously held.
     */
    PARAMETER_VALUE_REPLACED
}
