package com.codinggirls.zombierun;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

/**
 * Created by renu.yadav on 20/3/18.
 */

public class PlayerDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_details);
        findViewById(R.id.play_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = ((EditText) findViewById(R.id.player_name)).getText().toString();
                // start map activity
                ZombieMapActivity.startActivity(name, PlayerDetailsActivity.this);
                finish();
            }
        });
    }
}
