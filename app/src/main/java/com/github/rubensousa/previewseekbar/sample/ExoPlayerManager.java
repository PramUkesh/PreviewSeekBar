package com.github.rubensousa.previewseekbar.sample;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;

public class ExoPlayerManager {

    private ExoPlayerMediaSourceBuilder mediaSourceBuilder;
    private SimpleExoPlayerView playerView;
    private SimpleExoPlayerView previewPlayerView;
    private SimpleExoPlayer player;
    private SimpleExoPlayer previewPlayer;

    public ExoPlayerManager(SimpleExoPlayerView playerView, SimpleExoPlayerView previewPlayerView,
                            String url) {
        this.playerView = playerView;
        this.previewPlayerView = previewPlayerView;
        this.mediaSourceBuilder = new ExoPlayerMediaSourceBuilder(playerView.getContext(), url);
    }

    public void preview(float offset) {
        float offsetRounded = roundOffset(offset);
        player.setPlayWhenReady(false);
        previewPlayer.seekTo((long) (offsetRounded * previewPlayer.getDuration()));
        previewPlayer.setPlayWhenReady(false);
    }

    public void onStart() {
        if (Util.SDK_INT > 23) {
            createPlayers();
        }
    }

    public void onResume() {
        if ((Util.SDK_INT <= 23 || player == null || previewPlayer == null)) {
            createPlayers();
        }
    }

    public void onPause() {
        if (Util.SDK_INT <= 23) {
            releasePlayers();
        }
    }

    public void onStop() {
        if (Util.SDK_INT > 23) {
            releasePlayers();
        }
    }

    public void startPreview() {
        player.setPlayWhenReady(false);
    }

    public void stopPreview() {
        player.setPlayWhenReady(true);
    }

    private float roundOffset(float offset) {
        return (float) (Math.round(offset * Math.pow(10, 2)) / Math.pow(10, 2));
    }

    private void releasePlayers() {
        if (player != null) {
            player.release();
            player = null;
        }
        if (previewPlayer != null) {
            previewPlayer.release();
            previewPlayer = null;
        }
    }

    private void createPlayers() {
        player = createFullPlayer();
        playerView.setPlayer(player);
        previewPlayer = createPreviewPlayer();
        previewPlayerView.setPlayer(previewPlayer);
    }

    private SimpleExoPlayer createFullPlayer() {
        TrackSelection.Factory videoTrackSelectionFactory
                = new AdaptiveVideoTrackSelection.Factory(new DefaultBandwidthMeter());

        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        // 3. Create the player
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(playerView.getContext(),
                trackSelector, loadControl);
        player.setPlayWhenReady(true);
        player.prepare(mediaSourceBuilder.getMediaSourceHls());
        return player;
    }

    private SimpleExoPlayer createPreviewPlayer() {
        TrackSelection.Factory videoTrackSelectionFactory = new SingleVideoTrackSelection.Factory();

        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        // 3. Create the player
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(previewPlayerView.getContext(),
                trackSelector, loadControl);
        player.setPlayWhenReady(false);
        player.prepare(mediaSourceBuilder.getMediaSourceHls());
        return player;
    }
}
