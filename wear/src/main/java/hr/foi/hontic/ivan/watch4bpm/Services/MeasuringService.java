package hr.foi.hontic.ivan.watch4bpm.Services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Ivan on 11.9.2016..
 */
public class MeasuringService extends IntentService implements SensorEventListener {


    Sensor mHeartRateSensor;
    SensorManager mSensorManager;

    SQLiteDatabase mydatabase;


    public MeasuringService() {
        super("SMTH");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("MeasuringService", "onHandleIntent");
        //mSensorManager.unregisterListener(this);

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            Long time = System.currentTimeMillis();

            mydatabase = openOrCreateDatabase("dbBPM", MODE_PRIVATE, null);


            Log.d("MeasuringService", "onSensorChanged");
            ContentValues contentValues = new ContentValues();
            contentValues.put("Heartrate", (int) event.values[0]);
            contentValues.put("Time", time);

            mydatabase.insert("hrTable", null, contentValues);
            mydatabase.close();
            mSensorManager.unregisterListener(this);



        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
