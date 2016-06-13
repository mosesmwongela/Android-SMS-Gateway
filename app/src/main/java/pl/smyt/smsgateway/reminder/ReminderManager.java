package pl.smyt.smsgateway.reminder;

/**
 * Created by Pineapple on 3/26/2015.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.util.Calendar;

import pl.smyt.smsgateway.utility.DatabaseHelper;

public class ReminderManager {

    private static final String TAG = "ReminderManager";

    private Context mContext;
    private AlarmManager mAlarmManager;

    private DatabaseHelper db;
    Cursor cursor;

    PendingIntent pi;

    public ReminderManager(Context context) {
        mContext = context;
        mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        db = new DatabaseHelper(mContext);

    }

    public void setReminder(Long taskId, Calendar when) {

        Intent i = new Intent(mContext, OnAlarmReceiver.class);

        //Unique id for each class - unique pending intent
        final int _id = taskId.intValue();

        i.putExtra("mRowId", (long)taskId);

        pi = PendingIntent.getBroadcast(mContext, _id, i, PendingIntent.FLAG_UPDATE_CURRENT);

        //get seonds to poll from databse
        long time = 10000;
        try {
            String time_interval = db.getTimeToPoll();
            time = Long.parseLong(time_interval);
        }catch(Exception e){
            e.printStackTrace();
        }

        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), time, pi);

        Log.d(TAG, "Set reminder to go off every "+ time + "ms");
    }

    public void CancelReminder(String taskId) {

        Intent i = new Intent(mContext, OnAlarmReceiver.class);

        //Unique id for each class - unique pending intent
        final int _id = Integer.parseInt(taskId);

        pi = PendingIntent.getBroadcast(mContext, _id, i, PendingIntent.FLAG_UPDATE_CURRENT);

        long oneWeek= 604800000;

        mAlarmManager.cancel(pi);
        Log.d(TAG, "Reminder cancelled");
    }
}
