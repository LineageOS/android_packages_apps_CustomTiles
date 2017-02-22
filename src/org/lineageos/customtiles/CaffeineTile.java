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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.SystemClock;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class CaffeineTile extends TileService {

    private PowerManager.WakeLock mWakeLock;
    private int mSecondsRemaining;
    private int mDuration;
    private static int[] DURATIONS = new int[] {
        5 * 60,   // 5 min
        10 * 60,  // 10 min
        30 * 60,  // 30 min
        -1,       // infinity
    };
    private CountDownTimer mCountdownTimer = null;
    public long mLastClickTime = -1;

    @Override
    public void onStartListening() {
        super.onStartListening();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
                "CaffeineTile");

        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);

        refresh();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();

        stopCountDown();
        unregisterReceiver(mReceiver);
        if (mWakeLock.isHeld())
            mWakeLock.release();
    }

    @Override
    public void onClick() {
        super.onClick();

        // If last user clicks < 5 seconds
        // we cycle different duration
        // otherwise toggle on/off
        if (mWakeLock.isHeld() && (mLastClickTime != -1) &&
                (SystemClock.elapsedRealtime() - mLastClickTime < 5000)) {
            // cycle duration
            mDuration++;
            if (mDuration >= DURATIONS.length) {
                // all durations cycled, turn if off
                mDuration = -1;
                stopCountDown();
                if (mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
            } else {
                // change duration
                startCountDown(DURATIONS[mDuration]);
                if (!mWakeLock.isHeld()) {
                    mWakeLock.acquire();
                }
            }
        } else {
            // toggle
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
                stopCountDown();
            } else {
                mWakeLock.acquire();
                mDuration = 0;
                startCountDown(DURATIONS[mDuration]);
            }
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        refresh();
    }

    private void startCountDown(int duration) {
        stopCountDown();
        mSecondsRemaining = duration;
        if (duration == -1) {
            // infinity timing, no need to start timer
            return;
        }

        mCountdownTimer = new CountDownTimer(duration * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mSecondsRemaining = (int) (millisUntilFinished / 1000);
                refresh();
            }

            @Override
            public void onFinish() {
                if (mWakeLock.isHeld())
                    mWakeLock.release();
                refresh();
            }

        }.start();
    }

    private void stopCountDown() {
        if (mCountdownTimer != null) {
            mCountdownTimer.cancel();
            mCountdownTimer = null;
        }
    }

    private String formatValueWithRemainingTime() {
        if (mSecondsRemaining == -1) {
            return "\u221E"; // infinity
        }
        return String.format("%02d:%02d",
                        mSecondsRemaining / 60 % 60, mSecondsRemaining % 60);
    }

    private void refresh() {
        if (mWakeLock.isHeld()) {
            getQsTile().setLabel(formatValueWithRemainingTime());
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_caffeine_on));
            getQsTile().setState(Tile.STATE_ACTIVE);
        } else {
            getQsTile().setLabel(getString(R.string.caffeine_label));
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_caffeine_off));
            getQsTile().setState(Tile.STATE_INACTIVE);
        }
        getQsTile().updateTile();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                // disable caffeine if user force off (power button)
                stopCountDown();
                if (mWakeLock.isHeld())
                    mWakeLock.release();
                refresh();
            }
        }
    };
}
