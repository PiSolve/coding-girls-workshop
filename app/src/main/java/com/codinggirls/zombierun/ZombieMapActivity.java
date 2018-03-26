package com.codinggirls.zombierun;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;

/**
 * Created by renu.yadav on 21/3/18.
 */

public class ZombieMapActivity extends AppCompatActivity {
    private static final String PLAYER_NAME = "playerName";
    private String mPlayerName = "";
    private Integer mScore = 0;
    private TextView mCountDownText;

    public static void startActivity(String name, Context context) {
        Intent intent = new Intent(context, ZombieMapActivity.class);
        intent.putExtra(PLAYER_NAME, name);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zombie_run_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getDataFromBundle();
        mCountDownText = findViewById(R.id.start_timer);
        mCountDownText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCountDownTimer();
            }
        });
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
            }
        }.start();
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
            shareScore();
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareScore() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey checkout my score on Zombie run :  " + mPlayerName + " :" + mScore);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }


    // animate  marker based  on any interpolator , (Linear , LinearFixed, or spherical
    // TODO , once map is ready ,  need to test this @Adrian to take care of map set up 
    private void animateMarkerToGB(final Marker marker, final LatLng finalPosition, final LatLngInterpolators latLngInterpolator) {
        final LatLng startPosition = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;

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
