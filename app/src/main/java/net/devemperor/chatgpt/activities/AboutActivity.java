package net.devemperor.chatgpt.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.wear.widget.ConfirmationOverlay;

import net.devemperor.chatgpt.BuildConfig;
import net.devemperor.chatgpt.R;

import java.text.DecimalFormat;


public class AboutActivity extends Activity {

    DecimalFormat df = new DecimalFormat("#.#");
    TextView totalCost;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView aboutText = findViewById(R.id.version_tv);
        aboutText.setText(getString(R.string.chatgpt_about, BuildConfig.VERSION_NAME));

        totalCost = findViewById(R.id.total_cost_tv);
        sp = getSharedPreferences("net.devemperor.chatgpt", MODE_PRIVATE);
        refreshTotalCostTv();

        totalCost.setOnLongClickListener(v -> {
            sp.edit().putLong("net.devemperor.chatgpt.total_tokens", 0).apply();
            new ConfirmationOverlay().setMessage(getString(R.string.chatgpt_reset_cost_message)).showOn(this);
            refreshTotalCostTv();
            return true;
        });
    }

    private void refreshTotalCostTv() {
        totalCost.setText(getString(R.string.chatgpt_total_cost, df.format(sp.getLong("net.devemperor.chatgpt.total_tokens", 0) / 1000.0)));
    }
}