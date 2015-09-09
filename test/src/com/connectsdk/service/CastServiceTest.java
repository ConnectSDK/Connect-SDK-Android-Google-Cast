package com.connectsdk.service;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.LaunchOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import com.connectsdk.core.MediaInfo;
import com.connectsdk.core.SubtitleInfo;
import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.capability.MediaControl.DurationListener;
import com.connectsdk.service.capability.MediaControl.PositionListener;
import com.connectsdk.service.capability.MediaPlayer;
import com.connectsdk.service.capability.VolumeControl;
import com.connectsdk.service.capability.WebAppLauncher;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.config.ServiceConfig;
import com.connectsdk.service.config.ServiceDescription;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
@PrepareForTest({GoogleApiClient.class})
public class CastServiceTest {

    CastService service;

    CastService.CastClient castClient = Mockito.mock(CastService.CastClient.class);

    GoogleApiClient googleApiClient = PowerMockito.mock(GoogleApiClient.class);

    RemoteMediaPlayer mediaPlayer = Mockito.mock(RemoteMediaPlayer.class);

    ServiceDescription serviceDescription;

    class StubCastService extends CastService {

        public StubCastService(ServiceDescription serviceDescription,
                               ServiceConfig serviceConfig) {
            super(serviceDescription, serviceConfig);
            this.mCastClient = castClient;
        }

        protected GoogleApiClient createApiClient() {
            return googleApiClient;
        }

    }

    @Before
    public void setUp() {
        serviceDescription = mock(ServiceDescription.class);
        service = new StubCastService(serviceDescription, mock(ServiceConfig.class));
        Assert.assertNotNull(service);
    }

    @Test
    public void testConnect() {
        // Test desc.: connect creates mApiClient and invokes google api connect

        Assert.assertFalse(service.connected);
        Assert.assertNull(service.mApiClient);
        service.connect();

        Assert.assertNotNull(service.mApiClient);
        Assert.assertSame(googleApiClient, service.mApiClient);
        verify(googleApiClient).connect();
    }

    @Test
    public void testConnectShouldBeInvokedIfNotConnecting() {
        Mockito.when(googleApiClient.isConnecting()).thenReturn(Boolean.FALSE);
        Mockito.when(googleApiClient.isConnected()).thenReturn(Boolean.FALSE);
        service.connect();

        verify(googleApiClient, Mockito.times(1)).connect();
    }

    @Test
    public void testConnectShouldNotBeInvokedIfConnected() {
        Mockito.when(googleApiClient.isConnected()).thenReturn(Boolean.TRUE);
        service.connect();

        verify(googleApiClient, Mockito.times(0)).connect();
    }

    @Test
    public void testConnectShouldNotBeInvokedIfConnecting() {
        Mockito.when(googleApiClient.isConnecting()).thenReturn(Boolean.TRUE);
        service.connect();

        verify(googleApiClient, Mockito.times(0)).connect();
    }

    @Test
    public void testDisconnect() {
        // Test desc.: disconnect invokes google api disconnect

        service.connect();
        Assert.assertNotNull(service.mApiClient);

        setServiceConnected();

        service.disconnect();
        Assert.assertNull(service.mApiClient);

        verify(googleApiClient).disconnect();
    }

    @Test
    public void testDisconnectShouldBeInvokedWhenConnected() {
        // Test desc.: if service is not connected disconnect do nothing

        Assert.assertNull(service.mApiClient);
        service.disconnect();
        Assert.assertNull(service.mApiClient);

        verify(googleApiClient, Mockito.times(0)).disconnect();
    }

    @Test
    public void testPlay() {
        // Test desc.: should invoke player play

        service.mMediaPlayer = mediaPlayer;
        Mockito.when(googleApiClient.isConnected()).thenReturn(true);
        service.mApiClient = googleApiClient;
        ResponseListener<Object> listener = mock(ResponseListener.class);
        service.play(listener);

        verify(mediaPlayer).play(googleApiClient);
    }

    @Test
    public void testPause() {
        // Test desc.: should invoke player pause

        service.mMediaPlayer = mediaPlayer;
        Mockito.when(googleApiClient.isConnected()).thenReturn(true);
        service.mApiClient = googleApiClient;
        ResponseListener<Object> listener = mock(ResponseListener.class);
        service.pause(listener);

        verify(mediaPlayer).pause(googleApiClient);
    }

