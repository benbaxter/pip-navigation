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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Manages navigation between fragments. If the user is watching a video, they can enter PIP mode
 * upon hitting the home button.
 */
public class NavigationWithFragmentsActivity extends AppCompatActivity
        implements HomeFragment.OnFragmentInteractionListener,
                DetailsFragment.OnFragmentInteractionListener {

    private static final String BACK_STACK_ROOT_TAG = "root_fragment";

    HomeFragment homeFragment;
    DetailsFragment detailsFragment;
    WatchFragment watchFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_navigation);

        homeFragment = HomeFragment.newInstance();

        getFragmentManager()
                .beginTransaction()
                .add(R.id.container, homeFragment)
                .addToBackStack(BACK_STACK_ROOT_TAG)
                .commit();
    }

    @Override
    public void launchDetails() {
        if (detailsFragment == null) {
            detailsFragment = DetailsFragment.newInstance();
        }

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, detailsFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void launchWatch() {
        startPlaying();
    }

    @Override
    public void startPlaying() {
        if (watchFragment == null) {
            watchFragment = WatchFragment.newInstance();
        }
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, watchFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (watchFragment != null) {
            watchFragment.minimize();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_switcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_activity || itemId == R.id.menu_fragment) {
            Class<?> clazz =
                    itemId == R.id.menu_activity
                            ? MockHomeActivity.class
                            : NavigationWithFragmentsActivity.class;
            finish();
            startActivity(new Intent(this, clazz));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
