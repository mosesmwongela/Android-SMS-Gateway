package pl.smyt.smsgateway.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import pl.smyt.smsgateway.R;

public class CustomSystemCursorAdapter extends CursorAdapter {
 
    @SuppressWarnings("deprecation")
	public CustomSystemCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }
 
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // when the view will be created for first time,
        // we need to tell the adapters, how each item will look
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = inflater.inflate(R.layout.single_system, parent, false);
        return retView;
    }
 
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // here we are setting our data
        // that means, take the data from the cursor and put it in views
    	
    	TextView tvSystemId = (TextView) view.findViewById(R.id.tvSystemId);
        tvSystemId.setText(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(0))));
 
        TextView tvSystemName = (TextView) view.findViewById(R.id.tvSystemName);
        tvSystemName.setText(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(1))));

        
        TextView tvOutBoxUrl = (TextView) view.findViewById(R.id.tvOutBoxUrl);
        tvOutBoxUrl.setText(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(2))));

        TextView tvSystemStatus = (TextView) view.findViewById(R.id.tvSystemStatus);
        tvSystemStatus.setText(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(3))));

    }
}