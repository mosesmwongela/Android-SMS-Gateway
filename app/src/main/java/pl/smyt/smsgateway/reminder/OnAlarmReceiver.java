package pl.smyt.smsgateway.reminder;

/**
 * Created by Pineapple on 3/26/2015.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "OnAlarmReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {

        long rowid = intent.getExtras().getLong("mRowId");

        WakeReminderIntentService.acquireStaticLock(context);

        Intent i = new Intent(context, ReminderService.class);
        i.putExtra("mRowId", rowid);
        context.startService(i);

    }
}