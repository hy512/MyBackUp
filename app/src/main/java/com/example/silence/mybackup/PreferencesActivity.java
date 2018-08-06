package com.example.silence.mybackup;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        ((EditText)findViewById(R.id.store_path)).setText(getSharedPreferences("mybackup", Context.MODE_PRIVATE).getString("path", ""));
    }
}
