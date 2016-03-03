package com.example.zhudi.localization;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

// Graduate student's mandatory additional assignment
public class GpsVsNetworkMapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private double mLatGps, mLongGps; // latitude and longitude for GPS
    private double mLatNet, mLongNet; // latitude and longitude for network
    private LocationManager mLocationManager;
    private Location mGpsLocation;
    private Location mNetworkLocation;
    private boolean isGpsEnabled;
    private boolean isNetworkEnabled;
    private float mAccuracyGps, mAccuracyNetwork;

    private TextView mGpsAccuracyText;
    private TextView mNetworkAccuracyText;
    private TextView mGpsDelayText;
    private TextView mNetworkDelayText;
    private TextView mDistanceText;
    private Button mUpdateLocation;

    private long startTime;
    private long gpsTime;
    private long networkTime;

    // request permission needed for Android 6.0
    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int MY_PERMISSION_REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_vs_network_maps);
        setUpMapIfNeeded();
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        isGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        mGpsAccuracyText = (TextView) findViewById(R.id.accuracy_gps_text);
        mNetworkAccuracyText = (TextView) findViewById(R.id.accuracy_network_text);
        mGpsDelayText = (TextView) findViewById(R.id.delay_gps_text);
        mNetworkDelayText = (TextView) findViewById(R.id.delay_network_text);
        mDistanceText = (TextView) findViewById(R.id.distance_text);

        mUpdateLocation = (Button) findViewById(R.id.update_location_button);
        mUpdateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isGpsEnabled && !isNetworkEnabled) {
                    Toast.makeText(GpsVsNetworkMapsActivity.this, "Nothing is enabled", Toast.LENGTH_LONG).show();
                }

                if(isGpsEnabled) {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
                }
                if(isNetworkEnabled) {
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);

                }
                startTime = System.currentTimeMillis();
                // request permission for Android 6.0
                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(GpsVsNetworkMapsActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(GpsVsNetworkMapsActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {

                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(GpsVsNetworkMapsActivity.this,
                                LOCATION_PERMS,
                                MY_PERMISSION_REQUEST_LOCATION);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }

            }
        });
        /**
        if(!isGpsEnabled && !isNetworkEnabled) {
            Toast.makeText(this, "Nothing is enabled", Toast.LENGTH_LONG).show();
        }

        if(isGpsEnabled) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        }
        if(isNetworkEnabled) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);

        }



        // request permission for Android 6.0
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(GpsVsNetworkMapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(GpsVsNetworkMapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(GpsVsNetworkMapsActivity.this,
                        LOCATION_PERMS,
                        MY_PERMISSION_REQUEST_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
         */
    }


    LocationListener locationListenerGps = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            gpsTime = System.currentTimeMillis();
            mGpsLocation = location;
            mLatGps = location.getLatitude();
            mLongGps = location.getLongitude();
            mAccuracyGps = location.getAccuracy();
            mGpsAccuracyText.setText(String.valueOf(mAccuracyGps));
            mGpsDelayText.setText(String.valueOf(gpsTime - startTime) + " ms");
            if(mGpsLocation != null && mNetworkLocation != null) {
                mDistanceText.setText(String.valueOf(distance(mGpsLocation, mNetworkLocation)) + " m");
            }
            startTime = 0;
            gpsTime = 0;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            mNetworkLocation = location;
            networkTime = System.currentTimeMillis();
            mLatNet = location.getLatitude();
            mLongNet = location.getLongitude();
            mAccuracyNetwork = location.getAccuracy();
            mNetworkAccuracyText.setText(String.valueOf(mAccuracyNetwork));
            mNetworkDelayText.setText(String.valueOf(networkTime - startTime) + " ms");
            if(mGpsLocation != null && mNetworkLocation != null) {
                mDistanceText.setText(String.valueOf(distance(mGpsLocation, mNetworkLocation)) + " m");
            }
            startTime = 0;
            networkTime = 0;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private double distance(Location loc1, Location loc2) {
        LatLng l1 = new LatLng(loc1.getLatitude(), loc1.getLongitude());
        LatLng l2 = new LatLng(loc2.getLatitude(), loc2.getLongitude());
        double distance = SphericalUtil.computeDistanceBetween(l1, l2);
        return distance;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
    }
}
