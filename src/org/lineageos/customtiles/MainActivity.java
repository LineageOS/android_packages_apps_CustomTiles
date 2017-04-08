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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.service.quicksettings.TileService;

import java.util.HashMap;

public class MainActivity extends Activity {

    private static HashMap<String, Intent> LONG_CLICK_ACTIONS = new HashMap<String, Intent>() {{
        put(".AdbOverNetworkTile", new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
        put(".AmbientDisplayTile", new Intent(Settings.ACTION_DISPLAY_SETTINGS));
        put(".BatterySaverTile", new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS));
        put(".HeadsUpTile", new Intent(Settings.ACTION_NOTIFICATION_SETTINGS));
        put(".SyncTile", new Intent(Settings.ACTION_SYNC_SETTINGS));
        put(".UsbTetherTile", new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        put(".VolumePanelTile", new Intent(Settings.ACTION_SOUND_SETTINGS));
        put(".LocationModeTile", new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        finish();

        Intent intent = getIntent();
        if (!intent.hasExtra(TileService.EXTRA_COMPONENT)) return;
        Bundle extras = intent.getExtras();
        ComponentName component = extras.getParcelable(TileService.EXTRA_COMPONENT);

        Intent targetIntent = LONG_CLICK_ACTIONS.get(component.getShortClassName());
        if (targetIntent != null) {
            startActivity(targetIntent);
        }
    }

}
