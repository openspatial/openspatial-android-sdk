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
public class Pose6DEventParcelableTest {
    private Pose6DEvent mPose6DEvent;

    @Before
    public void setUp() {
        mPose6DEvent = new Pose6DEvent(null, 7, 8, 9, 0.111f, 0.222f, 0.333f);
    }

    @After
    public void tearDown() {
        mPose6DEvent = null;
    }

    @Test
    public void testParceling() {
        Parcel p = Parcel.obtain();
        mPose6DEvent.writeToParcel(p, 0);
        p.setDataPosition(0);

        Pose6DEvent event = Pose6DEvent.CREATOR.createFromParcel(p);

        Assert.assertNotNull(event);
        Assert.assertEquals(mPose6DEvent.eventType, event.eventType);
        Assert.assertEquals(mPose6DEvent.timestamp, event.timestamp);
        Assert.assertEquals(mPose6DEvent.x, event.x, 0);
        Assert.assertEquals(mPose6DEvent.y, event.y, 0);
        Assert.assertEquals(mPose6DEvent.z, event.z, 0);
        Assert.assertEquals(mPose6DEvent.roll, event.roll, 0);
        Assert.assertEquals(mPose6DEvent.pitch, event.pitch, 0);
        Assert.assertEquals(mPose6DEvent.yaw, event.yaw, 0);
    }
}
