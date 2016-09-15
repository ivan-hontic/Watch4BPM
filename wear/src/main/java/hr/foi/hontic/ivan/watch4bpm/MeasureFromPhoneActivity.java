package hr.foi.hontic.ivan.watch4bpm;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import hr.foi.hontic.ivan.watch4bpm.Services.NotifyPhoneEndOfMeasuringService;
import hr.foi.hontic.ivan.watch4bpm.Services.SendDataToPhone;

public class MeasureFromPhoneActivity extends Activity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {

    public static final String TAG = "MeasureFromPhoneActvty";
    private static final String MEASURING_DATA_FROM_WATCH = "/receive_measurements";
    private static final String END_OF_PHONE_ACTIVITY = "/end_of_phone_measure_activity";
    private static final String END_OF_WEAR_ACTIVITY = "/end_of_wear_measure_activity";

    boolean record = true;


    GoogleApiClient mGoogleApiClient;

    Sensor mHeartRateSensor;
    SensorManager mSensorManager;
    TextView mTextViewHeart;
    TextView mTextStopped;

    SQLiteDatabase mydatabase;

    int heartRate = 0;
    long time = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_from_phone);

        //this is stopping device from dimming or closing the app(because of saving energy)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mTextViewHeart = (TextView) findViewById(R.id.txtView);
        mTextStopped = (TextView) findViewById(R.id.txtViewStop);


        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);


        initGoogleApiClient();


    }

    @Override
    protected void onResume() {
        super.onResume();
        //Register the listener
        if (mSensorManager != null) {
            mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //Unregister the listener

        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);

    }

    private void initGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnected( Bundle bundle ) {
        Log.d("Hoc", "Add listener mapiClient");
        //sendMessage("/tak", "");

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    private void sendMessage(final String path, final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), path, text.getBytes()).await();
                }
            }
        }).start();
    }


    @Override
    protected void onDestroy() {
        Intent msgIntent = new Intent(this, NotifyPhoneEndOfMeasuringService.class);
        startService(msgIntent);

        super.onDestroy();

        //Unregister the listener
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
        if (heartRate != 0) {
            mydatabase = openOrCreateDatabase("dbBPM", MODE_PRIVATE, null);

            mydatabase.execSQL("CREATE TABLE IF NOT EXISTS hrTable(Heartrate INTEGER, Time INTEGER);");
            ContentValues contentValues = new ContentValues();
            contentValues.put("Heartrate", heartRate);
            contentValues.put("Time", time);

            mydatabase.insert("hrTable", null, contentValues);
            mydatabase.close();
        }

        mGoogleApiClient.disconnect();

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {

            if (record) {
                //using the value when phone activity was abandoned
                heartRate = (int) event.values[0];
            }

            time = System.currentTimeMillis();

            mTextViewHeart.setText("" + (int) event.values[0]);

            sendMessage(MEASURING_DATA_FROM_WATCH, "" + (int) event.values[0]);


        } else
            Log.d(TAG, "Unknown sensor type");
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (messageEvent.getPath().equalsIgnoreCase(END_OF_PHONE_ACTIVITY)) {
                    record = false;
                    mTextStopped.setText(R.string.stoppedMeasurement);

                }
            }
        });
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged - accuracy: " + accuracy);
    }
}
