package com.example.zhudi.localization;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;

public class LocationDataActivity extends AppCompatActivity {

    private LocationData mLocationData;
    //private LocationBaseHelper mDataHelper;
    private ArrayList<LocationRecord> mCheckedInLocations;
    private TextView mLocationsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_data);
        mLocationsText = (TextView) findViewById(R.id.locations);
        //mDataHelper = new LocationBaseHelper(this);
        mLocationData = new LocationData(this);
        mCheckedInLocations = mLocationData.getLocations();
        //new GetDataTask().execute();
        mLocationsText.setText(getString());

    }

    private class GetDataTask extends AsyncTask<String, Integer, ArrayList<LocationRecord>> {

        @Override
        protected ArrayList<LocationRecord> doInBackground(String... params) {
            ArrayList<LocationRecord> locations = mLocationData.getLocations();

            return locations;
        }

        @Override
        protected void onPostExecute(ArrayList<LocationRecord> locations) {
            for(LocationRecord loc : locations) {
                mCheckedInLocations.add(loc);
            }
        }
    }

    private String getString() {
        StringBuilder sb = new StringBuilder();
        for(LocationRecord loc : mCheckedInLocations) {
            sb.append(loc.getLatitude() + " ");
            sb.append(loc.getLongitude() + " ");
            sb.append(loc.getUpdateTime());
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_location_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
