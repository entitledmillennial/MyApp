package com.hz.myapp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

/**
 * Created by zeee on 05-04-2018.
 */

public class PlayerActivity extends YouTubeBaseActivity {
    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer youTubePlayer;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_player);

        Intent intent = getIntent();
        final String videoID = intent.getStringExtra("videoID");

        youTubePlayerView = findViewById(R.id.player_view);
        youTubePlayerView.initialize(getString(R.string.developer_key), new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                PlayerActivity.this.youTubePlayer = youTubePlayer;
                PlayerActivity.this.youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION);
                PlayerActivity.this.youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE);
                PlayerActivity.this.youTubePlayer.loadVideo(videoID);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(youTubePlayer != null){
            youTubePlayer.release();
        }
    }
}
