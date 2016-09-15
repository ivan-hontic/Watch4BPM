package hr.foi.hontic.ivan.watch4bpm;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;



public class MeasureNowActivity extends Activity implements SensorEventListener {

    public static final String TAG = "MeasureNowActivity";

    Sensor mHeartRateSensor;
    SensorManager mSensorManager;
    TextView mTextViewHeart;

    SQLiteDatabase mydatabase;

    int heartRate = 0;
    long time=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_now);

        //this is stopping device from dimming or closing the app(because of saving energy)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mTextViewHeart = (TextView) findViewById(R.id.txtView);



        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        //mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);


    }

    @Override
    protected void onResume() {
        super.onResume();
        //Register the listener
        if (mSensorManager != null) {
            mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Unregister the listener
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Unregister the listener
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
        if (heartRate!=0){
            mydatabase = openOrCreateDatabase("dbBPM", MODE_PRIVATE, null);

            //mydatabase.execSQL("DROP TABLE hrTable;");

            mydatabase.execSQL("CREATE TABLE IF NOT EXISTS hrTable(Heartrate INTEGER, Time INTEGER);");
            ContentValues contentValues = new ContentValues();
            contentValues.put("Heartrate", heartRate);
            contentValues.put("Time", time);

            mydatabase.insert("hrTable", null, contentValues);
            mydatabase.close();
        }

        Log.d(TAG, "onDestroy");
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {

            heartRate=(int) event.values[0];
            time = System.currentTimeMillis();
            String msg = "HR: " + (int) event.values[0];
            //msg=timesMeasured+" "+(int) event.values[0];
            mTextViewHeart.setText("" + (int) event.values[0]);
            Log.d(TAG, msg);



        } else
            Log.d(TAG, "Unknown sensor type");
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged - accuracy: " + accuracy);
    }
}
