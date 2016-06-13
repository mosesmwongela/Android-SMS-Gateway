package pl.smyt.smsgateway.reminder;

/**
 * Created by Pineapple on 3/26/2015.
 */

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.telephony.gsm.SmsManager;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.smyt.smsgateway.utility.DatabaseHelper;
import pl.smyt.smsgateway.utility.JSONParser;

public class ReminderService extends WakeReminderIntentService {

    private String TAG = "ReminderService";

    private JSONParser jsonParser = new JSONParser();

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_ID = "id";
    private static final String TAG_NUMBER = "number";
    private static final String TAG_INFO = "info";

    private String number=null, info=null, message=null, systemName="", currentTime, id;

    private String DeviceModel = "";
    private String DeviceManufacturer = "";
    private String DeviceAndroidVersion = "";
    private String DeviceIMEI = "";

    private DatabaseHelper db;
    Cursor cursor = null;
    String outBoxUrl = null;
    String serverStatus = null;

    public ReminderService() {
        super("ReminderService");
    }

    @Override
    void doReminderWork(Intent intent) {

            getPhoneDetails();

            db = new DatabaseHelper(getBaseContext());
            cursor = db.getAllSystems();
            cursor.moveToFirst();

            if (isConnected()) {
                if(cursor.isFirst()) {
                    new Poll().execute();
                }else {
                    Log.e(TAG, "No system configured");
                }
            }else {
                db.insertLog(systemName, "No internet connection", "1", getTime());
                Log.e(TAG, "No internet connection");
            }
    }

    private boolean isConnected(){
        boolean isConnected=false;
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni!=null){
            isConnected=true;
        }else{
            isConnected=false;
        }
        return isConnected;
    }

    public String getTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    public String getDateTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyy HH:mm:ss");
        return sdf.format(new Date());
    }

    public void getPhoneDetails(){
        DeviceModel = Build.MODEL;
        DeviceManufacturer = Build.MANUFACTURER;
        DeviceAndroidVersion = Build.VERSION.RELEASE;

        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        DeviceIMEI = mngr.getDeviceId();
    }

    class Poll extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            int success = 9;
                systemName = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(1)));
                outBoxUrl = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(2)));
                serverStatus = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(3)));

                if(serverStatus.equalsIgnoreCase("Active")) {
                    try {
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("DeviceModel", DeviceModel));
                        params.add(new BasicNameValuePair("DeviceManufacturer", DeviceManufacturer));
                        params.add(new BasicNameValuePair("DeviceAndroidVersion", DeviceAndroidVersion));
                        params.add(new BasicNameValuePair("DeviceIMEI", DeviceIMEI));
                        params.add(new BasicNameValuePair("DeviceLastSeen", getDateTime()));

                        JSONObject json = jsonParser.makeHttpRequest(outBoxUrl, "POST", params);

                        Log.e(TAG + " " + systemName, outBoxUrl);
                        Log.e(TAG + " " + systemName, json.toString());

                            success = json.getInt(TAG_SUCCESS);
                            message = json.getString(TAG_MESSAGE);

                            if (success == 1) {
                                JSONArray messages = json.getJSONArray("messages");
                                for(int i=0; i<messages.length(); i++){
                                    db.insertLog(systemName, message, "0", getTime());

                                    JSONObject msg = messages.getJSONObject(i);

                                    id = msg.getString("message_id");
                                    info = msg.getString("message");
                                    number = msg.getString("phone_number");
                                    sendSMS(systemName, id, number, info);
                                }
                                return message;
                            } else {
                                db.insertLog(systemName, message, "1", getTime());
                                return message;
                            }

                    } catch (Exception e) {
                        e.printStackTrace();
                        db.insertLog(systemName, "Server response is unreadable", "1", getTime());
                        message = "Server response is unreadable.";
                        return message;
                    }
                }
            return null;
        }

        protected void onPostExecute(String file_url) {
            if (file_url != null){
               // Toast.makeText(ReminderService.this, file_url, Toast.LENGTH_LONG).show();
            }
            if(!cursor.isLast()){
                cursor.moveToNext();
                new Poll().execute();
            }
        }
    }

    //---sends an SMS message and listen for delivery message---
    private void sendSMS(final String systemName, final String msg_id, String phoneNumber, String message)
    {

        db.insertLog(systemName, "Sending SMS #" +msg_id+" to "+phoneNumber, "0", getTime());
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                       // Toast.makeText(getBaseContext(), "SMS sent",Toast.LENGTH_SHORT).show();
                        db.insertLog(systemName, "SMS #" +msg_id+" sent to "+number, "0", getTime());
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                       // Toast.makeText(getBaseContext(), "Generic failure",Toast.LENGTH_SHORT).show();
                        db.insertLog(systemName, "SMS #" +msg_id+" not sent to: "+number+": Generic failure", "1", getTime());
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        //Toast.makeText(getBaseContext(), "No service",Toast.LENGTH_SHORT).show();
                        db.insertLog(systemName, "SMS #" +msg_id+" not sent to: "+number+": No service", "1", getTime());
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        //Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                        db.insertLog(systemName, "SMS #" +msg_id+" not sent to: "+number+": Null PDU", "1", getTime());
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        //Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                        db.insertLog(systemName, "SMS #" +msg_id+" not sent to: "+number+": Radio off", "1", getTime());
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        db.insertLog(systemName, "SMS #" +msg_id+" delivered to: "+number, "0", getTime());
                        break;
                    case Activity.RESULT_CANCELED:
                        db.insertLog(systemName, "SMS #" +msg_id+" not delivered to: " + number, "1", getTime());
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));


        SmsManager sm = SmsManager.getDefault();
        ArrayList<String> parts =sm.divideMessage(message);
        int numParts = parts.size();

        ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();

        for (int i = 0; i < numParts; i++) {
            sentIntents.add(sentPI);
            deliveryIntents.add(deliveredPI);
        }
        sm.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, deliveryIntents);

        Log.e(TAG, "Number of parts: "+numParts);
    }
}
