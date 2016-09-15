package hr.foi.hontic.ivan.watch4bpm.Services;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import hr.foi.hontic.ivan.watch4bpm.MainActivity;
import hr.foi.hontic.ivan.watch4bpm.StatisticActivity;

/**
 * Created by Ivan on 12.9.2016..
 */
public class WearMessageListenerService extends WearableListenerService {
    private static final String START_ACTIVITY = "/start_activity";
    private static final String START_STATISTIC_ACTIVITY = "/start_statistic_activity";
    private static final String SEND_DATA_TO_PHONE = "/sending_data_to_phone";
    private static final String OPEN_PHONE_APP = "/open_phone_app";

    SQLiteDatabase mydatabase;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if( messageEvent.getPath().equalsIgnoreCase( START_STATISTIC_ACTIVITY ) ) {
            Log.d("MessageListener", "OpenStatisticActivity");
            Intent intent = new Intent( this, StatisticActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity( intent );
        } if( messageEvent.getPath().equalsIgnoreCase( SEND_DATA_TO_PHONE ) ) {
            Log.d("MessageListener", "SaveDataToDB");
            byte[] bytes = messageEvent.getData();
            String dataMessage = new String(bytes);
            mydatabase = openOrCreateDatabase("dbBPM", MODE_PRIVATE, null);
            mydatabase.execSQL("DROP TABLE hrTable;");
            mydatabase.execSQL("CREATE TABLE IF NOT EXISTS hrTable(Heartrate INTEGER, Time INTEGER);");


            String[] separatedRows = dataMessage.split(" ");
            for (int i=0; i<separatedRows.length; i++){
                String[] separatedColumns = separatedRows[i].split(",");

                int currHR= Integer.parseInt(separatedColumns[0]);
                long currTime= Long.valueOf(separatedColumns[1]).longValue();

                ContentValues contentValues = new ContentValues();
                contentValues.put("Heartrate", currHR);
                contentValues.put("Time", currTime);

                mydatabase.insert("hrTable", null, contentValues);


            }
            mydatabase.close();

        } if( messageEvent.getPath().equalsIgnoreCase( OPEN_PHONE_APP ) ) {
            Log.d("MessageListener", "OpenMainActivity");
            Intent intent = new Intent( this, MainActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity( intent );
        } else {

            byte[] bytes = messageEvent.getData();
            String str = new String(bytes);
            Log.d("Hoc", "Poruka:" + str);
            super.onMessageReceived(messageEvent);
        }
    }
}
