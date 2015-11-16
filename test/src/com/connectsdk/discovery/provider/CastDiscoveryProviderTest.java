package com.connectsdk.discovery.provider;

import android.content.Context;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;

import com.connectsdk.BuildConfig;
import com.connectsdk.discovery.DiscoveryProviderListener;
import com.connectsdk.service.config.ServiceDescription;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
@PrepareForTest({MediaRouter.class})
public class CastDiscoveryProviderTest {

    private CastDiscoveryProvider dp;

    private MediaRouter mediaRouter = PowerMockito.mock(MediaRouter.class);

    /**
     * CastDiscoveryProvider with injected MediaRouter object for testing behavior
     *
     * @author oleksii.frolov
     */
    class StubCastDiscoveryProvider extends CastDiscoveryProvider {

        public StubCastDiscoveryProvider(Context context) {
            super(context);

        }

        protected MediaRouter createMediaRouter(Context context) {
            return mediaRouter;
        }

    }

    @Before
    public void setUp() {
        dp = new StubCastDiscoveryProvider(RuntimeEnvironment.application);
        assertNotNull(dp);
    }

    @Test
    public void testStart() throws Exception {
        // TEST DESC.: start method should invoke MediaRouter removeCallback and addCalback
        // for stopping and starting services

        // when
        dp.start();

        // waiting for timer call
        Thread.sleep(200);
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        // then
        verify(mediaRouter).addCallback(any(MediaRouteSelector.class),
                any(MediaRouter.Callback.class), eq(MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY));
    }

    @Test
    public void testStop() throws Exception {
        // Test desc.: stop should invoke MediaRouter removeCallback

        // when
        dp.stop();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        // then
        verify(mediaRouter).removeCallback(any(MediaRouter.Callback.class));
    }

    @Test
    public void testReset() throws Exception {
        // Test desc.: reset method should stop discovering and clear found services

        // given
        dp.foundServices.put("service", mock(ServiceDescription.class));
        Assert.assertFalse(dp.foundServices.isEmpty());

        // when
        dp.reset();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        // then
        verify(mediaRouter).removeCallback(any(MediaRouter.Callback.class));
        Assert.assertTrue(dp.foundServices.isEmpty());
    }

    @Test
    public void testAddListener() {
        // Test desc.: there is no listeners by default, addListener should save listener
        DiscoveryProviderListener listener = mock(DiscoveryProviderListener.class);
        Assert.assertTrue(dp.serviceListeners.isEmpty());

        dp.addListener(listener);
        Assert.assertEquals(1, dp.serviceListeners.size());
    }

    @Test
    public void testRemoveListener() {
        // Test desc.: there is no listeners by default, addListener should save listener
        DiscoveryProviderListener listener = mock(DiscoveryProviderListener.class);
        Assert.assertTrue(dp.serviceListeners.isEmpty());

        dp.serviceListeners.add(listener);
        dp.removeListener(listener);
        Assert.assertTrue(dp.serviceListeners.isEmpty());
    }


}
