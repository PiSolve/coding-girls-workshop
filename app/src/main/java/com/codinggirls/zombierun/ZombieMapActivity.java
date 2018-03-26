package com.codinggirls.zombierun;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
}
