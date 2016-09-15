package hr.foi.hontic.ivan.watch4bpm.Services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Ivan on 11.9.2016..
 */
public class SendDataToPhone extends IntentService implements GoogleApiClient.ConnectionCallbacks{

    private static final String SEND_DATA_TO_PHONE = "/sending_data_to_phone";
    GoogleApiClient mGoogleApiClient;

    SQLiteDatabase mydatabase;



    public SendDataToPhone() {
        super("S");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

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
        //sendMessage(SEND_DATA_TO_PHONE, dataMessage);





        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API)
                .build();

        mGoogleApiClient.connect();
        //sendMessage(END_OF_WEAR_ACTIVITY, "N");
        final String text= dataMessage;

        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mGoogleApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), SEND_DATA_TO_PHONE, text.getBytes() ).await();
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
