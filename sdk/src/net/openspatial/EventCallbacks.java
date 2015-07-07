package net.openspatial;

/**
 * An {@code EventCallbacks} represents the set of callbacks registered to a given device and
 * provides the methods required to access or set them.
 */
public class EventCallbacks {

    OpenSpatialEvent.EventListener mPointerEventCallback = null;
    OpenSpatialEvent.EventListener mButtonEventCallback = null;
    OpenSpatialEvent.EventListener mPose6DEventCallback = null;
    OpenSpatialEvent.EventListener mMotion6DEventCallback = null;
    OpenSpatialEvent.EventListener mGestureEventCallback = null;
    OpenSpatialEvent.EventListener mAnalogDataEventCallback = null;
    OpenSpatialEvent.EventListener mExtendedEventCallback = null;

    /**
     * Get the registered callback that corresponds to the event type supplied as an argument
     *
     * @param eventType  The type of {@code OpenSpatialEvent} that the returned callback
     *                   corresponds to.
     *
     * @return The registered callback that is associated with the specified event type
     */
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
     * Get the registered callback that corresponds to the event type supplied as an argument
     *
     * @param eventType  The type of {@code OpenSpatialEvent} that the returned callback will be
     *                   registered for.
     *
     * @return The registered callback that is associated with the specified event type
     */
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
