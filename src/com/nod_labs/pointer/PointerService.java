/*
 * Copyright 2014 Nod Labs
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

package com.nod_labs.pointer;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.*;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.google.inject.Inject;
import roboguice.service.RoboService;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class PointerService extends RoboService {
    private PointerView mPointerView;
    private final IBinder mBinder = new PointerServiceBinder();

    @Inject
    private WindowManager mWindowManager;
    @Inject
    Application mApplication;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();

        mPointerView = new PointerView(mApplication);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowManager.addView(mPointerView, params);
        Point point = new Point();
        mWindowManager.getDefaultDisplay().getSize(point);
        mPointerView.setWindowBounds(point.x, point.y);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(mPointerView);
    }

    public void addPointer(final String pointerId,
                           final int radius,
                           final int a,
                           final int r,
                           final int g,
                           final int b,
                           final String caption) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPointerView.addPointer(pointerId, radius, a, r, g, b, caption);
                mPointerView.invalidate();
            }
        });
    }

    public void removePointer(final String pointerId) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPointerView.removePointer(pointerId);
                mPointerView.invalidate();
            }
        });
    }

    public void updatePointerPosition(final String pointerId, final int deltaX, final int deltaY) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPointerView.updatePointerPosition(pointerId, deltaX, deltaY);
                mPointerView.invalidate();
            }
        });
    }

    public void updatePointerRadius(final String pointerId, final int radius) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPointerView.updatePointerRadius(pointerId, radius);
                mPointerView.invalidate();
            }
        });
    }

    public void updatePointerColor(final String pointerId, final int a, final int r, final int g, final int b) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPointerView.updatePointerColor(pointerId, a, r, g, b);
                mPointerView.invalidate();
            }
        });
    }

    public void updatePointerCaption(final String pointerId, final String caption) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPointerView.updatePointerCaption(pointerId, caption);
                mPointerView.invalidate();
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class PointerServiceBinder extends Binder {
        public PointerService getService() {
            return PointerService.this;
        }
    }
}