    @Test
    public void testStop() {
        // Test desc.: should invoke player stop

        service.mMediaPlayer = mediaPlayer;
        Mockito.when(googleApiClient.isConnected()).thenReturn(true);
        service.mApiClient = googleApiClient;
        ResponseListener<Object> listener = mock(ResponseListener.class);
        service.stop(listener);

        verify(mediaPlayer).stop(googleApiClient);
    }

    @Test
    public void testRewindNotImplemented() {
        // Test desc.: rewind should invoke error - "not supported"

        ResponseListener<Object> listener = mock(ResponseListener.class);
        service.rewind(listener);
        Robolectric.runUiThreadTasksIncludingDelayedTasks();
        verify(listener).onError(Mockito.any(ServiceCommandError.class));
    }


    @Test
    public void testFastForwardNotImplemented() {
        // Test desc.: fastForward should invoke error - "not supported"

        ResponseListener<Object> listener = mock(ResponseListener.class);
        service.fastForward(listener);
        Robolectric.runUiThreadTasksIncludingDelayedTasks();
        verify(listener).onError(Mockito.any(ServiceCommandError.class));
    }


    @Test
    public void testPreviousNotImplemented() {
        // Test desc.: previous should invoke error - "not supported"

        ResponseListener<Object> listener = mock(ResponseListener.class);
        service.previous(listener);
        Robolectric.runUiThreadTasksIncludingDelayedTasks();
        verify(listener).onError(Mockito.any(ServiceCommandError.class));
    }


    @Test
    public void testNextNotImplemented() {
        // Test desc.: next should invoke error - "not supported"

        ResponseListener<Object> listener = mock(ResponseListener.class);
        service.next(listener);
        Robolectric.runUiThreadTasksIncludingDelayedTasks();
        verify(listener).onError(Mockito.any(ServiceCommandError.class));
    }

    @Test
    public void testSeek() {
        // Test desc.: only if googleApi is connected and media player state is not null should invoke seek method

        // given
        long position = 10;
        ResponseListener<Object> listener = mock(ResponseListener.class);
        when(mediaPlayer.getMediaStatus()).thenReturn(mock(MediaStatus.class));
        service.mMediaPlayer = mediaPlayer;
        when(googleApiClient.isConnected()).thenReturn(true);
        service.mApiClient = googleApiClient;
        when(mediaPlayer.seek(googleApiClient, position, RemoteMediaPlayer.RESUME_STATE_UNCHANGED))
                .thenReturn(mock(PendingResult.class));

        // when
        service.seek(position, listener);

        // then
        verify(mediaPlayer).seek(googleApiClient, position, RemoteMediaPlayer
                .RESUME_STATE_UNCHANGED);
    }


    @Test
    public void testSeekWithEmptyMediaState() {

        long position = 10;
        ResponseListener<Object> listener = mock(ResponseListener.class);
        when(mediaPlayer.getMediaStatus()).thenReturn(null);
        service.mMediaPlayer = mediaPlayer;
        service.seek(position, listener);

        ArgumentCaptor<ServiceCommandError> errorArgument = ArgumentCaptor.forClass(ServiceCommandError.class);
        verify(mediaPlayer, times(0)).seek(googleApiClient, position, RemoteMediaPlayer.RESUME_STATE_UNCHANGED);
        verify(listener).onError(errorArgument.capture());

        Assert.assertEquals("There is no media currently available", errorArgument.getValue().getMessage());
    }

    @Test
    public void testGetDuration() {
        // Test desc.: should call getStreamDuration method and onSuccess

        // given
        when(mediaPlayer.getMediaStatus()).thenReturn(mock(MediaStatus.class));
        service.mMediaPlayer = mediaPlayer;
        DurationListener listener = mock(DurationListener.class);

        service.getDuration(listener);
        Robolectric.runUiThreadTasksIncludingDelayedTasks();

        verify(mediaPlayer).getStreamDuration();
        verify(listener).onSuccess(Mockito.any(Long.class));
    }

    @Test
    public void testGetPosition() {
        // Test desc.: should call getApproximateStreamPosition method and onSuccess

        // given
        when(mediaPlayer.getMediaStatus()).thenReturn(mock(MediaStatus.class));
        service.mMediaPlayer = mediaPlayer;
        PositionListener listener = mock(PositionListener.class);

        service.getPosition(listener);
        Robolectric.runUiThreadTasksIncludingDelayedTasks();

        verify(mediaPlayer).getApproximateStreamPosition();
        verify(listener).onSuccess(Mockito.any(Long.class));
    }

