/*
 * Copyright (C) 2017 The LineageOS Project
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
package org.lineageos.customtiles;

import android.annotation.Nullable;
import android.app.ListActivity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.internal.util.ArrayUtils;

import java.util.Set;

public class ScreenTimeoutTile extends TileService {
    private static final String TAG = "ScreenTimeoutTile";
    private static final boolean DBG = false;

    private static final String TIMEOUT_ENTRIES_NAME = "screen_timeout_entries";
    private static final String TIMEOUT_VALUES_NAME = "screen_timeout_values";
    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";

    public static final String CONFIGURATION_INTENT = "org.lineageos.customtiles.SCREEN_TIMEOUT_CONFIG";

    public static final String SETTINGS_STORAGE_KEY = "enabled_screen_timeouts";

    final Handler mHandler = new Handler();

    private static String[] mTimeoutEntries, mTimeoutValues;

    private static boolean[] mTimeoutEnabledEntries;

    @Override
    public void onCreate() {
        setupTimeoutEntries(this);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT),
                false, mObserver);
        update();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();

        getContentResolver().unregisterContentObserver(mObserver);
    }

    private Runnable mTimedUpdate = new Runnable() {
        @Override
        public void run() {
            int newIndex = getScreenTimeoutIndex();

            if (DBG) Log.d(TAG, "Old timeout: " + newIndex);

            // Loop to search for an enabled timeout. If they're all off, go back to the previous item.
            int remainingItems = mTimeoutValues.length;
            while (--remainingItems >= 0) {
                newIndex++;
                if (newIndex >= mTimeoutValues.length) {
                    newIndex = 0;
                }
                if (mTimeoutEnabledEntries[newIndex]) {
                    break;
                }
            }

            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT, Integer.parseInt(mTimeoutValues[newIndex]));

            if (DBG) Log.d(TAG, "New timeout " + mTimeoutEntries[newIndex] + " (index " + newIndex + ")");
        }
    };

    @Override
    public void onClick() {
        super.onClick();
        if (DBG) Log.d(TAG, "onClick()");

        if (!mHandler.hasCallbacks(mTimedUpdate)) {
            mHandler.postDelayed(mTimedUpdate, 100);
        }
    }

    private void update() {
        int timeoutIndex = getScreenTimeoutIndex();
        CharSequence label = mTimeoutEntries[timeoutIndex];

        Tile tile = getQsTile();
        tile.setIcon(Icon.createWithResource(this, R.drawable.ic_qs_screen_timeout_vector));
        tile.setContentDescription(getString(R.string.screen_timeout_label, label));
        tile.setLabel(getString(R.string.screen_timeout_label, label));
        tile.setState(Tile.STATE_INACTIVE);
        tile.updateTile();

        if (DBG) Log.d(TAG, "Updated to '" + tile.getContentDescription() + "' (" + mTimeoutValues[timeoutIndex] + "s, index " +
                timeoutIndex + ")");
    }

    private int getScreenTimeoutIndex() {
        int currentValue = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0);

        for (int idx = 0 ; idx < mTimeoutValues.length ; idx++) {
            if (mTimeoutValues[idx].equals("" + currentValue)) {
                return idx;
            }
        }

        Log.w(TAG, "Cannot find timeout value " + currentValue + " in settings list");
        return 0;
    }

    private static void setupTimeoutEntries(Context context) {
        try {
            Resources resources = context.createPackageContext(SETTINGS_PACKAGE_NAME, 0).getResources();
            int id = resources.getIdentifier(TIMEOUT_ENTRIES_NAME, "array", SETTINGS_PACKAGE_NAME);
            if (id <= 0) {
                return;
            }
            mTimeoutEntries = resources.getStringArray(id);
            id = resources.getIdentifier(TIMEOUT_VALUES_NAME, "array", SETTINGS_PACKAGE_NAME);
            if (id <= 0) {
                return;
            }
            mTimeoutValues = resources.getStringArray(id);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        boolean noEnabledChoices = true;
        mTimeoutEnabledEntries = new boolean[mTimeoutValues.length];
        Set<String> enabledIntervals = context.getSharedPreferences(context.getPackageName(), MODE_PRIVATE)
                .getStringSet(SETTINGS_STORAGE_KEY, null);
        if (enabledIntervals != null) {
            if (DBG) Log.d(TAG, "Saved values: " + enabledIntervals);
            for (String item : enabledIntervals) {
                int pos = ArrayUtils.indexOf(mTimeoutValues, item);
                if (pos != -1) {
                    noEnabledChoices = false;
                    mTimeoutEnabledEntries[pos] = true;
                }
            }
        }
        if (enabledIntervals == null || noEnabledChoices) {
            if (DBG) Log.d(TAG, "No saved values, enabling all options");
            for (int i = 0 ; i < mTimeoutValues.length ; i++) {
                mTimeoutEnabledEntries[i] = true;
            }
        }
    }

    private ContentObserver mObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            update();
        }
    };

    public static class ConfigDialog extends ListActivity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setupTimeoutEntries(this);

            setContentView(R.layout.layout_qs_screentimeout);
            setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, mTimeoutEntries));

            ListView listView = getListView();
            for (int i = 0 ; i < mTimeoutEnabledEntries.length ; i++) {
                listView.setItemChecked(i, mTimeoutEnabledEntries[i]);
            }
        }

        @Override
        protected void onPause() {
            super.onPause();
            ListView listView = getListView();

            ArraySet<String> enabledIntervals = new ArraySet<>(mTimeoutEntries.length);
            SparseBooleanArray checkedListItems = listView.getCheckedItemPositions();
            for (int i = 0 ; i < mTimeoutEntries.length ; i++) {
                if (checkedListItems.get(i)) {
                    enabledIntervals.add(mTimeoutValues[i]);
                }
            }

            getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putStringSet(SETTINGS_STORAGE_KEY, enabledIntervals).apply();
        }
    }
}
