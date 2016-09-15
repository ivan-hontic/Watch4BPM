package hr.foi.hontic.ivan.watch4bpm.Services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Ivan on 12.9.2016..
 */
public class NotifyPhoneEndOfMeasuringService extends IntentService implements GoogleApiClient.ConnectionCallbacks {

    private static final String END_OF_WEAR_ACTIVITY = "/end_of_wear_measure_activity";
    GoogleApiClient mGoogleApiClient;



    public NotifyPhoneEndOfMeasuringService() {
        super("S");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d("Notify", "onHandle");
        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API)
                .build();

        mGoogleApiClient.connect();
        //sendMessage(END_OF_WEAR_ACTIVITY, "N");
        final String text= " ";

        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mGoogleApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), END_OF_WEAR_ACTIVITY, text.getBytes() ).await();
                }
            }
        }).start();
        //mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }



}
