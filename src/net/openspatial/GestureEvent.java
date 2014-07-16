/**
 * Copyright 2014, ënimai Inc.
 */

package net.openspatial;

import android.os.Parcel;

/**
 * A {@code GestureEvent} respresents an instance of a "Gesture". A Gesture is a combination of
 * {@link net.openspatial.ButtonEvent}s and {@link net.openspatial.RotationEvent}s. When an event is recognized,
 * a GestureEvent of the given {@code GestureEventType} is fired.
 */
public class GestureEvent extends OpenSpatialEvent {

    /**
     * The currently supported Gestures
     */
    public enum GestureEventType {
        /**
         * The user swiped up
         */
        SWIPE_UP,

        /**
         * The user swiped down
         */
        SWIPE_DOWN,

        /**
         * The user swiped left
         */
        SWIPE_LEFT,

        /**
         * The user swiped right
         */
        SWIPE_RIGHT,

        /**
         * The user rotated clockwise
         */
        CLOCKWISE_ROTATION,

        /**
         * The user rotated counterclockwise
         */
        COUNTERCLOCKWISE_ROTATION,

        /**
         * The user scrolled down
         */
        SCROLL_DOWN,

        /**
         * The user scrolled up
         */
        SCROLL_UP,
    }

    /**
     * The Gesture event type
     */
    public GestureEventType gestureEventType;

    /**
     * The magnitude associated with this event. For example, a longer {@code SWIPE_*} Gesture will have a larger
     * magnitude than a smaller one.
     */
    public double magnitude;

    /**
     * Create a new {@code GestureEvent} of a given type with a given magnitude
     * @param type The type of the Gesture as defined by {@code GestureEventType}
     * @param magnitude The magnitude of the Gesture
     */
    public GestureEvent(GestureEventType type, double magnitude) {
        super(EventType.EVENT_GESTURE);

        this.gestureEventType = type;
        this.magnitude = magnitude;
    }

    // Methods for making the class Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeSerializable(gestureEventType);
        out.writeDouble(magnitude);
    }

    private GestureEvent(Parcel in) {
        super(in);
        this.gestureEventType = (GestureEventType)in.readSerializable();
        this.magnitude = in.readDouble();
    }

    public static final Creator<GestureEvent> CREATOR = new Creator<GestureEvent>() {
        @Override
        public GestureEvent createFromParcel(Parcel in) {
            return new GestureEvent(in);
        }

        @Override
        public GestureEvent[] newArray(int size) {
            return new GestureEvent[size];
        }
    };
}
