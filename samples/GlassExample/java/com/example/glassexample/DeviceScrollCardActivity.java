/**
 * Copyright 2014, Nod Labs
 */

package com.example.glassexample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DeviceScrollCardActivity extends Activity {
    List<BluetoothDevice> mDevices;
    List<Card> mCards;
    CardScrollView mCardScrollView;

    private static final String TAG = DeviceScrollCardActivity.class.getSimpleName();

    private class DeviceCardScrollAdapter extends CardScrollAdapter {
        @Override
        public int getPosition(Object item) {
            return mCards.indexOf(item);
        }

        @Override
        public int getCount() {
            return mCards.size();
        }

        @Override
        public Object getItem(int position) {
            return mCards.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mCards.get(position).getView(convertView, parent);
        }
    }

    private Card createCard(BluetoothDevice device) {
        Card card = new Card(this);
        card.setText(device.getName() + "(" + device.getAddress() + ")");
        card.setFootnote("Swipe right for next device");

        View v = card.getView();
        v.setTag(device);
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothDevice d = (BluetoothDevice)v.getTag();
                Toast.makeText(DeviceScrollCardActivity.this, d.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        return card;
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(Constants.EXTRA_DEVICE_LIST);
        mDevices = new ArrayList<BluetoothDevice>(parcelables.length);
        mCards = new ArrayList<Card>(parcelables.length);
        for (Parcelable p : parcelables) {
            BluetoothDevice device = (BluetoothDevice)p;

            mDevices.add(device);
            mCards.add(createCard(device));
        }

        mCardScrollView = new CardScrollView(this);
        mCardScrollView.setAdapter(new DeviceCardScrollAdapter());
        mCardScrollView.activate();
        mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = mDevices.get(position);
                Toast.makeText(DeviceScrollCardActivity.this, "selected " + device.getName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DeviceScrollCardActivity.this, CameraPreviewActivity.class);
                intent.putExtra(Constants.EXTRA_DEVICE, device);

                startActivity(intent);
            }
        });
        setContentView(mCardScrollView);
    }
}
