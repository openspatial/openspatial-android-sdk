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

package net.openspatial;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Parcel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
@Config(manifest = Config.NONE, emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class PointerEventParcelableTest {
    private PointerEvent mPointerEvent;

    @Before
    public void setUp() {
        mPointerEvent = new PointerEvent(PointerEvent.PointerEventType.RELATIVE, 77, -99);
    }

    @After
    public void tearDown() {
        mPointerEvent = null;
    }

    @Test
    public void testParceling() {
        Parcel p = Parcel.obtain();

        mPointerEvent.writeToParcel(p, 0);
        p.setDataPosition(0);

        PointerEvent event = PointerEvent.CREATOR.createFromParcel(p);

        Assert.assertNotNull(event);
        Assert.assertEquals(mPointerEvent.eventType, event.eventType);
        Assert.assertEquals(mPointerEvent.timestamp, event.timestamp);
        Assert.assertEquals(mPointerEvent.pointerEventType, event.pointerEventType);
        Assert.assertEquals(mPointerEvent.x, event.x);
        Assert.assertEquals(mPointerEvent.y, event.y);
    }
}
