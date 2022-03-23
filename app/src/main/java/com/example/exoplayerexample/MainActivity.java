package com.example.exoplayerexample;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    PlayerView playerView;
    SimpleExoPlayer simpleExoPlayer;
    boolean playWhenReady = true;
    int currentWindow =0;
    long playbackPosition = 0L;
    Player.Listener playerListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPlayer();

    }

    public void initPlayer() {
        playerListener = getPlayerListener();
        playerView = findViewById(R.id.video_view);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
        trackSelector.setParameters(new DefaultTrackSelector.ParametersBuilder().setMaxVideoSizeSd());
        //DefaultTrackSelector -> is responsible for choose tracks in our media items ,
        //and set track selector on mode to picks tracks of standard defenition or lower to save user data
        simpleExoPlayer = new SimpleExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .build();
        playerView.setPlayer(simpleExoPlayer);
        MediaItem mp4Item = MediaItem.fromUri(getString(R.string.mp4_video));
        //Here, we need to use DASH adaptive streaming format with MIME Application_MPD type
        MediaItem mp4AdaptiveItem =new MediaItem.Builder()
                .setUri(getString(R.string.media_url_dash))
                .setMimeType(MimeTypes.APPLICATION_MPD)
                .build();

        simpleExoPlayer.setMediaItem(mp4AdaptiveItem);
        simpleExoPlayer.setPlayWhenReady(playWhenReady);
        simpleExoPlayer.seekTo(currentWindow,playbackPosition);
        simpleExoPlayer.addListener(playerListener);
        simpleExoPlayer.prepare();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24)
            initPlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        if (Util.SDK_INT < 24 || simpleExoPlayer == null)
            initPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24)
            releasePlayer();
    }

    private void releasePlayer() {
        currentWindow = playerView.getPlayer().getCurrentWindowIndex();
        playbackPosition = playerView.getPlayer().getCurrentPosition();
        playWhenReady = playerView.getPlayer().getPlayWhenReady();
        playerView.getPlayer().removeListener(playerListener);
        playerView.getPlayer().release();
        playerView.setPlayer(null);
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    public Player.Listener getPlayerListener(){
        return new Player.Listener(){
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                String checkState = null;
//                Player.Listener.super.onPlaybackStateChanged(playbackState);
                switch (playbackState){
                    case(ExoPlayer.STATE_READY):
                        checkState = "STATE_READY";
                        break;
                    case (ExoPlayer.STATE_BUFFERING):
                        checkState = "STATE_BUFFERING";
                        break;
                    case (ExoPlayer.STATE_IDLE):
                        checkState = "STATE_IDLE";
                        break;
                    case (ExoPlayer.STATE_ENDED):
                        checkState = "STATE_ENDED";
                        break;
                }
                Log.d(TAG, "onPlaybackStateChanged: "+checkState);
            }
        };
    }
}

