package net.openspatial;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;

/**
 * This event contains raw accelerometer and gyroscope readings along the 3 axes.
 */
public class Motion6DEvent extends OpenSpatialEvent {
    /**
     * The accelerometer reading along the X axis
     */
    public float accelX;

    /**
     * The accelerometer reading along the Y axis
     */
    public float accelY;

    /**
     * The accelerometer reading along the Z axis
     */
    public float accelZ;

    /**
     * The gyroscope reading about the Y axis
     */
    public float gyroX;

    /**
     * The gyroscope reading about the X axis
     */
    public float gyroY;

    /**
     * The gyroscope reading about the Z axis
     */
    public float gyroZ;


    /**
     * Construct a Pose6DEvent based on x, y, z, roll, pitch, yaw
     */
    public Motion6DEvent(BluetoothDevice device,
                         float accelX,
                         float accelY,
                         float accelZ,
                         float gyroX,
                         float gyroY,
                         float gyroZ) {
        super(device, EventType.EVENT_MOTION6D);

        this.accelX = accelX;
        this.accelY = accelY;
        this.accelZ = accelZ;
        this.gyroX = gyroX;
        this.gyroY = gyroY;
        this.gyroZ = gyroZ;
    }

    // Methods to make this class Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeFloat(accelX);
        out.writeFloat(accelY);
        out.writeFloat(accelZ);

        out.writeFloat(gyroX);
        out.writeFloat(gyroY);
        out.writeFloat(gyroZ);
    }

    private Motion6DEvent(Parcel in) {
        super(in);
        accelX = in.readFloat();
        accelY = in.readFloat();
        accelZ = in.readFloat();

        gyroX = in.readFloat();
        gyroY = in.readFloat();
        gyroZ = in.readFloat();
    }

    public static final Creator<Motion6DEvent> CREATOR = new Creator<Motion6DEvent>() {
        @Override
        public Motion6DEvent createFromParcel(Parcel in) {
            return new Motion6DEvent(in);
        }

        @Override
        public Motion6DEvent[] newArray(int size) {
            return new Motion6DEvent[size];
        }
    };
}
