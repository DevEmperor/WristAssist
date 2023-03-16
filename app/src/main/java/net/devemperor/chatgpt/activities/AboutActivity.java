package net.devemperor.chatgpt.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import net.devemperor.chatgpt.BuildConfig;
import net.devemperor.chatgpt.R;


public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView aboutText = findViewById(R.id.version_tv);
        aboutText.setText(getString(R.string.chatgpt_about, BuildConfig.VERSION_NAME));
    }
}