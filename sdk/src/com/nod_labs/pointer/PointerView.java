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

package com.nod_labs.pointer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PointerView extends ViewGroup {
    private final Map<String, Integer> mX = new HashMap<String, Integer>();
    private final Map<String, Integer> mY = new HashMap<String, Integer>();
    private final Map<String, Integer> mRadius = new HashMap<String, Integer>();
    private final Map<String, Integer> mA = new HashMap<String, Integer>();
    private final Map<String, Integer> mR = new HashMap<String, Integer>();
    private final Map<String, Integer> mG = new HashMap<String, Integer>();
    private final Map<String, Integer> mB = new HashMap<String, Integer>();
    private final Map<String, String>  mCaptions = new HashMap<String, String>();

    private final Map<String, Bitmap> mPointerBitmaps = new HashMap<String, Bitmap>();
    private final Map<String, Paint> mPointerPaints = new HashMap<String, Paint>();

    private static final int MAX_CAPTION_LEN = 6;
    private static final String TAG = PointerView.class.getSimpleName();

    private int mWindowWidth;
    private int mWindowHeight;

    public PointerView(Context context) {
        super(context);
    }

    private void updatePointerBitmap(String pointerId) {
        int radius = mRadius.get(pointerId);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setARGB(mA.get(pointerId), mR.get(pointerId), mG.get(pointerId), mB.get(pointerId));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(radius);
        mPointerPaints.put(pointerId, paint);

        int width = radius * 4;
        int height = radius * 4;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawCircle(width / 2, radius, radius, paint);

        String caption = mCaptions.get(pointerId);
        int len = Math.min(caption.length(), MAX_CAPTION_LEN);

        canvas.drawText(caption, 0, len, width / 2, radius * 3, mPointerPaints.get(pointerId));

        mPointerBitmaps.put(pointerId, bitmap);
    }

    public void setWindowBounds(int width, int height) {
        mWindowWidth = width;
        mWindowHeight = height;
    }

    public void addPointer(String pointerId, int radius, int a, int r, int g, int b, String caption) {
        mRadius.put(pointerId, radius);
        mA.put(pointerId, a);
        mR.put(pointerId, r);
        mG.put(pointerId, g);
        mB.put(pointerId, b);
        mCaptions.put(pointerId, caption);
        mX.put(pointerId, 0);
        mY.put(pointerId, 0);

        updatePointerBitmap(pointerId);
    }

    public void removePointer(String pointerId) {
        mRadius.remove(pointerId);
        mA.remove(pointerId);
        mR.remove(pointerId);
        mG.remove(pointerId);
        mB.remove(pointerId);
        mCaptions.remove(pointerId);
        mX.remove(pointerId);
        mY.remove(pointerId);

        mPointerBitmaps.remove(pointerId);
        mPointerPaints.remove(pointerId);
    }

    public void updatePointerPosition(String pointerId, int deltaX, int deltaY) {
        int x = mX.get(pointerId) + deltaX;
        int y = mY.get(pointerId) + deltaY;

        if (x < 0) {
            x = 0;
        } else if (x > mWindowWidth) {
            x = mWindowWidth;
        }

        if (y < 0) {
            y = 0;
        } else if (y > mWindowHeight) {
            y = mWindowHeight;
        }

        mX.put(pointerId, x);
        mY.put(pointerId, y);
    }

    public void updatePointerRadius(String pointerId, int radius) {
        mRadius.put(pointerId, radius);
        updatePointerBitmap(pointerId);
    }

    public void updatePointerColor(String pointerId, int a, int r, int g, int b) {
        mA.put(pointerId, a);
        mR.put(pointerId, r);
        mG.put(pointerId, g);
        mB.put(pointerId, b);

        updatePointerBitmap(pointerId);
    }

    public void updatePointerCaption(String pointerId, String caption) {
        mCaptions.put(pointerId, caption);
        updatePointerBitmap(pointerId);
    }

    public Point getCurrentPosition(String pointerId) {
        return new Point(mX.get(pointerId), mY.get(pointerId));
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Set<String> pointerIds = mPointerBitmaps.keySet();

        for (String pointerId : pointerIds) {
            canvas.drawBitmap(mPointerBitmaps.get(pointerId),
                    mX.get(pointerId) - (2 * mRadius.get(pointerId)),
                    mY.get(pointerId) - mRadius.get(pointerId),
                    mPointerPaints.get(pointerId));
        }
    }

    @Override
    public void onLayout(boolean changed, int l, int r, int t, int b) {
    }

}
