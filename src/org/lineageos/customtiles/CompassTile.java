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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class CompassTile extends TileService implements SensorEventListener {
    private final static float ALPHA = 0.97f;

    private boolean mActive = false;

    private SensorManager mSensorManager;
    private Sensor mAccelerationSensor;
    private Sensor mGeomagneticFieldSensor;

    private float mDegrees = 0f;
    private float mAngle = 0;

    private float[] mAcceleration;
    private float[] mGeomagnetic;

    private Bitmap mImage;

    @Override
    public void onStartListening() {
        super.onStartListening();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGeomagneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        refresh();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();

        setListeningSensors(false);
        mImage = null;
    }

    @Override
    public void onClick() {
        super.onClick();
        mActive = !mActive;
        setListeningSensors(mActive);
        refresh();
    }

    private Bitmap getBitmap(Drawable drawable) {
        Bitmap bm = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bm;
    }

    private void setListeningSensors(boolean listening) {
        if (listening) {
            mSensorManager.registerListener(
                    this, mAccelerationSensor, SensorManager.SENSOR_DELAY_GAME);
            mSensorManager.registerListener(
                    this, mGeomagneticFieldSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            mSensorManager.unregisterListener(this);
        }
    }

    private void refresh() {
        if (mActive) {
            getQsTile().setState(Tile.STATE_ACTIVE);
            if (mDegrees != 0) {
                float target = 360 - mDegrees;
                float relative = target - mAngle;
                if (relative > 180) {
                    relative -= 360;
                }
                mAngle = mAngle + relative / 2;
                setRotation(mAngle + relative / 2);

                getQsTile().setIcon(Icon.createWithBitmap(mImage));
                getQsTile().setLabel(formatValueWithCardinalDirection(mDegrees));
            } else {
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_compass_on));
                getQsTile().setLabel(getString(R.string.quick_settings_compass_init));
                setRotation(0);
            }
        } else {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_compass_off));
            getQsTile().setLabel(getString(R.string.compass_label));
            getQsTile().setState(Tile.STATE_INACTIVE);
        }
        getQsTile().updateTile();
    }

    private void setRotation(float degree) {
        Bitmap image = getBitmap(getResources().getDrawable(R.drawable.ic_compass_on));
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        int width = image.getWidth();
        int height = image.getHeight();
        mImage = Bitmap.createBitmap(image, 0, 0, width, height, matrix, true);
    }

    private String formatValueWithCardinalDirection(float degree) {
        int cardinalDirectionIndex = (int) (Math.floor(((degree - 22.5) % 360) / 45) + 1) % 8;
        String[] cardinalDirections = getResources().getStringArray(
                R.array.cardinal_directions);

        return getString(R.string.quick_settings_compass_value, degree,
                cardinalDirections[cardinalDirectionIndex]);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (mAcceleration == null) {
                mAcceleration = event.values.clone();
            }

            values = mAcceleration;
        } else {
            // Magnetic field sensor
            if (mGeomagnetic == null) {
                mGeomagnetic = event.values.clone();
            }

            values = mGeomagnetic;
        }

        for (int i = 0; i < 3; i++) {
            values[i] = ALPHA * values[i] + (1 - ALPHA) * event.values[i];
        }

        if (!mActive || mAcceleration == null || mGeomagnetic == null) {
            // Nothing to do at this moment
            return;
        }

        float R[] = new float[9];
        float I[] = new float[9];
        if (!SensorManager.getRotationMatrix(R, I, mAcceleration, mGeomagnetic)) {
            // Rotation matrix couldn't be calculated
            return;
        }

        // Get the current orientation
        float[] orientation = new float[3];
        SensorManager.getOrientation(R, orientation);

        // Convert azimuth to degrees
        Float newDegree = (float) Math.toDegrees(orientation[0]);
        newDegree = (newDegree + 360) % 360;

        mDegrees = newDegree;
        refresh();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
