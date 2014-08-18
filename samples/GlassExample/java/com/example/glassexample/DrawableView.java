/*
 * Copyright (C) 2014 Nod Labs
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

package com.example.glassexample;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.View;
import com.nod_labs.pointer.PointerService;
import net.openspatial.ButtonEvent;

import java.util.Vector;

public class DrawableView extends View implements PointerService.PointerViewCallback {
    private Vector<PointF> mPoints;
    private Vector<Vector<PointF>> mHistoricalPoints;
    private Paint mPaint;

    private final float STROKE_WIDTH = 10;
    private final String TAG = DrawableView.class.getSimpleName();


    public DrawableView(Context context) {
        super(context);
        mPoints = new Vector<PointF>();
        mHistoricalPoints = new Vector<Vector<PointF>>();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(STROKE_WIDTH);
        mPaint.setARGB(255, 255, 0, 0);
    }

    public void addPoint(float x, float y) {
        mPoints.add(new PointF(x, y));
    }

    public void clearPoints() {
        mHistoricalPoints.clear();
        mPoints.clear();
    }

    public void resetStart() {
        mHistoricalPoints.add(mPoints);
        mPoints = new Vector<PointF>();
    }

    private void draw(Canvas canvas, Vector<PointF> v) {
        Path path = new Path();

        int size = v.size();
        if (size == 0) {
            return;
        }

        path.moveTo(v.get(0).x, v.get(0).y);
        for (int i = 1; i < size; ++i) {
          path.lineTo(v.get(i).x, v.get(i).y);
        }
        canvas.drawPath(path, mPaint);
    }

    public void onDraw(Canvas canvas) {
        for (Vector<PointF> v : mHistoricalPoints) {
            draw(canvas, v);
        }
        draw(canvas, mPoints);
    }

    public void onButtonEvent(String pointerId, ButtonEvent event, int x, int y, PointerService service) {
        Log.d(TAG, "ButtonEvent of type: " + event.buttonEventType);
    }

    public void onClick(String pointerId, int x, int y, PointerService service) {
        Log.d(TAG, "onClick");
        resetStart();
    }

    public void onDrag(String pointerId, int x, int y, PointerService service) {
        Log.d(TAG, "on drag");
        addPoint(x, y);
        invalidate();
    }
}
