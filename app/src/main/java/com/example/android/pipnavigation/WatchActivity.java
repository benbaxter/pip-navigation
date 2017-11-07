/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.pipnavigation;

import android.app.PictureInPictureParams;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.widget.ScrollView;

import com.example.android.pipnavigation.widget.MovieView;

/**
 * Demonstrates watching a video from an activity that can enter PIP mode and handle navigation
 * backwards when the PIP window has been restored.
 */
public class WatchActivity extends AppCompatActivity {

    private final PictureInPictureParams.Builder mPictureInPictureParamsBuilder =
            new PictureInPictureParams.Builder();

    private MovieView mMovieView;

    private ScrollView mScrollView;

    private boolean mBackstackLost;

    private final View.OnClickListener mOnClickListener =
            view -> {
                switch (view.getId()) {
                    case R.id.pip:
                        minimize();
                        break;
                }
            };

    private MovieView.MovieListener mMovieListener =
            new MovieView.MovieListener() {

                @Override
                public void onMovieStarted() {
                    // Not implemented
                }

                @Override
                public void onMovieStopped() {
                    // Not implemented
                }

                @Override
                public void onMovieMinimized() {
                    // The MovieView wants us to minimize it. We enter Picture-in-Picture mode now.
                    minimize();
                }
            };

    public static Intent createIntent(Context context) {
        return new Intent(context, WatchActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watcher);

        mMovieView = findViewById(R.id.movie);
        mScrollView = findViewById(R.id.scroll);

        // Set up the video; it automatically starts.
        mMovieView.setMovieListener(mMovieListener);
        findViewById(R.id.pip).setOnClickListener(mOnClickListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // On entering Picture-in-Picture mode, onPause is called, but not onStop.
        // For this reason, this is the place where we should pause the video playback.
        mMovieView.pause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!isInPictureInPictureMode()) {
            // Show the video controls so the video can be easily resumed.
            mMovieView.showControls();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustFullScreen(newConfig);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            adjustFullScreen(getResources().getConfiguration());
        }
    }

    private void adjustFullScreen(Configuration config) {
        final View decorView = getWindow().getDecorView();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            mScrollView.setVisibility(View.GONE);
            mMovieView.setAdjustViewBounds(false);
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            mScrollView.setVisibility(View.VISIBLE);
            mMovieView.setAdjustViewBounds(true);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        minimize();
    }

    void minimize() {
        if (mMovieView == null) {
            return;
        }
        // Hide the controls in picture-in-picture mode.
        mMovieView.hideControls();
        // Calculate the aspect ratio of the PiP screen.
        Rational aspectRatio = new Rational(mMovieView.getWidth(), mMovieView.getHeight());
        mPictureInPictureParamsBuilder.setAspectRatio(aspectRatio);
        enterPictureInPictureMode(mPictureInPictureParamsBuilder.build());
    }

    @Override
    public void onPictureInPictureModeChanged(
            boolean isInPictureInPictureMode, Configuration configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, configuration);
        Log.d("PIP", "is pip: " + isInPictureInPictureMode);
        if (!isInPictureInPictureMode) {
            mBackstackLost = true;
            // Show the video controls if the video is not playing
            if (mMovieView != null && !mMovieView.isPlaying()) {
                mMovieView.showControls();
            }
        }
    }

    @Override
    public void finish() {
        if (mBackstackLost) {
            finishAndRemoveTask();
            startActivity(
                    Intent.makeRestartActivityTask(
                            new ComponentName(this, MockHomeActivity.class)));
        } else {
            super.finish();
        }
    }
}
