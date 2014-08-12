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
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
@Config(manifest = Config.NONE, emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Motion6DEventParcelableTest {
    private Motion6DEvent mMotion6DEvent;

    @Before
    public void setUp() {
        mMotion6DEvent = new Motion6DEvent(null, 1.77f, 1.654f, 0.999f, 0.111f, 0.222f, 0.333f);
    }

    @After
    public void tearDown() {
        mMotion6DEvent = null;
    }

    @Test
    public void testParceling() {
        Parcel p = Parcel.obtain();
        mMotion6DEvent.writeToParcel(p, 0);
        p.setDataPosition(0);

        Motion6DEvent event = Motion6DEvent.CREATOR.createFromParcel(p);

        Assert.assertNotNull(event);
        Assert.assertEquals(mMotion6DEvent.eventType, event.eventType);
        Assert.assertEquals(mMotion6DEvent.timestamp, event.timestamp);
        Assert.assertEquals(mMotion6DEvent.accelX, event.accelX, 0);
        Assert.assertEquals(mMotion6DEvent.accelY, event.accelY, 0);
        Assert.assertEquals(mMotion6DEvent.accelZ, event.accelZ, 0);
        Assert.assertEquals(mMotion6DEvent.gyroX, event.gyroX, 0);
        Assert.assertEquals(mMotion6DEvent.gyroY, event.gyroY, 0);
        Assert.assertEquals(mMotion6DEvent.gyroZ, event.gyroZ, 0);
    }
}
