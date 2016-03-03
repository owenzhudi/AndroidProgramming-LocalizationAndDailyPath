package com.example.zhudi.localization;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,
        OnMapReadyCallback{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "MapsActivity";
    private Location mCurrentLocation;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mUpdateTimeText;
    private TextView mAddressText;
    private Button mCheckInButton;
    private Button mHeatmapButton;
    private Button mCheckedInLocationsButton;
    private Button mGpsVsNetworkButton;
    private LocationRequest mLocationRequest;
    private String mLastUpdateTime;
    private String mAddressOutput;
    private boolean mRequestingLocationUpdates;
    private boolean mAddressRequested;
    private AddressResultReceiver mResultReceiver;
    private MapFragment mMapFragment;
    private LocationData mLocationData;
    private LocationRecord mLocationRecord;

    private static final String REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES";
    private static final String LOCATION_KEY = "LOCATION";
    private static final String LAST_UPDATED_TIME_STRING_KEY = "LAST_UPDATED_TIME_STRING";
    private static final String ADDRESS_KEY = "ADDRESS";

    private static final LatLng BUSCH_CAMPUS_CENTER = new LatLng(40.523128, -74.458797);
    private static final LatLng STADIUM = new LatLng(40.513817, -74.464844);
    private static final LatLng EE_BUILDING = new LatLng(40.521663, -74.460665);
    private static final LatLng RUTGERS_STUDENT_CENTER = new LatLng(40.502661, -74.451771);
    private static final LatLng OLD_QUEENS = new LatLng(40.498720, -74.446229);

    private Marker buschCampusCenterMarker;
    private Marker stadiumMarker;
    private Marker eeBuildingMarker;
    private Marker rutgersStudentCenterMarker;
    private Marker oldQueensMarker;


    // request permission needed for Android 6.0
    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int MY_PERMISSION_REQUEST_LOCATION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        //mLatitudeText = (TextView) findViewById(R.id.latitude_text);
        //mLongitudeText = (TextView) findViewById(R.id.longitude_text);
        mUpdateTimeText = (TextView) findViewById(R.id.update_time_text);
        mAddressText = (TextView) findViewById(R.id.address_text);
        mCheckInButton = (Button) findViewById(R.id.check_in_button);
        mHeatmapButton = (Button) findViewById(R.id.heat_map_button);
        mCheckedInLocationsButton = (Button) findViewById(R.id.checked_in_locations_button);
        mGpsVsNetworkButton = (Button) findViewById(R.id.gps_vs_network_button);
        setUpMapIfNeeded();
        Thread mainThread = Thread.currentThread();
        long id = mainThread.getId();
        Log.d(TAG, "Main thread id: " + id);

        //mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        //mMapFragment.getMapAsync(this);
        mLocationData = new LocationData(this);

        updateValuesFromBundle(savedInstanceState);
        mResultReceiver = new AddressResultReceiver(new Handler());
        mCheckInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // update location and save to SQLite database
                startLocationUpdates();
                startIntentService();
                //updateUI();
                mUpdateTimeText.setText(mLastUpdateTime);
                mLocationData.addLocation(new LocationRecord(mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude(), mLastUpdateTime));
                Toast.makeText(MapsActivity.this, "You have checked in!", Toast.LENGTH_LONG).show();
                //mLocationData.addLocation(mCurrentLocation, mAddressOutput, mLastUpdateTime);
            }
        });

        mHeatmapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addHeatMap();
            }
        });

        mCheckedInLocationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MapsActivity.this, LocationDataActivity.class);
                startActivity(i);
            }
        });

        mGpsVsNetworkButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MapsActivity.this, GpsVsNetworkMapsActivity.class);
                startActivity(i);
            }
        });

        // request permission for Android 6.0
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MapsActivity.this,
                        LOCATION_PERMS,
                        MY_PERMISSION_REQUEST_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            if(savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
            }
            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if(savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if(savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }

            if(savedInstanceState.keySet().contains(ADDRESS_KEY)) {
                mAddressOutput = savedInstanceState.getString(ADDRESS_KEY);
            }

            //updateUI();
        }
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mCurrentLocation != null) {
            // determine whether a Geocoder is available
            LocationAddress locationAddress = new LocationAddress();
            locationAddress.getAddressFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                    getApplicationContext(), new GeocoderHandler());
            setDistanceSnippet();
            if(!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
                return;
            }
            if(mAddressRequested) {
                startIntentService();
            }
            //updateUI();
        }
    }

    protected void startLocationUpdates() {
        createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mCurrentLocation);
        startService(intent);

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        Thread callBackThread = Thread.currentThread();
        long id = callBackThread.getId();
        Log.d("TAG", "Callback thread ID: " + id);
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        startIntentService();
        LocationAddress locationAddress = new LocationAddress();
        locationAddress.getAddressFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                getApplicationContext(), new GeocoderHandler());

        setDistanceSnippet();
