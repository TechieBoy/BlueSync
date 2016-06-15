package nas.tek.bluesync;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class NotifyService extends IntentService{

    private static final String TAG = "NotifyService";
    private Realm realm;
    private static final int NOTIFICATION_ID = 1;
    private static final String mNotificationTitle = "You forgot some items";
    private String mNotificationBody;
    private NotificationManager mNotificationManager;

    public NotifyService(){
        super("SchedulingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ArrayList<String> unCheckedItems = new ArrayList<>();
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getApplicationContext()).build();
        realm = Realm.getInstance(realmConfig);
        RealmResults<ListItem> results = realm.where(ListItem.class)
                .equalTo("mPresent",false)
                .findAll();
        Log.i(TAG,"results size :"+ results.size());
        if(results.size()!=0){
            for(ListItem li: results){
                Log.i(TAG,li.getTitle());
                unCheckedItems.add(li.getTitle());
            }
        }

        String temp = "";
        for(String title : unCheckedItems){
           temp = temp + title + ", ";
        }
        temp = temp.substring(0,temp.length()-2);
        temp = temp+ " ";

        mNotificationBody = temp + "are unchecked";
        sendNotification(mNotificationBody);
        AlarmReceiver.completeWakefulIntent(intent);
        realm.close();
    }

    public void sendNotification(String body){
        mNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this,1,new Intent(this,MainActivity.class),0);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_img)
                .setContentTitle(mNotificationTitle)
                        .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setContentText(body);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID,mBuilder.build());


    }
}
