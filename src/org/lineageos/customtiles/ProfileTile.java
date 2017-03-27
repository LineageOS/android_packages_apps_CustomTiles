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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import java.util.UUID;

import cyanogenmod.app.Profile;
import cyanogenmod.app.ProfileManager;
import cyanogenmod.providers.CMSettings;

public class ProfileTile extends TileService {

    private ProfileManager mProfileManager;

    private Profile mCurrentProfile;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    };

    @Override
    public void onStartListening() {
        super.onStartListening();

        mProfileManager = ProfileManager.getInstance(getBaseContext());

        IntentFilter filter = new IntentFilter();
        filter.addAction(ProfileManager.INTENT_ACTION_PROFILE_SELECTED);
        filter.addAction(ProfileManager.INTENT_ACTION_PROFILE_UPDATED);
        filter.addAction(ProfileManager.PROFILES_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);

        refresh();
    }

    @Override
    public void onStopListening() {
        unregisterReceiver(mReceiver);
        super.onStopListening();
    }

    private Dialog selectProfileDialog() {

        Profile[] profilesList = mProfileManager.getProfiles();
        if (profilesList.length == 0) {
            return new AlertDialog.Builder(getBaseContext())
                    .setTitle(R.string.dialog_system_profile_title)
                    .setMessage(R.string.dialog_system_profile_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        }

        final CharSequence[] profileLabels = new CharSequence[profilesList.length];
        final UUID[] profileUuids = new UUID[profilesList.length];
        int selectedProfile = -1;
        for (int i = 0; i < profilesList.length; i++) {
            UUID profileUuuid = profilesList[i].getUuid();
            profileLabels[i] = profilesList[i].getName();
            profileUuids[i] = profileUuuid;
            if (mCurrentProfile != null && mCurrentProfile.getUuid().equals(profileUuuid)) {
                selectedProfile = i;
            }
        }

        return new AlertDialog.Builder(getBaseContext())
                .setTitle(R.string.dialog_system_profile_title)
                .setSingleChoiceItems(profileLabels, selectedProfile,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int index) {
                                if (!mProfileManager.isProfilesEnabled()) {
                                    enableProfiles();
                                }
                                mProfileManager.setActiveProfile(profileUuids[index]);
                                dialogInterface.dismiss();
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .create();
    }

    @Override
    public void onClick() {
        super.onClick();

        unlockAndRun(new Runnable() {
            @Override
            public void run() {
                showDialog(selectProfileDialog());
            }
        });
    }

    private void refresh() {
        if (mProfileManager.isProfilesEnabled()) {
            mCurrentProfile = mProfileManager.getActiveProfile();
            getQsTile().setState(Tile.STATE_ACTIVE);
            getQsTile().setLabel(mCurrentProfile.getName());
        } else {
            mCurrentProfile = null;
            getQsTile().setState(Tile.STATE_INACTIVE);
            getQsTile().setLabel(getString(R.string.system_profile_label));
        }
        getQsTile().updateTile();
    }

    private void enableProfiles() {
        CMSettings.System.putInt(getContentResolver(),
                CMSettings.System.SYSTEM_PROFILES_ENABLED, 1);
    }
}
