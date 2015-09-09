/*
 * CastServiceChannelTest
 * Connect SDK
 *
 * Copyright (c) 2015 LG Electronics.
 * Created by Oleksii Frolov on 23 Jul 2015
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
package com.connectsdk.service.google_cast;

import com.connectsdk.service.sessions.CastWebAppSession;
import com.connectsdk.service.sessions.WebAppSessionListener;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class CastServiceChannelTest {

    private CastServiceChannel channel;
    private CastWebAppSession session;

    @Before
    public void setUp() {
        session = Mockito.mock(CastWebAppSession.class);
        channel = new CastServiceChannel("id", session);
    }

    @Test
    public void testGetNamespace() {
        Assert.assertEquals("urn:x-cast:com.connectsdk", channel.getNamespace());
    }

    @Test
    public void testSendMessageWithNullValues() {
        WebAppSessionListener listener = Mockito.mock(WebAppSessionListener.class);
        Mockito.when(session.getWebAppSessionListener()).thenReturn(listener);
        channel.onMessageReceived(null, null, null);
        Robolectric.runUiThreadTasksIncludingDelayedTasks();

        Mockito.verify(listener).onReceiveMessage(session, null);
    }

    @Test
    public void testSendMessageWithCustomData() {
        final String content = "message";
        WebAppSessionListener listener = Mockito.mock(WebAppSessionListener.class);
        Mockito.when(session.getWebAppSessionListener()).thenReturn(listener);
        channel.onMessageReceived(null, null, content);
        Robolectric.runUiThreadTasksIncludingDelayedTasks();

        ArgumentCaptor<Object> argMessage = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(listener).onReceiveMessage(Mockito.same(session), argMessage.capture());
        Assert.assertEquals(content, argMessage.getValue());
    }

    @Test
    public void testSendMessageWithJSONData() throws JSONException {
        final String content = "{'key':'message'}";
        WebAppSessionListener listener = Mockito.mock(WebAppSessionListener.class);
        Mockito.when(session.getWebAppSessionListener()).thenReturn(listener);
        channel.onMessageReceived(null, null, content);
        Robolectric.runUiThreadTasksIncludingDelayedTasks();

        ArgumentCaptor<Object> argMessage = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(listener).onReceiveMessage(Mockito.same(session), argMessage.capture());
        Assert.assertEquals(new JSONObject(content).toString(), argMessage.getValue().toString());
        Assert.assertEquals(JSONObject.class, argMessage.getValue().getClass());
    }

    @Test
    public void testSendMessageWithNullSessionListener() {
        WebAppSessionListener listener = Mockito.mock(WebAppSessionListener.class);
        Mockito.when(session.getWebAppSessionListener()).thenReturn(listener);
        try {
            Robolectric.getUiThreadScheduler().pause();
            channel.onMessageReceived(null, null, null);
            // modify session for checking pending UI task
            Mockito.when(session.getWebAppSessionListener()).thenReturn(null);
            Robolectric.runUiThreadTasksIncludingDelayedTasks();
        } catch (RuntimeException e) {
            Assert.fail("onMessageReceived should not thrown an Exception");
        }
    }
}
