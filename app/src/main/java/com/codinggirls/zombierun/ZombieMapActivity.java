package com.codinggirls.zombierun;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Created by renu.yadav on 21/3/18.
 */

public class ZombieMapActivity extends AppCompatActivity {
    private static final String PLAYER_NAME = "playerName";
    private String mPlayerName = "";

    public static void startActivity(String name, Context context) {
        Intent intent = new Intent(context, ZombieMapActivity.class);
        intent.putExtra(PLAYER_NAME, name);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getDataFromBundle();
    }

    private void getDataFromBundle() {
        mPlayerName = getIntent().getStringExtra(PLAYER_NAME);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
