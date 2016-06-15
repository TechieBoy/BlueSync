package nas.tek.bluesync;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.Calendar;

public class AlarmReceiver extends WakefulBroadcastReceiver{
    private final String TAG = "AlarmReceiver";

    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context,NotifyService.class);
        startWakefulService(context,service);

    }

    public void setAlarm(Context context){
        mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context,AlarmReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(context,0,intent,0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,6);
        calendar.set(Calendar.MINUTE,45);

        Log.i(TAG,"setAlarm called");
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY,mPendingIntent);

    }

    public void cancelAlarm(Context context){
        if(mAlarmManager!=null){
            Log.i(TAG,"Alarm stopped");
            mAlarmManager.cancel(mPendingIntent);
        }
    }
}