    @Test
    public void testGetMediaPlayer() {
        Assert.assertSame(service, service.getMediaPlayer());
    }

    @Test
    public void testCastDeviceShouldBeAssignedFromServiceDescription() {
        CastDevice device = Mockito.mock(CastDevice.class);
        service.connected = false;
        Mockito.when(serviceDescription.getDevice()).thenReturn(device);
        service.connect();
        Assert.assertSame(device, service.castDevice);
    }

    @Test
    public void testGetCapabilities() {
        Set<String> expectedCapabilities = new HashSet<String>(Arrays.asList(
                MediaPlayer.Display_Image,
                MediaPlayer.Play_Video,
                MediaPlayer.Play_Audio,
                MediaPlayer.Close,
                MediaPlayer.MetaData_Title,
                MediaPlayer.MetaData_Description,
                MediaPlayer.MetaData_Thumbnail,
                MediaPlayer.MetaData_MimeType,
                MediaPlayer.MediaInfo_Get,
                MediaPlayer.MediaInfo_Subscribe,

                VolumeControl.Volume_Get,
                VolumeControl.Volume_Set,
                VolumeControl.Volume_Up_Down,
                VolumeControl.Volume_Subscribe,
                VolumeControl.Mute_Get,
                VolumeControl.Mute_Set,
                VolumeControl.Mute_Subscribe,

                MediaControl.Play,
                MediaControl.Pause,
                MediaControl.Stop,
                MediaControl.Duration,
                MediaControl.Seek,
                MediaControl.Position,
                MediaControl.PlayState,
                MediaPlayer.Subtitle_WebVTT,
                MediaControl.PlayState_Subscribe,

                WebAppLauncher.Launch,
                WebAppLauncher.Message_Send,
                WebAppLauncher.Message_Receive,
                WebAppLauncher.Message_Send_JSON,
                WebAppLauncher.Message_Receive_JSON,
                WebAppLauncher.Connect,
                WebAppLauncher.Disconnect,
                WebAppLauncher.Join,
                WebAppLauncher.Close
        ));
        Set<String> capabilities = new HashSet<String>(service.getCapabilities());
        Assert.assertEquals(expectedCapabilities, capabilities);
    }

    @Test
    public void testPlayMedia() throws CastService.CastClientException {
        String mediaUrl = "http://media/";
        String mediaType = "video/mp4";
        MediaInfo mediaInfo = new MediaInfo.Builder(mediaUrl, mediaType)
                .build();

        com.google.android.gms.cast.MediaInfo media = verifyPlayMedia(mediaInfo);

        Assert.assertEquals(mediaUrl, media.getContentId());
        Assert.assertEquals(mediaType, media.getContentType());
    }

    @Test
    public void testPlayMediaWithSubtitles() throws CastService.CastClientException {
        String mediaUrl = "http://media/";
        String mediaType = "video/mp4";
        String subtitleUrl = "http://subtitle";

        MediaInfo mediaInfo = new MediaInfo.Builder(mediaUrl, mediaType)
                .setSubtitleInfo(new SubtitleInfo.Builder(subtitleUrl).build())
                .build();

        com.google.android.gms.cast.MediaInfo media = verifyPlayMedia(mediaInfo);
        MediaTrack track = media.getMediaTracks().get(0);

        Assert.assertEquals(mediaUrl, media.getContentId());
        Assert.assertEquals(mediaType, media.getContentType());
        Assert.assertEquals(subtitleUrl, track.getContentId());
        Assert.assertNull(track.getContentType());
    }

