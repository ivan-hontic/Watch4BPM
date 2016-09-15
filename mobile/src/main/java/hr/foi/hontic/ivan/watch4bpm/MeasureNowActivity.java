package hr.foi.hontic.ivan.watch4bpm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import hr.foi.hontic.ivan.watch4bpm.Services.NotifyWatchEndOfMeasuringService;

public class MeasureNowActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener{

    TextView txtHR;
    TextView txtStop;
    GoogleApiClient mApiClient;
    private static final String MEASURING_DATA_FROM_WATCH = "/receive_measurements";
    private static final String END_OF_PHONE_ACTIVITY = "/end_of_phone_measure_activity";
    private static final String END_OF_WEAR_ACTIVITY = "/end_of_wear_measure_activity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_now);


        txtHR = (TextView) findViewById(R.id.txtView);
        txtStop = (TextView) findViewById(R.id.txtViewStop);

        initGoogleApiClient();
    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .build();

        mApiClient.connect();
        Wearable.MessageApi.addListener(mApiClient, this);
    }


    @Override
    public void onConnected( Bundle bundle ) {
        Log.d("Hoc", "Add listener mapiClient");
        Wearable.MessageApi.addListener(mApiClient, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Hoc", "Main - connSuspended:");
    }


    @Override
    public void onMessageReceived( final MessageEvent messageEvent ) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (messageEvent.getPath().equalsIgnoreCase(MEASURING_DATA_FROM_WATCH)) {

                    byte[] bytes = messageEvent.getData();
                    String str = new String(bytes);
                    txtHR.setText(str);

                } else if (messageEvent.getPath().equalsIgnoreCase(END_OF_WEAR_ACTIVITY)) {
                    txtStop.setText(R.string.stoppedMeasurement);

                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        Intent msgIntent = new Intent(this, NotifyWatchEndOfMeasuringService.class);
        startService(msgIntent);
        super.onDestroy();

        mApiClient.disconnect();

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

}
