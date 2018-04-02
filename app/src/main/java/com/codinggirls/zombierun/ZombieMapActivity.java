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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.Timer;
import java.util.TimerTask;

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
    Location lastuserlocation;
    boolean firstZoom = false;
    boolean activated = false;
    boolean firstRun = true;
    boolean caught = false;
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

    //The primary method of the activity, automatically called by Android. We override it in order to get it to do what we want.
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getDataFromBundle();
        cmeter = (Chronometer) findViewById(R.id.chronometer);
        activate = (Button) findViewById(R.id.buttonactivate);
        activate.setText("Start!");
        checkLocationPermission();
        mCountDownText = findViewById(R.id.start_timer);
        mCountDownText.setVisibility(View.GONE);
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
                activated = true;

            }
        });


    }

    //code for the chronometer/Timer. Methods to start, stop, and fetch the recorded value
    public void startclock() {
        cmeter.start();
    }

    public void stopclock() {
        cmeter.stop();
    }

    public long fetchclock() {
        return cmeter.getBase();
    }


    //Required code for LocationListener,  LocationRequests serve to tell Android that we wish to use
    // GPS and GoogleMaps in order to track our location. In order to effectively track this location in GoogleMaps,
    //We need to connect to the Google Maps API.
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

    //In theory, this method is called every time the User's location changes. In practice, it is called every few seconds automatically
    @Override
    public void onLocationChanged(Location location) {
        //Heres the code to track user location. Extracting Coordinates from location. One can add markers or do whatever when the user momves.

        mLastLocation = location;

        lat = mLastLocation.getLatitude();
        lon = mLastLocation.getLongitude();
        LatLng loc = new LatLng(lat, lon);


        //initial zoom
        if (firstZoom == false) {
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            firstZoom = true;
        }
        //If the user has pressed the start button, and we have zombie markers present, we begin the game
        if (activated == true && allMarkers.size() != 0) {


            // Follow player once first, in order to update lastuserlocation and avoid a null error
            if (firstRun == true) {
                for (int i = 0; i < allMarkers.size(); i++) {

                    followPlayer(loc, allMarkers.get(i), 3000);
                    lastuserlocation = mLastLocation;
                    firstRun = false;
                }
            } else {
                //if user hasnt moved , then we catch, else we follow
                boolean close = checkifclose(mLastLocation, lastuserlocation);
                if (close == true && caught == false) {
                    for (int i = 0; i < allMarkers.size(); i++) {
                        catchPlayer(loc, allMarkers.get(i), 3000);
//                        Toast.makeText(getApplicationContext(), "Caught!", Toast.LENGTH_LONG).show();
                        showCaughtDialog();
                    }
                }
                if (close != true && caught == false) {
                    for (int i = 0; i < allMarkers.size(); i++) {

                        followPlayer(loc, allMarkers.get(i), 3000);
                        lastuserlocation = mLastLocation;


                    }
                }
            }


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

    //This code gets called by GoogleMaps as soon as the Map is Ready. We want to put all map-related code here
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

    //spwns a zombie Marker on map
    public void spawnzombie(LatLng zombielatlng) {
        drawZombieMarker(zombielatlng, zombieID);
        zombieID += 1;
    }

    // A fallback check in case we do not have GoogleAPI for any reason (i.e. no internet)
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

    //Many Android methods, such as accessing the internet, or using GPS, require the permission of the user. We either request them in the manifest
    //Or do it here.
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


    //callback from RequestPermissions() method, handling the user's response to our requests
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

    //called when you minimize app
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
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey checkout my score on Zombie run :  " + mPlayerName + " : " + mScore + " seconds");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }


    //When instructed to follow, the zombies will travel 1/3 of the distance to the player.

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

    //when instructed to catch, zombies will move to the user, and then score will be shared.
    //Customization options: check if distance between user and zombie is too large.
    private void catchPlayer(LatLng userloc, Marker zombiemarker, float speed) {
        double userlat = userloc.latitude;
        double userlng = userloc.longitude;
        LatLng zombieloc = zombiemarker.getPosition();

        LatLngInterpolators interpolator = new LatLngInterpolators.LinearFixed();

        animateMarkerToGB(zombiemarker, userloc, interpolator, speed);
        cmeter.stop();
        mScore = fetchclock();
        ;
        caught = true;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                
            }
        }, 3000);


    }

    //method to measure up two locations to check if we are close enough
    private boolean checkifclose(Location userloc, Location zombieloc) {
        float distance = userloc.distanceTo(zombieloc);

        if (distance < 5) {
            return true;

        } else {
            return false;
        }
    }


    // animate  marker based  on any interpolator , (Linear , LinearFixed, or spherical)
    //An interpolator is simply a class used to animate something.
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

    private void showCaughtDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Caught!!")
                .setPositiveButton("Start Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // reset everything
                        activated = false;
                    }
                })
                .setCancelable(false)
                .setNeutralButton("Share Score", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shareScore();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create().show();
    }
}

