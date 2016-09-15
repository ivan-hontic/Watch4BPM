package hr.foi.hontic.ivan.watch4bpm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import hr.foi.hontic.ivan.watch4bpm.Services.MeasuringService;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks {

    private TextView mTextView;
    GoogleApiClient mApiClient;

    private static final String OPEN_PHONE_APP = "/open_phone_app";


    Button btnService;

    SQLiteDatabase mydatabase;

    int serviceMinutes=0;
    int serviceState=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //this is stopping device from dimming or closing the app(because of saving energy)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnService = (Button) findViewById(R.id.bpmServiceButton);




        Log.d("Main", "onCreate");
        initGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Main", "onStart");
        setTextToServiceBPMButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Main", "onResume");
        setTextToServiceBPMButton();
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d("Main", "onStop");
    }
    @Override
    protected void onPause() {
        super.onPause();

        Log.d("Main", "onPause");
    }





    //opens MeasureNowActivity
    public void openMeasureNow(View v){
        Intent aboutIntent = new Intent(this, MeasureNowActivity.class);
        startActivity(aboutIntent);
    }

    //opens StatisticActivity
    public void openStatistics(View v){
        Intent aboutIntent = new Intent(this, StatisticActivity.class);
        startActivity(aboutIntent);
    }

    //opens SettingsActivity
    public void openSettingsActivity(View v){
        Intent aboutIntent = new Intent(this, SettingsActivity.class);
        startActivity(aboutIntent);
    }

    //opens AboutActivity
    public void openAboutActivity(View v){
        Intent aboutIntent = new Intent(this, AboutActivity.class);
        startActivity(aboutIntent);
    }


    //sets text on service button
    public void setTextToServiceBPMButton() {
        mydatabase = openOrCreateDatabase("dbBPM", MODE_PRIVATE, null);
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS servicesetTable(Minutes INTEGER, State INTEGER);");
        Cursor resultSet = mydatabase.rawQuery("Select * from servicesetTable",null);

        resultSet.moveToFirst();
        boolean prazno = true;
        Log.d("Main", "prazno = "+prazno);
        while(resultSet.isAfterLast() == false){
            prazno = false;
            Log.d("Main", "prazno2 = "+prazno);


            serviceMinutes = Integer.parseInt(resultSet.getString(0));
            serviceState=Integer.parseInt(resultSet.getString(1));

            resultSet.moveToNext();

        }
        if (prazno){
            setSettingsToDB(5, 0);//5 mins , not active
        }
        if(serviceState==0) {
            btnService.setText(R.string.measurementServiceButtonStart);
        } else {
            btnService.setText(R.string.measurementServiceButtonStop);
        }
        resultSet.close();
        mydatabase.close();
    }

    public void setBPMServiceState(View v){

        if (serviceState==0){
            //start service
            setSettingsToDB(serviceMinutes, 1);
            btnService.setText(R.string.measurementServiceButtonStop);
            serviceState=1;
            registerMeasuringServiceAlarm(this, serviceMinutes);
            Toast.makeText(MainActivity.this, R.string.toastMeasuringServiceStart, Toast.LENGTH_SHORT).show();
        } else {
            //stop service
            setSettingsToDB(serviceMinutes, 0);
            btnService.setText(R.string.measurementServiceButtonStart);
            serviceState=0;
            cancelMeasuringServiceAlarmIfExists(this);
            Toast.makeText(MainActivity.this, R.string.toastMeasuringServiceStop, Toast.LENGTH_SHORT).show();
        }

    }
    public void setSettingsToDB(int mins, int state){
        mydatabase = openOrCreateDatabase("dbBPM", MODE_PRIVATE, null);
        mydatabase.execSQL("DROP TABLE servicesetTable;");

        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS servicesetTable(Minutes INTEGER, State INTEGER);");



        ContentValues contentValues = new ContentValues();
        contentValues.put("Minutes", mins);
        contentValues.put("State", state);

        mydatabase.insert("servicesetTable", null, contentValues);
        mydatabase.close();
    }

    public void openPhoneApp(View v){
        sendMessage(OPEN_PHONE_APP, "N");
    }


    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .build();

        mApiClient.connect();
    }


    @Override
    public void onConnected( Bundle bundle ) {
        Log.d("Hoc", "Add listener mapiClient");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Hoc", "Main - connSuspended:");
    }

    private void sendMessage( final String path, final String text ) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes() ).await();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();

        Log.d("Main", "onDestroy");
    }

    public static void registerMeasuringServiceAlarm(Context context, int mins) {
        Intent i = new Intent(context, MeasuringService.class);

        PendingIntent sender = PendingIntent.getService(context, 0, i, 0);

        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 6 * 1000;//start 6 seconds after first register.

        // Schedule the alarm!
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, mins*60000, sender);//10min interval

    }

    public void cancelMeasuringServiceAlarmIfExists(Context mContext){
        try{
            Intent i = new Intent(mContext, MeasuringService.class);
            PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, i, 0);
            AlarmManager am=(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
            am.cancel(pendingIntent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
