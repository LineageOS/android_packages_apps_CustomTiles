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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;

public class CaffeineTile extends CustomTileService {

    public static final String CAFFEINE_PREF = "caffeine_pref";

    private boolean mEnabled = false;

    private SharedPreferences mSharedPreferences;

    @Override
    public void onClick() {
        super.onClick();

        mSharedPreferences.edit().putBoolean(CAFFEINE_PREF, !mEnabled).apply();
        Context context = getApplicationContext();
        if (!mEnabled) {
            startServiceAsUser(new Intent(context, CaffeineTileService.class), UserHandle.CURRENT);
        } else {
            stopServiceAsUser(new Intent(context, CaffeineTileService.class), UserHandle.CURRENT);
        }
        refresh();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        refresh();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEnabled = mSharedPreferences.getBoolean(CAFFEINE_PREF, false);
    }

    private void refresh() {
        boolean enabled = mSharedPreferences.getBoolean(CAFFEINE_PREF, false);
        if (mEnabled == enabled) {
            return;
        }
        mEnabled = enabled;
        if (enabled) {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_caffeine_on));
            getQsTile().setState(Tile.STATE_ACTIVE);
        } else {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_caffeine_off));
            getQsTile().setState(Tile.STATE_INACTIVE);
        }
        getQsTile().updateTile();
    }

    @Override
    public int getInitialTileState() {
        return mEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;
    }

    @Override
    public Icon getInitialIcon() {
        return mEnabled ? Icon.createWithResource(this, R.drawable.ic_caffeine_on) : null;
    }
}
