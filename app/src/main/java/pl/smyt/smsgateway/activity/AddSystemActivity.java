package pl.smyt.smsgateway.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import pl.smyt.smsgateway.R;
import pl.smyt.smsgateway.utility.ConnectionDetector;
import pl.smyt.smsgateway.utility.DatabaseHelper;


public class AddSystemActivity extends ActionBarActivity {

    ConnectionDetector cd;
    private DatabaseHelper db;

    private EditText etSystemName, etOutboxURL;
    private Spinner spnStatus;
    private Button btnAddSystem;
    private int Bundles=0;

    private String strSystemName=null,  strOutboxURL=null, strStatus=null;
    private String rowId=null, system_name=null, inbox_Url=null, outbox_Url=null, system_status=null;
    private boolean duplicateSystemName=false , duplicateOutboxUrl=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_system);
        cd = new ConnectionDetector(getApplicationContext());

        db = new DatabaseHelper(this);

        etSystemName = (EditText) findViewById(R.id.etSystemName);
        etOutboxURL = (EditText) findViewById(R.id.etOutBoxURL);
        spnStatus = (Spinner) findViewById(R.id.spnStatus);
        btnAddSystem = (Button) findViewById(R.id.btnAddSystem);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            rowId = extras.getString("rowId");
            Cursor systemDetailsCursor = db.getSystemDetails(rowId);
            systemDetailsCursor.moveToFirst();
            system_name = systemDetailsCursor.getString(systemDetailsCursor.getColumnIndex(systemDetailsCursor.getColumnName(1)));
            outbox_Url = systemDetailsCursor.getString(systemDetailsCursor.getColumnIndex(systemDetailsCursor.getColumnName(2)));
            system_status = systemDetailsCursor.getString(systemDetailsCursor.getColumnIndex(systemDetailsCursor.getColumnName(3)));

            etSystemName.setText(system_name);
            etOutboxURL.setText(outbox_Url);
            if(system_status.equalsIgnoreCase("Active")){
                spnStatus.setSelection(0);
            }else{
                spnStatus.setSelection(1);
            }
            btnAddSystem.setText("Update");
            Bundles++;
        }

        registerButtonListeners();

    }

    public void registerButtonListeners(){
        btnAddSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Bundles==0) {
                    saveSystem();
                }else{
                    updateSystem(rowId);
                }
            }
        });
    }

    public int validateEntries() {
        int errors = 0;
        strSystemName = etSystemName.getText().toString();
        strOutboxURL = etOutboxURL.getText().toString();
        strStatus = spnStatus.getSelectedItem().toString();

        if ((strSystemName.equalsIgnoreCase("") || (strSystemName == null)) && (errors == 0)) {
            errors++;
            errorToast("Enter the name of your system");
        }
        if(Bundles==0) {
            duplicateSystemName = db.getDuplicateSystemName(strSystemName);
        }else{
            duplicateSystemName = db.getDuplicateSystemName(strSystemName, rowId);
        }
        if (duplicateSystemName && errors == 0) {
            errors++;
            errorToast("This system name already Exists");
        }


        if ((strOutboxURL.equalsIgnoreCase("") || (strOutboxURL == null)) && (errors == 0)) {
            errorToast("Enter your Server URL");
            errors++;
        }
        if (!(URLUtil.isValidUrl(strOutboxURL)) && (errors == 0)) {
            errorToast("Invalid Server URL");
            errors++;
        }
        if(Bundles==0) {
            duplicateOutboxUrl = db.getDuplicateOutBoxURL(strOutboxURL);
        }else{
            duplicateOutboxUrl = db.getDuplicateOutBoxURL(strOutboxURL, rowId);
        }
        if (duplicateOutboxUrl && errors == 0) {
            errors++;
            errorToast("This url is being used by another system");
        }

        if ((strStatus.equalsIgnoreCase("") || (strStatus == null)) && (errors == 0)) {
            errorToast("Select status of your server");
            errors++;
        }
        return errors;
    }

    public void saveSystem(){
        int errors = validateEntries();
        if(errors==0){
            db.insertSystem(strSystemName, strOutboxURL, strStatus);
            errorToast("Saved");
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }
    }

    public void updateSystem(String rowId){
        int errors = validateEntries();
        if(errors==0){
            db.updateSystem(rowId, strSystemName, strOutboxURL, strStatus);
            errorToast("Updated");
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }
    }

    private void errorToast(String tst){
        Toast.makeText(AddSystemActivity.this, tst, Toast.LENGTH_SHORT).show();
    }

}
