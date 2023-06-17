package net.devemperor.chatgpt.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import net.devemperor.chatgpt.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_settings, new PreferencesFragment())
                .commit();
    }
}