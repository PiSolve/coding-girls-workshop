package com.codinggirls.zombierun;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ZombieMapActivity extends AppCompatActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private static final String PLAYER_NAME = "playerName";
    private String mPlayerName = "";
    private long mScore = 0;
    private TextView mCountDownText;
    ArrayList<Marker> allMarkers;

    //Map Var
    GoogleMap googleMap;
    LocationManager locationManager;

    Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    double lat, lon;
    double userlat, userlon;
    Location userloc;
    boolean firstZoom = false;
    Chronometer cmeter;
    Button activate;
    int zombieID;
    //Check if user allowed permission already
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    //Renus old code
    public static void startActivity(String name, Context context) {
        Intent intent = new Intent(context, ZombieMapActivity.class);
        intent.putExtra(PLAYER_NAME, name);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getDataFromBundle();
        cmeter = (Chronometer) findViewById(R.id.chronometer);
        activate = (Button) findViewById(R.id.buttonactivate);
        checkLocationPermission();
        mCountDownText = findViewById(R.id.start_timer);
        allMarkers = new ArrayList<Marker>();
        mCountDownText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCountDownTimer();
            }
        });

        activate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                for (int i = 0; i < allMarkers.size(); i++) {
                    // todo , what are we trying to pass here @adrian ,user's current location , if not please change it accordingly
                    followPlayer(latLng, allMarkers.get(i), 3000);
                }
            }
        });


    }

    //2703 code for the chronometer
    public void startclock() {
        cmeter.start();
    }

    public void stopclock() {
        cmeter.stop();
    }

    public long fetchclock() {
        return cmeter.getBase();
    }


    //Required code for LocationListener, onConnected to Google Play Services, we will find
    @Override
    public void onConnected(Bundle bundle) {
        checkLocationPermission();
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100);
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {

            buildGoogleApiClient();
            mGoogleApiClient.connect();

        }
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }


        //TODO Upon connection, marker on current location, also used in checkArrival

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            lat = mLastLocation.getLatitude();
            lon = mLastLocation.getLongitude();
            LatLng loc = new LatLng(lat, lon);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
            /*

             */
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //Heres the code to track user location. One can add markers or do whatever when the user momves.
        mLastLocation = location;
        lat = mLastLocation.getLatitude();
        lon = mLastLocation.getLongitude();
        LatLng loc = new LatLng(lat, lon);


        //initial zoom
        if (firstZoom == false) {
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            firstZoom = true;
        }


    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        buildGoogleApiClient();
    }

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    ;

    //Connect and disconnect to ApiClient during start/destroy
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {

            buildGoogleApiClient();
            mGoogleApiClient.connect();

        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    //Code required for getMapAsync, part of onMapReadyCallback
    //GetMapAsync needs hence this callback method, where you can immediately set stuff
    @Override
    public void onMapReady(GoogleMap map) {


        this.googleMap = map;


        map.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        //Custom Map UI set up
        //disable zoom Controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        checkLocationPermission();

        //TODO heres where we can control the amount of zoom necessary to maintain the illusion of spawn and chase
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        //2703 spawning zombie onclick
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng zombielatlng) {
                spawnzombie(zombielatlng);
            }
        });


    }

    public void spawnzombie(LatLng zombielatlng) {
        drawZombieMarker(zombielatlng, zombieID);
        zombieID += 1;
    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int status = api.isGooglePlayServicesAvailable(this);
        //if we have a problem, return false
        if (status != ConnectionResult.SUCCESS) {
            if (api.isUserResolvableError(status)) {
                api.getErrorDialog(activity, status, 2404).show();
            }
            return false;

        }
        return true;

    }


    private void startCountDownTimer() {
        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished == 0) {
                    onFinish();
                } else {
                    mCountDownText.setText(millisUntilFinished / 1000 + "");
                }

            }

            @Override
            public void onFinish() {
                mCountDownText.setVisibility(View.GONE);
                startclock();
            }
        }.start();
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //No permission allowed, force user to give one
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.CAMERA, Manifest.permission.WRITE_SETTINGS, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_LOCATION);
            return false;
        } else {
            return true;
        }

    }

    //DrawZombieMarker
    private void drawZombieMarker(LatLng point, int ID) {
        Marker temp;
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

        temp = googleMap.addMarker(markerOptions);
        temp.setTag(ID);
        allMarkers.add(temp);

    }


    //callback from RequestPermissions() method
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                //if request is cancelled result arrays are empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted, so do everything related to locations
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        googleMap.setMyLocationEnabled(true);
                    }
                } else {
                    //permission denied
                    Toast.makeText(this, "permission denied, app functionality disabled", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        //Required CONNECT CALL TO ACTUALLY START FUSED LOCATION API

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {

            buildGoogleApiClient();
            mGoogleApiClient.connect();

        }

        if (googleMap == null) {
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            fm.getMapAsync(this);
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }


    private void getDataFromBundle() {
        mPlayerName = getIntent().getStringExtra(PLAYER_NAME);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.share_score) {
            mScore = fetchclock();
            shareScore();
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareScore() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey checkout my score on Zombie run :  " + mPlayerName + " :" + mScore + " seconds");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }


    //TODO Renu please check if this method makes sense. THis is called to follow player 33%,
    //if player not moving call catchplayer instead

    private void followPlayer(LatLng userloc, Marker zombiemarker, float speed) {
        double userlat = userloc.latitude;
        double userlng = userloc.longitude;
        LatLng zombieloc = zombiemarker.getPosition();
        double zombielat = zombiemarker.getPosition().latitude;
        double zombielng = zombiemarker.getPosition().longitude;
        LatLngInterpolators interpolator = new LatLngInterpolators.LinearFixed();

        double distancelat = userlat - zombielat;
        double distancelng = userlng - zombielng;

        double gainlat = distancelat / 3;
        double gainlng = distancelng / 3;

        double newzombielat = zombielat + gainlat;
        double newzombielng = zombielng + gainlng;

        LatLng finalzombiepos = new LatLng(newzombielat, newzombielng);

        animateMarkerToGB(zombiemarker, finalzombiepos, interpolator, speed);

    }


    private void catchPlayer(LatLng userloc, Marker zombiemarker, float speed) {
        double userlat = userloc.latitude;
        double userlng = userloc.longitude;
        LatLng zombieloc = zombiemarker.getPosition();

        LatLngInterpolators interpolator = new LatLngInterpolators.LinearFixed();

        animateMarkerToGB(zombiemarker, userloc, interpolator, speed);

    }

    // animate  marker based  on any interpolator , (Linear , LinearFixed, or spherical
    // TODO , once map is ready ,  need to test this @Adrian to take care of map set up
    //2603 CHANGED TO TAKE A SPEED VALUE.
    private void animateMarkerToGB(final Marker marker, final LatLng finalPosition,
                                   final LatLngInterpolators latLngInterpolator, float speed) {
        final LatLng startPosition = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = speed;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                marker.setPosition(latLngInterpolator.interpolate(v, startPosition, finalPosition));

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }
}

