package pl.smyt.smsgateway.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import pl.smyt.smsgateway.R;
import pl.smyt.smsgateway.adapter.CustomSystemCursorAdapter;
import pl.smyt.smsgateway.reminder.ReminderManager;
import pl.smyt.smsgateway.utility.DatabaseHelper;


public class MainActivity extends ActionBarActivity {

    private static String TAG = "MainActivity";

    private CustomSystemCursorAdapter customSystemAdapter;
    private ListView listView;

    private TextView consoleText = null;

    String[] SystemListContextMenuItems;

    private Calendar mCalendar;

    private DatabaseHelper db;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        consoleText = (TextView) findViewById(R.id.consoleText);
        consoleText.setText("");

        listView = (ListView) findViewById(R.id.list_system);
        customSystemAdapter = new CustomSystemCursorAdapter(MainActivity.this, db.getAllSystems());
        listView.setAdapter(customSystemAdapter);
        registerForContextMenu(listView);
        SystemListContextMenuItems = getResources().getStringArray(R.array.SystemListContextMenu);


        setReminderToPoll(false);

        new TerminalAsync().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_addSystem:
                Intent addSystem = new Intent(MainActivity.this, AddSystemActivity.class);
                addSystem.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                addSystem.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(addSystem);
                finish();
                return true;
            case R.id.action_clear_log:
                db.clearLog();
                new TerminalAsync().execute();
                return true;
            case R.id.action_set_interval:
                try {
                    selectLevelDialog(false);
                }catch(Exception e){
                    selectLevelDialog(true);
                    e.printStackTrace();
                }
                return true;
            case R.id.action_about:
                about();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void about(){
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("About SMYT");
        alert.setMessage(
                "SMYT SMS gateway\n"+
                "Version 1.0\n"+
                        "copyright (c) 2015-2016\n"+
                        "www.smyt.pl\n"+
                        "All rights reserved");
        alert.setPositiveButton("ok",null);
        alert.show();
    }

    class TerminalAsync extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {

