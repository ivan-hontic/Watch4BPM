package hr.foi.hontic.ivan.watch4bpm;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatisticActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks{

    SQLiteDatabase mydatabase;

    TextView txtHr1;
    TextView txtDate1;
    TextView txtHr2;
    TextView txtDate2;
    TextView txtHr3;
    TextView txtHr4;
    TextView txtDate4;

    private static final String ASKING_FOR_DATA_TO_PHONE = "/asking_for_sending_data_to_phone";

    GoogleApiClient mApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

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

        int numberOfAll = 0;
        int sumOfResults = 0;

        int lastHR=0;
        String lastD="";

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

    public void fetchNewData(View v){
        sendMessage(ASKING_FOR_DATA_TO_PHONE, "");

        //Intent refresh = new Intent(this, StatisticActivity.class);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                fillTheData();

            }
        }, 850);

    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .build();

        mApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        //sendMessage(START_ACTIVITY, "");
    }

    @Override
    public void onConnectionSuspended(int i) {

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
    }

}
