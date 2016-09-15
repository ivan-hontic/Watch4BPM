package hr.foi.hontic.ivan.watch4bpm.Services;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import hr.foi.hontic.ivan.watch4bpm.MainActivity;
import hr.foi.hontic.ivan.watch4bpm.MeasureFromPhoneActivity;

/**
 * Created by Ivan on 10.9.2016..
 */
public class MessageListenerService extends WearableListenerService{

    private static final String START_ACTIVITY = "/start_activity";
    private static final String START_MEASURE_FROM_PHONE = "/start_measure_from_phone";
    private static final String START_WEAR_APP = "/start_wear_app";
    private static final String ASKING_FOR_DATA_TO_PHONE = "/asking_for_sending_data_to_phone";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if( messageEvent.getPath().equalsIgnoreCase( START_ACTIVITY ) ) {
            Log.d("MessageListenerService", "Preslo u start:");
            Intent intent = new Intent( this, MainActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity( intent );
        } if( messageEvent.getPath().equalsIgnoreCase( START_MEASURE_FROM_PHONE ) ) {
            Log.d("MessageListenerService", "Starting Measure now from phone:");
            Intent intent = new Intent( this, MeasureFromPhoneActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity( intent );
        } if( messageEvent.getPath().equalsIgnoreCase( START_WEAR_APP ) ) {
            Log.d("MessageListenerService", "Starting wear app");
            Intent intent = new Intent( this, MainActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity( intent );
        } if( messageEvent.getPath().equalsIgnoreCase( ASKING_FOR_DATA_TO_PHONE ) ) {
            Log.d("MessageListenerService", "Asking for new data");

            Intent intent = new Intent( this, SendDataToPhone.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            startService(intent);

        }else {

            byte[] bytes = messageEvent.getData();
            String str = new String(bytes);
            Log.d("Hoc", "Poruka:" + str);
            super.onMessageReceived(messageEvent);
        }
    }
}
