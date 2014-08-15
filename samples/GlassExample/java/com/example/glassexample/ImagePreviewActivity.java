/*
 * Copyright 2014, Nod Labs.
 */

package com.example.glassexample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.nod_labs.pointer.PointerService;
import net.openspatial.*;

import java.io.File;
import java.io.FileOutputStream;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ImagePreviewActivity extends Activity {
    private static final String TAG = "ImagePreviewActivity";
    private static final int RADIUS = 10;
    private static final int[] COLOR = {255, 255, 255, 255};

    private DrawableView mDrawableView;
    Bitmap mBitmap;
    Handler mHandler;
    private boolean mShouldWrite = true;
    private boolean mShouldShare = true;

    BluetoothDevice mDevice;

    private PointerService mPointerService;

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            OpenSpatialEvent event = intent.getParcelableExtra(Constants.EXTRA_OPENSPATIAL_EVENT);

            switch (event.eventType) {
                case EVENT_BUTTON:
                    handleButtonEvent((ButtonEvent)event);
                    break;
                case EVENT_POINTER:
                    handlePointerEvent((PointerEvent)event);
                    break;
            }
        }
    };

    private ServiceConnection mPointerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Bound to PointerService");

            mPointerService = ((PointerService.PointerServiceBinder)service).getService();

            mPointerService.addPointer(mDevice.getAddress(),
                    RADIUS, COLOR[0], COLOR[1], COLOR[2], COLOR[3], mDevice.getName());
            mPointerService.registerView(mDrawableView, mDrawableView);

            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.ACTION_BUTTON_EVENT);
            filter.addAction(Constants.ACTION_POINTER_EVENT);
            registerReceiver(mReceiver, filter);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPointerService = null;
        }
    };

    public int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize * 2;
    }

    public Bitmap getSampledBitmap(Uri uri, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri.getPath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(uri.getPath(), options);
    }

    private void writeOverlayBitmap() {
        final Bitmap bmp = mBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(bmp);
        mDrawableView.onDraw(canvas);

          File picDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
          File picFile = new File(picDir.getPath() + File.separator + "out_pic.png");

          try {
              FileOutputStream outputStream = new FileOutputStream(picFile);
              bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
              outputStream.flush();
              outputStream.close();

              Intent intent = new Intent(this, ShareActivity.class);
              intent.putExtra(Constants.SHARE_PIC_EXTRA, Uri.fromFile(picFile));
              intent.putExtra(Constants.EXTRA_DEVICE, mDevice);
              startActivity(intent);
              finish();
          } catch (Exception e) {
              Log.e(TAG, "Error writing output file" + e.getMessage());
          } finally {
              Log.d(TAG, "Writing done! - " + picFile.getPath());
          }
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.image_preview);

        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.image_preview_container);

        HandlerThread t = new HandlerThread("ImagePreviewFileWriter");
        t.start();
        mHandler = new Handler(t.getLooper());

        Intent intent = getIntent();
        Uri picUri = intent.getParcelableExtra(Constants.BITMAP_URI_EXTRA);

        mDevice = intent.getParcelableExtra(Constants.EXTRA_DEVICE);
        if (mDevice == null) {
            mDevice = savedInstance.getParcelable(Constants.EXTRA_DEVICE);
        }

        Log.d(TAG, "Path is " + picUri.getPath());
        ImageView image = new ImageView(this);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        mBitmap = getSampledBitmap(picUri, size.x, size.y);
        image.setImageBitmap(mBitmap);
        image.setScaleType(ImageView.ScaleType.FIT_XY);

        frameLayout.addView(image);
        //Log.d(TAG, "Got bitmap");
        mDrawableView = new DrawableView(this);
        frameLayout.addView(mDrawableView, 1);
        Log.d(TAG, "setting views");
    }

    @Override
    public void onResume() {
        super.onResume();

        bindService(new Intent(this, PointerService.class), mPointerServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(mReceiver);
        unbindService(mPointerServiceConnection);
    }

    public void handleButtonEvent(ButtonEvent buttonEvent) {
        Log.d(TAG, "Got ButtonEvent of type: " + buttonEvent.buttonEventType);

        switch (buttonEvent.buttonEventType) {
            case TOUCH2_DOWN:
                if (mShouldWrite && mShouldShare) {
                    mShouldShare = false;
                    mShouldWrite = false;
                    writeOverlayBitmap();
                }
                break;
            default:
                mPointerService.performButtonEvent(buttonEvent.device.getAddress(), buttonEvent);
                break;
        }
    }

    public void handlePointerEvent(PointerEvent pointerEvent) {
        Log.d(TAG, "Got PointerEvent with x: " + pointerEvent.x + ", y: " + pointerEvent.y);
        mPointerService.updatePointerPosition(
                pointerEvent.device.getAddress(), pointerEvent.x, pointerEvent.y);
    }
}
