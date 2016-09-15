package hr.foi.hontic.ivan.watch4bpm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    GoogleApiClient mApiClient;

    private static final String START_ACTIVITY = "/start_activity";
    private static final String START_WEAR_APP = "/start_wear_app";
    private static final String WEAR_MESSAGE_PATH = "/nezz kaj tu ide";
    private static final String START_MEASURE_FROM_PHONE = "/start_measure_from_phone";








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        initGoogleApiClient();








    }

    public void startMeasureNow(View v){
        sendMessage(START_MEASURE_FROM_PHONE, "N");
        Intent intent = new Intent( this, MeasureNowActivity.class );
        startActivity(intent);
    }

    public void startStatisticActivity(View v){
        Intent intent = new Intent( this, StatisticActivity.class );
        startActivity(intent);
    }

    public void openWearApp(View v){
        sendMessage(START_WEAR_APP, "");
    }

    public void openSettings(View v){
        Intent intent = new Intent( this, SettingsActivity.class );
        startActivity(intent);
    }

    public void exitApp(View v){
        System.exit(0);
    }


    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .build();

        mApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        sendMessage(START_ACTIVITY, "");
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
