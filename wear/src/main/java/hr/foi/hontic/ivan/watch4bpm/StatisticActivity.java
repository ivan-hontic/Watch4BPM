package hr.foi.hontic.ivan.watch4bpm;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatisticActivity extends Activity implements GoogleApiClient.ConnectionCallbacks{

    SQLiteDatabase mydatabase;

    TextView txtHr1;
    TextView txtDate1;
    TextView txtHr2;
    TextView txtDate2;
    TextView txtHr3;
    TextView txtHr4;
    TextView txtDate4;

    Node mNode; // the connected device to send the message to
    GoogleApiClient mGoogleApiClient;


    private static final String START_STATISTIC_ACTIVITY = "/start_statistic_activity";
    private static final String SEND_DATA_TO_PHONE = "/sending_data_to_phone";
    public static String SERVICE_CALLED_WEAR = "WearListClicked";
    String TAG = "StatisticActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        //this is stopping device from dimming or closing the app(because of saving energy)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        txtHr1 = (TextView) findViewById(R.id.txtMax2);
        txtDate1 = (TextView) findViewById(R.id.txtMax3);

        txtHr2 = (TextView) findViewById(R.id.txtMin2);
        txtDate2 = (TextView) findViewById(R.id.txtMin3);

        txtHr3 = (TextView) findViewById(R.id.txtAvg2);

        txtHr4 = (TextView) findViewById(R.id.txtLast2);
        txtDate4 = (TextView) findViewById(R.id.txtLast3);

        fillTheData();
        initGoogleApiClient();
    }

    public void fillTheData(){
        mydatabase = openOrCreateDatabase("dbBPM", MODE_PRIVATE, null);
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS hrTable(Heartrate INTEGER, Time INTEGER);");
        Cursor resultSet = mydatabase.rawQuery("Select * from hrTable",null);

        resultSet.moveToFirst();

        int max=0;
        String maxD="";
        int min=500;
        String minD="";
        int avg=0;

        int lastHR=0;
        String lastD="";

        int numberOfAll = 0;
        int sumOfResults = 0;

        //Log.d("Main", "prazno = " + prazno);
        while(resultSet.isAfterLast() == false){
            numberOfAll++;

            int currHR= Integer.parseInt(resultSet.getString(0));
            sumOfResults+=currHR;
            String time = resultSet.getString(1);

            resultSet.moveToNext();


            Date date = new Date(Long.valueOf(time).longValue());
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String dateFormatted = formatter.format(date);

            if(resultSet.isAfterLast() == true){
                lastHR=currHR;
                lastD=dateFormatted;
            }

            if(currHR>max){
                max= currHR;
                maxD = dateFormatted;
            }
            if(currHR<min){
                min =currHR;
                minD = dateFormatted;
            }
            avg=sumOfResults/numberOfAll;


        }
        mydatabase.close();
        if(max==0){
            txtHr1.setText("--");
        } else {
            txtHr1.setText(""+max);
        }
        txtDate1.setText(maxD);

        if(min==500){
            txtHr2.setText("--");
        } else {
            txtHr2.setText(""+min);
        }
        txtDate2.setText(minD);

        if(avg==0){
            txtHr3.setText("--");
        } else {
            txtHr3.setText(""+avg);
        }


        if(lastHR==0){
            txtHr4.setText("--");
        } else {
            txtHr4.setText("" + lastHR);
        }
        txtDate4.setText(lastD);


    }

    public void openStatsOnPhone(View v){

        mydatabase = openOrCreateDatabase("dbBPM", MODE_PRIVATE, null);
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS hrTable(Heartrate INTEGER, Time INTEGER);");
        Cursor resultSet = mydatabase.rawQuery("Select * from hrTable",null);

        resultSet.moveToFirst();


        String dataMessage="";

        while(resultSet.isAfterLast() == false){
            dataMessage+=resultSet.getString(0)+","+resultSet.getString(1);
            resultSet.moveToNext();
            if(resultSet.isAfterLast() == false) {
                dataMessage+=" ";
            }
        }
        mydatabase.close();
        sendMessage(SEND_DATA_TO_PHONE, dataMessage);
        sendMessage(START_STATISTIC_ACTIVITY, "N");
    }

    private void initGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API)
                .build();

        mGoogleApiClient.connect();
    }

    public void deleteRecordedData(View v){
        mydatabase = openOrCreateDatabase("dbBPM", MODE_PRIVATE, null);
        mydatabase.execSQL("DROP TABLE hrTable;");
        mydatabase.close();
        fillTheData();

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("StatsActivity", "onConnected");



    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void sendMessage( final String path, final String text ) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mGoogleApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), path, text.getBytes() ).await();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }
}
