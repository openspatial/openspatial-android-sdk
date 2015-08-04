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
 * Contains the set of callbacks registered to a given device and
 * provides the methods required to access or set them.
 */
class EventCallbacks {

    OpenSpatialEvent.EventListener mPointerEventCallback = null;
    OpenSpatialEvent.EventListener mButtonEventCallback = null;
    OpenSpatialEvent.EventListener mPose6DEventCallback = null;
    OpenSpatialEvent.EventListener mMotion6DEventCallback = null;
    OpenSpatialEvent.EventListener mGestureEventCallback = null;
    OpenSpatialEvent.EventListener mAnalogDataEventCallback = null;
    OpenSpatialEvent.EventListener mExtendedEventCallback = null;
    OpenSpatialDataListener mOpenSpatialDataListener = null;

    /**
     * Get the registered callback that corresponds to the event type supplied as an argument
     *
     * @param eventType  The type of {@code OpenSpatialEvent} that the returned callback
     *                   corresponds to.
     *
     * @return The registered callback that is associated with the specified event type
     *
     * @deprecated use {@link #getCallback()} instead.
     */
    @Deprecated
    public OpenSpatialEvent.EventListener getCallback(OpenSpatialEvent.EventType eventType) {
        switch (eventType) {
            case EVENT_BUTTON:
                return mButtonEventCallback;
            case EVENT_GESTURE:
                return mGestureEventCallback;
            case EVENT_MOTION6D:
                return mMotion6DEventCallback;
            case EVENT_POINTER:
                return mPointerEventCallback;
            case EVENT_POSE6D:
                return mPose6DEventCallback;
            case EVENT_ANALOGDATA:
                return mAnalogDataEventCallback;
            case EVENT_EXTENDED:
                return mExtendedEventCallback;
            default:
                return null;
        }
    }

    /**
     * Get the callback that triggers upon receipt of an {@code OpenSpatialData}
     *
     * @return The callback registered by the client
     */
    protected OpenSpatialDataListener getCallback() {
        return mOpenSpatialDataListener;
    }

    /**
     * Get the registered callback that corresponds to the event type supplied as an argument.
     *
     * @param listener The listener that will report the receipt of data from an OpenSpatial device.
     */
    protected void setCallback(OpenSpatialDataListener listener) {
        mOpenSpatialDataListener = listener;
    }

    /**
     * Get the registered callback that corresponds to the event type supplied as an argument
     *
     * @param eventType  The type of {@code OpenSpatialEvent} that the returned callback will be
     *                   registered for.
     * @param callback The callback to be set.
     * @throws OpenSpatialException If eventType is null;
     *
     * @deprecated use {@link #setCallback(OpenSpatialDataListener)} instead.
     */
    @Deprecated
    public void setCallback(OpenSpatialEvent.EventType eventType,
                            OpenSpatialEvent.EventListener callback)
            throws OpenSpatialException {
        if (eventType == null) {
            throw new OpenSpatialException(OpenSpatialException.ErrorCode.INVALID_PARAMETER,
                    "setCallback called with a null event type!");
        }
        switch (eventType) {
            case EVENT_BUTTON:
                mButtonEventCallback = callback;
                break;
            case EVENT_GESTURE:
                mGestureEventCallback = callback;
                break;
            case EVENT_MOTION6D:
                mMotion6DEventCallback = callback;
                break;
            case EVENT_POINTER:
                mPointerEventCallback = callback;
                break;
            case EVENT_POSE6D:
                mPose6DEventCallback = callback;
                break;
            case EVENT_ANALOGDATA:
                mAnalogDataEventCallback = callback;
                break;
            case EVENT_EXTENDED:
                mExtendedEventCallback = callback;
                break;
        }
    }
}