//        fetchAddress();
       //updateUI();
    }

    private void setDistanceSnippet() {
        LatLng currentLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        buschCampusCenterMarker.setSnippet("Distance: "+
                (int) SphericalUtil.computeDistanceBetween(currentLocation, BUSCH_CAMPUS_CENTER)+ " m");
        stadiumMarker.setSnippet("Distance: " +
                (int) SphericalUtil.computeDistanceBetween(currentLocation, STADIUM) + " m");
        eeBuildingMarker.setSnippet("Distance: " +
                (int) SphericalUtil.computeDistanceBetween(currentLocation, EE_BUILDING) + " m");
        rutgersStudentCenterMarker.setSnippet("Distance: " +
                (int) SphericalUtil.computeDistanceBetween(currentLocation, RUTGERS_STUDENT_CENTER) + " m");
        oldQueensMarker.setSnippet("Distance: " +
                (int) SphericalUtil.computeDistanceBetween(currentLocation, OLD_QUEENS) + " m");
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            mAddressText.setText(locationAddress);
        }
    }

    // add heatmap
    private void addHeatMap() {
        List<LatLng> list = new ArrayList<>();

        ArrayList<LocationRecord> locations = mLocationData.getLocations();
        for(LocationRecord loc : locations) {
            list.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
        }

        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder().data(list).build();

        TileOverlay mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

    }

    /**
    private void updateUI() {
        mLatitudeText.setText(String.valueOf(mCurrentLocation.getLatitude()));
        mLongitudeText.setText(String.valueOf(mCurrentLocation.getLongitude()));
        mUpdateTimeText.setText(mLastUpdateTime);
        //mAddressText.setText(mAddressOutput);
    }
     */

    /**
    private void fetchAddress() {
        if(mGoogleApiClient.isConnected() && mCurrentLocation != null) {
            startIntentService();
            mAddressRequested = true;
            updateUI();
        }
    }
     */

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            Log.d(TAG, "Address: " + mAddressOutput);

            // show a toast message is address is found
            if(resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(MapsActivity.this, R.string.address_found, Toast.LENGTH_LONG).show();
            }
        }

    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Toast.makeText(this, "Connection failed!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Toast.makeText(this, "Connection suspended", Toast.LENGTH_LONG).show();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        createLocationRequest();
        if(mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
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
        UiSettings uiSettings = mMap.getUiSettings();
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        onMapReady(mMap);
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
        mMap.setMyLocationEnabled(true);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        // add markers
        /**
        LatLng currentLocation;
        try {
            currentLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }catch (NullPointerException nullException){
            Log.d(TAG, "current location is null");
            //startLocationUpdates();
            currentLocation = new LatLng(40.5, -74.45);
        }
         */

        // add markers
        buschCampusCenterMarker = mMap.addMarker(new MarkerOptions().position(BUSCH_CAMPUS_CENTER)
                .title("Busch Campus Center"));
                //.snippet("Distance: "+ (int) SphericalUtil.computeDistanceBetween(currentLocation, BUSCH_CAMPUS_CENTER)+ " m"));
        buschCampusCenterMarker.showInfoWindow();
        stadiumMarker = mMap.addMarker(new MarkerOptions().position(STADIUM)
                .title("HighPoint Solution Stadium"));
                //.snippet("Distance: " + (int) SphericalUtil.computeDistanceBetween(currentLocation, STADIUM) + " m"));
        stadiumMarker.showInfoWindow();
        eeBuildingMarker = mMap.addMarker(new MarkerOptions().position(EE_BUILDING)
                .title("Electrical Engineering Building"));
                //.snippet("Distance: " + (int) SphericalUtil.computeDistanceBetween(currentLocation, EE_BUILDING) + " m"));
        eeBuildingMarker.showInfoWindow();
        rutgersStudentCenterMarker = mMap.addMarker(new MarkerOptions().position(RUTGERS_STUDENT_CENTER)
                .title("Rutgers Student Center"));
                //.snippet("Distance: " + (int) SphericalUtil.computeDistanceBetween(currentLocation, RUTGERS_STUDENT_CENTER) + " m"));
        rutgersStudentCenterMarker.showInfoWindow();
        oldQueensMarker = mMap.addMarker(new MarkerOptions().position(OLD_QUEENS)
                .title("Old Queens"));
                //.snippet("Distance: " + (int) SphericalUtil.computeDistanceBetween(currentLocation, OLD_QUEENS) + "m"));
        oldQueensMarker.showInfoWindow();

    }


    @Override
    protected void onPause() {
        super.onPause();
        //stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        savedInstanceState.putString(ADDRESS_KEY, mAddressOutput);
        super.onSaveInstanceState(savedInstanceState);
    }
}
