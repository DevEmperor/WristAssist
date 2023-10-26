package net.devemperor.wristassist.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import net.devemperor.wristassist.BuildConfig;
import net.devemperor.wristassist.R;
import net.devemperor.wristassist.util.Util;

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
        aboutText.setText(getString(R.string.wristassist_about, BuildConfig.VERSION_NAME));
        aboutText.setTextSize(16 * Util.getFontMultiplier(this));

        totalCost = findViewById(R.id.total_cost_tv);
        totalCost.setTextSize(16 * Util.getFontMultiplier(this));
        sp = getSharedPreferences("net.devemperor.wristassist", MODE_PRIVATE);
        refreshTotalCostTv();

        totalCost.setOnLongClickListener(v -> {
            sp.edit().putLong("net.devemperor.wristassist.total_tokens", 0).apply();
            Toast.makeText(v.getContext(), R.string.wristassist_reset_cost_message, Toast.LENGTH_SHORT).show();
            refreshTotalCostTv();
            return true;
        });
    }

    private void refreshTotalCostTv() {
        totalCost.setText(getString(R.string.wristassist_total_cost, df.format(sp.getLong("net.devemperor.wristassist.total_tokens", 0) / 1000.0)));
    }
}