            try {
                cursor = db.getAllLogs();

                String log = "";

                if(cursor.moveToFirst()) {

                    String message = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(4)));
                    String flag = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(2)));
                    String time = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(3)));
                    String system = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(1)));
                    log = time + ": " + message;

                    while (cursor.moveToNext()) {
                        // cursor.moveToNext();
                        message = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(4)));
                        flag = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(2)));
                        time = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(3)));
                        system = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(1)));

                        log = time + ": " + message + "\n" + log;
                    }
                }
                return log;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(String file_url) {
            try {
                if (file_url != null) {
                    printTerminal(file_url);
                }
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new TerminalAsync().execute();
                    }
                }, 1000 * 15);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void printTerminal(String log) {
        //String previousString = consoleText.getText().toString();
        //String nextString = log + "\n" + previousString;
        //String nextString =  previousString + "\n" + log ;
        //consoleText.setText(nextString);
        consoleText.setText(log);
    }

    private void setReminderToPoll(boolean reset){
        mCalendar = Calendar.getInstance();
        setDate();
        long taskId= 1234;
        if(reset){
            new ReminderManager(this).CancelReminder(taskId + "");
        }
        new ReminderManager(this).setReminder(taskId, mCalendar);
    }


    private void setDate() {
        String day="Monday";
        if(day.equalsIgnoreCase("Sunday")){
            mCalendar.set(2013, 8, 22);
        }
        if(day.equalsIgnoreCase("Monday")){
            mCalendar.set(2013, 8, 23);
        }
        if(day.equalsIgnoreCase("Tuesday")){
            mCalendar.set(2013, 8, 24);
        }
        if(day.equalsIgnoreCase("Wednesday")){
            mCalendar.set(2013, 8, 25);
        }
        if(day.equalsIgnoreCase("Thursday")){
            mCalendar.set(2013, 8, 26);
        }
        if(day.equalsIgnoreCase("Friday")){
            mCalendar.set(2013, 8, 27);
        }
        if(day.equalsIgnoreCase("Saturday")){
            mCalendar.set(2013, 8, 28);
        }

    }


    private void registerButtonListeners() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("options menu");
        menu.add(menu.NONE, 1, 0, "Edit");
        menu.add(menu.NONE, 2, 1, "Activate/Deactivate");
        menu.add(menu.NONE, 3, 2, "Delete");
        //menu.add("group_menu_item","integer id of the menu item","sort order","label of the menu");
    }

    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch(item.getItemId()){
            case 1:
                editSystem(String.valueOf(info.id));
                break;
            case 2:
                deactivateConfirmDialogue(String.valueOf(info.id));
                break;
            case 3:
                deleteConfirmDialogue(String.valueOf(info.id));
                break;
        }

        return true;
    }

    public void editSystem(String id){
        Intent i = new Intent(getApplicationContext(), AddSystemActivity.class);
        i.putExtra("rowId", id);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        //finish();
    }

    public void deleteConfirmDialogue(final String id){
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Delete server");
        alert.setMessage("This command will delete this server's configuration details. This action is irreversible.");
        alert.setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.deleteSystem(id);
                        Toast.makeText(MainActivity.this, "System deleted", Toast.LENGTH_SHORT).show();
                        finish();
                        Intent home = new Intent(MainActivity.this, MainActivity.class);
                        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(home);
                    }
                });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    public void deactivateConfirmDialogue(final String id){
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Activate/Deactivate");
        alert.setMessage("This command will activate/deactivate this server.");
        alert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.changeSystemStatus(id);
                        finish();
                        Intent home = new Intent(MainActivity.this, MainActivity.class);
                        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(home);
                    }
                });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    //Select level
    public void selectLevelDialog(boolean error) {

        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View promptsView = li.inflate(R.layout.prompt_level_layout, null);

        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptsView);

        TextView prompt_instruction = (TextView) promptsView.findViewById(R.id.level_prompt_instruction);
        final Spinner level_spinner = (Spinner) promptsView.findViewById(R.id.input_level);
        final EditText time_edittext = (EditText) promptsView.findViewById(R.id.etTime);

        HashMap<String, String> listOfLevels = new HashMap<String, String>();
        listOfLevels.put("1", "seconds");
        listOfLevels.put("2", "minutes");
        listOfLevels.put("3", "hours");
        listOfLevels.put("4", "days");
        listOfLevels.put("5", "weeks");
        listOfLevels.put("6", "months");
        listOfLevels.put("7", "years");


        List<String> spinnerItems = new ArrayList<String>(listOfLevels.values());

        //populate level spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, spinnerItems);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        level_spinner.setAdapter(dataAdapter);

        if(error) {
            prompt_instruction.setText("please select a valid time interval");
        }else{
            prompt_instruction.setText("enter time interval between polls");
        }

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                try{
                                String level_selected = level_spinner.getSelectedItem().toString();
                                ;
                                int time_selected = Integer.parseInt(time_edittext.getText().toString());
                                if (!level_selected.equalsIgnoreCase("select") && time_selected > 0) {
                                    Log.e(TAG, "Valid choice");

                                    int time_in_ms = 10 * 1000; //default 10secs
                                    switch (level_selected) {
                                        case "seconds":
                                            time_in_ms = time_selected * 1000;
                                            break;
                                        case "minutes":
                                            time_in_ms = time_selected * 60 * 1000;
                                            break;
                                        case "hours":
                                            time_in_ms = time_selected * 60 * 60 * 1000;
                                            break;
                                        case "days":
                                            time_in_ms = time_selected * 60 * 60 * 24 * 1000;
                                            break;
                                        case "weeks":
                                            time_in_ms = time_selected * 60 * 60 * 24 * 7 * 1000;
                                            break;
                                        case "months":
                                            time_in_ms = time_selected * 60 * 60 * 24 * 7 * 4 * 1000;
                                            break;
                                        case "years":
                                            time_in_ms = time_selected * 60 * 60 * 24 * 7 * 4 * 12 * 1000;
                                            break;
                                        default:
                                            time_in_ms = time_selected * 1000;
                                            break;
                                    }

                                    db.updatePollTime("1", time_in_ms + "");
                                    Log.e(TAG, "Time in ms: " + time_in_ms);

                                    setReminderToPoll(true);

                                } else {
                                    //make prompt again
                                    selectLevelDialog(true);
                                    Log.e(TAG, "Invalid choice");
                                }

                            }catch(Exception e){
                                e.printStackTrace();
                                    selectLevelDialog(true);
                            }

                        }
    })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                Log.e(TAG, "Canceled");
                            }
                        });
        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }



}