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
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
@Config(manifest = Config.NONE, emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class OpenSpatialServiceTests {
    private OpenSpatialService mService;
    private final OpenSpatialEvent.EventListener mListener = mock(OpenSpatialEvent.EventListener.class);
    private final BluetoothDevice mDevice = mock(BluetoothDevice.class);

    @Before
    public void setUp() {
        mService = new OpenSpatialService();
        mService.onCreate();
    }

    @After
    public void tearDown() {
        System.out.println("tearDown called");
        mService.stopSelf();
        mService = null;
    }

    @Test
    public void testRegister() throws OpenSpatialException {
        mService.registerForButtonEvents(mDevice, mListener);
        mService.registerForPointerEvents(mDevice, mListener);
        mService.registerForPose6DEvents(mDevice, mListener);
        mService.registerForGestureEvents(mDevice, mListener);
    }

    @Test(expected = OpenSpatialException.class)
    public void testDoubleRegister() throws OpenSpatialException {
        mService.registerForButtonEvents(mDevice, mListener);

        try {
            OpenSpatialEvent.EventListener listener = mock(OpenSpatialEvent.EventListener.class);
            mService.registerForButtonEvents(mDevice, listener);
        } catch (OpenSpatialException e) {
            Assert.assertEquals(OpenSpatialException.ErrorCode.DEVICE_ALREADY_REGISTERED, e.getErrorCode());
            throw e;
        }
    }

    @Test
    public void testunRegister() throws OpenSpatialException {
        mService.registerForButtonEvents(mDevice, mListener);
        mService.registerForPointerEvents(mDevice, mListener);
        mService.registerForPose6DEvents(mDevice, mListener);
        mService.registerForGestureEvents(mDevice, mListener);

        mService.unRegisterForButtonEvents(mDevice);
        mService.unRegisterForPointerEvents(mDevice);
        mService.unRegisterForPose6DEvents(mDevice);
        mService.unRegisterForGestureEvents(mDevice);
    }

    @Test(expected = OpenSpatialException.class)
    public void testUnregisterWithoutRegister() throws OpenSpatialException {
        try {
            mService.unRegisterForButtonEvents(mDevice);
        } catch (OpenSpatialException e) {
            Assert.assertEquals(OpenSpatialException.ErrorCode.DEVICE_NOT_REGISTERED, e.getErrorCode());
            throw e;
        }
    }

    private boolean verifyOpenSpatialEvent(OpenSpatialEvent expected, OpenSpatialEvent actual) {
        return (expected.eventType == actual.eventType &&
                expected.timestamp == actual.timestamp);
    }

    @Test
    public void testButtonEventDelivery() throws OpenSpatialException {
        mService.registerForButtonEvents(mDevice, mListener);

        final ButtonEvent sendEvent = new ButtonEvent(mDevice, ButtonEvent.ButtonEventType.TOUCH1_UP);
        Intent i = new Intent();
        i.putExtra(OpenSpatialConstants.BLUETOOTH_DEVICE, mDevice);
        i.putExtra(OpenSpatialConstants.OPENSPATIAL_EVENT, sendEvent);
        mService.processEventIntent(i);

        verify(mListener).onEventReceived(argThat(new ArgumentMatcher<OpenSpatialEvent>() {
            @Override
            public boolean matches(Object o) {
                ButtonEvent event = (ButtonEvent)o;

                return (verifyOpenSpatialEvent(sendEvent, event) &&
                    sendEvent.buttonEventType == event.buttonEventType);
            }
        }));
    }

    @Test
    public void testPointerEventDelivery() throws OpenSpatialException {
        mService.registerForPointerEvents(mDevice, mListener);

        final PointerEvent sendEvent = new PointerEvent(mDevice, PointerEvent.PointerEventType.RELATIVE, 77, -88);
        Intent i = new Intent();
        i.putExtra(OpenSpatialConstants.BLUETOOTH_DEVICE, mDevice);
        i.putExtra(OpenSpatialConstants.OPENSPATIAL_EVENT, sendEvent);
        mService.processEventIntent(i);

        verify(mListener).onEventReceived(argThat(new ArgumentMatcher<OpenSpatialEvent>() {
            @Override
            public boolean matches(Object o) {
                PointerEvent event = (PointerEvent)o;

                return (verifyOpenSpatialEvent(sendEvent, event) &&
                        sendEvent.pointerEventType == event.pointerEventType &&
                        sendEvent.x == event.x &&
                        sendEvent.y == event.y);
            }
        }));
    }

    @Ignore
    @Test
    public void testPose6DEventDelivery() throws OpenSpatialException {
        mService.registerForPose6DEvents(mDevice, mListener);

        final Pose6DEvent sendEvent = new Pose6DEvent(mDevice, 1, 2, 3, .444f, .555f, .666f);
        Intent i = new Intent();
        i.putExtra(OpenSpatialConstants.BLUETOOTH_DEVICE, mDevice);
        i.putExtra(OpenSpatialConstants.OPENSPATIAL_EVENT, sendEvent);
        mService.processEventIntent(i);

        verify(mListener).onEventReceived(argThat(new ArgumentMatcher<OpenSpatialEvent>() {
            @Override
            public boolean matches(Object o) {
                Pose6DEvent event = (Pose6DEvent)o;

                return (verifyOpenSpatialEvent(sendEvent, event) &&
                        sendEvent.x == event.x &&
                        sendEvent.y == event.y &&
                        sendEvent.z == event.z &&
                        sendEvent.roll == event.roll &&
                        sendEvent.pitch == event.pitch &&
                        sendEvent.yaw == event.yaw);
            }
        }));
    }

    private boolean verifyGestureEvent(GestureEvent expected, GestureEvent actual) {
        return (verifyOpenSpatialEvent(expected, actual) &&
                expected.gestureEventType == actual.gestureEventType &&
                expected.magnitude == actual.magnitude);
    }

    @Test
    public void testGestureEventDelivery() throws OpenSpatialException {
        mService.registerForGestureEvents(mDevice, mListener);

        final GestureEvent sendEvent = new GestureEvent(mDevice, GestureEvent.GestureEventType.SWIPE_DOWN, 1.1);
        Intent i = new Intent();
        i.putExtra(OpenSpatialConstants.BLUETOOTH_DEVICE, mDevice);
        i.putExtra(OpenSpatialConstants.OPENSPATIAL_EVENT, sendEvent);
        mService.processEventIntent(i);

        verify(mListener).onEventReceived(argThat(new ArgumentMatcher<OpenSpatialEvent>() {
            @Override
            public boolean matches(Object o) {
                GestureEvent event = (GestureEvent)o;

                return verifyGestureEvent(sendEvent, event);
            }
        }));
    }

    @Test
    public void testDoubleGestureEventDelivery() throws OpenSpatialException {
        mService.registerForGestureEvents(mDevice, mListener);

        final GestureEvent downEvent = new GestureEvent(mDevice, GestureEvent.GestureEventType.SWIPE_DOWN, 1.1);
        final GestureEvent upEvent = new GestureEvent(mDevice, GestureEvent.GestureEventType.SWIPE_UP, 1.1);

        Intent i = new Intent();
        i.putExtra(OpenSpatialConstants.BLUETOOTH_DEVICE, mDevice);
        i.putExtra(OpenSpatialConstants.OPENSPATIAL_EVENT, downEvent);
        mService.processEventIntent(i);

        verify(mListener).onEventReceived(argThat(new ArgumentMatcher<OpenSpatialEvent>() {
            @Override
            public boolean matches(Object o) {
                GestureEvent event = (GestureEvent)o;

                return verifyGestureEvent(downEvent, event);
            }
        }));

        i = new Intent();
        i.putExtra(OpenSpatialConstants.BLUETOOTH_DEVICE, mDevice);
        i.putExtra(OpenSpatialConstants.OPENSPATIAL_EVENT, upEvent);
        mService.processEventIntent(i);

        verify(mListener).onEventReceived(argThat(new ArgumentMatcher<OpenSpatialEvent>() {
            @Override
            public boolean matches(Object o) {
                GestureEvent event = (GestureEvent)o;

                return verifyGestureEvent(upEvent, event);
            }
        }));
    }
}