    @Test
    public void testPlayMediaWithAllParameters() throws CastService.CastClientException {
        String mediaUrl = "http://media/";
        String mediaType = "video/mp4";
        String subtitleUrl = "http://subtitle";
        String subtitleType = "text/vtt";
        String title = "title";
        String description = "description";
        String icon = "icon";
        String subtitleLang = "en";
        String subtitleName = "English";
        MediaInfo mediaInfo = new MediaInfo.Builder(mediaUrl, mediaType)
                .setTitle(title)
                .setDescription(description)
                .setIcon(icon)
                .setSubtitleInfo(new SubtitleInfo.Builder(subtitleUrl)
                        .setMimeType(subtitleType)
                        .setLanguage(subtitleLang)
                        .setLabel(subtitleName)
                        .build())
                .build();

        com.google.android.gms.cast.MediaInfo media = verifyPlayMedia(mediaInfo);
        MediaTrack track = media.getMediaTracks().get(0);
        MediaMetadata metadata = media.getMetadata();

        Assert.assertEquals(mediaUrl, media.getContentId());
        Assert.assertEquals(mediaType, media.getContentType());
        Assert.assertEquals(title, metadata.getString(MediaMetadata.KEY_TITLE));
        Assert.assertEquals(description, metadata.getString(MediaMetadata.KEY_SUBTITLE));
        Assert.assertEquals(icon, metadata.getImages().get(0).getUrl().toString());
        Assert.assertEquals(subtitleUrl, track.getContentId());
        Assert.assertEquals(subtitleType, track.getContentType());
        Assert.assertEquals(subtitleLang, track.getLanguage());
        Assert.assertEquals(subtitleName, track.getName());
        Assert.assertEquals(MediaTrack.TYPE_TEXT, track.getType());
        Assert.assertEquals(MediaTrack.SUBTYPE_SUBTITLES, track.getSubtype());
    }

    @Test
    public void testPlayMediaShouldNotCrashWhenCastThrowsException() throws CastService.CastClientException {
        verifyPlayMediaWhenCastThrowsException(CastService.CastClientException.class);
    }

    private void verifyPlayMediaWhenCastThrowsException(Class<? extends Throwable> exception) throws CastService.CastClientException {
        MediaInfo mediaInfo = new MediaInfo.Builder("http://host.com/", "video/mp4").build();
        MediaPlayer.LaunchListener listener = Mockito.mock(MediaPlayer.LaunchListener.class);
        Mockito.when(castClient.getApplicationStatus(Mockito.any(GoogleApiClient.class)))
                .thenThrow(exception);

        service.playMedia(mediaInfo, true, listener);
    }

    private com.google.android.gms.cast.MediaInfo verifyPlayMedia(MediaInfo mediaInfo) throws CastService.CastClientException {
        setServiceConnected();
        MediaPlayer.LaunchListener listener = Mockito.mock(MediaPlayer.LaunchListener.class);
        PendingResult<Cast.ApplicationConnectionResult> pendingResult
                = Mockito.mock(PendingResult.class);
        Mockito.when(castClient.launchApplication(Mockito.any(GoogleApiClient.class),
                        Mockito.anyString(), Mockito.any(LaunchOptions.class)))
                .thenReturn(pendingResult);


        // playMedia
        service.playMedia(mediaInfo, false, listener);
        Robolectric.runUiThreadTasksIncludingDelayedTasks();

        // CastApi.launchApplication
        Mockito.verify(castClient).launchApplication(Mockito.same(googleApiClient), Mockito.anyString(), Mockito.any(LaunchOptions.class));

        // ResultCallback.setResultCallback
        ArgumentCaptor<ResultCallback> argResultCallback =
                ArgumentCaptor.forClass(ResultCallback.class);
        Mockito.verify(pendingResult).setResultCallback(argResultCallback.capture());

        // ApplicationConnectionResultCallback.onResult
        ResultCallback resultCallback = argResultCallback.getValue();
        Cast.ApplicationConnectionResult result = Mockito.mock(Cast.ApplicationConnectionResult.class);
        Status status = Mockito.mock(Status.class);
        Mockito.when(status.isSuccess()).thenReturn(Boolean.TRUE);
        Mockito.when(result.getStatus()).thenReturn(status);
        ApplicationMetadata applicationMetadata = Mockito.mock(ApplicationMetadata.class);
        Mockito.when(result.getApplicationMetadata()).thenReturn(applicationMetadata);
        resultCallback.onResult(result);
        Robolectric.runUiThreadTasksIncludingDelayedTasks();

        // loadMedia
        ArgumentCaptor<com.google.android.gms.cast.MediaInfo> argMedia =
                ArgumentCaptor.forClass(com.google.android.gms.cast.MediaInfo.class);
        Mockito.verify(mediaPlayer).load(Mockito.same(googleApiClient), argMedia.capture(), Mockito.eq(true));
        return argMedia.getValue();
    }


    private void setServiceConnected() {
        service.connect();
        service.connected = true;
        Mockito.when(googleApiClient.isConnected()).thenReturn(true);
        service.mMediaPlayer = mediaPlayer;
    }
}
