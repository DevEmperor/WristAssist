package net.devemperor.wristassist.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.adapters.UsageAdapter;
import net.devemperor.wristassist.database.UsageDatabaseHelper;

import java.util.Locale;

public class UsageActivity extends AppCompatActivity {

    ListView usageLv;
    Button resetUsageBtn;
    TextView totalCostTv;

    UsageDatabaseHelper usageDatabaseHelper;
    UsageAdapter usageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage);

        usageDatabaseHelper = new UsageDatabaseHelper(this);
        usageAdapter = new UsageAdapter(this, usageDatabaseHelper.getAll());
        usageLv = findViewById(R.id.activity_usage_lv);
        usageLv.setAdapter(usageAdapter);

        View footerView = LayoutInflater.from(this).inflate(R.layout.layout_usage_footer, usageLv, false);
        resetUsageBtn = footerView.findViewById(R.id.layout_usage_footer_reset_btn);

        totalCostTv = footerView.findViewById(R.id.layout_usage_footer_total_cost_tv);
        totalCostTv.setText(getString(R.string.wristassist_total_cost,
                String.format(Locale.getDefault(), "%,.2f", usageDatabaseHelper.getTotalCost())));

        usageLv.addFooterView(footerView);

        usageLv.requestFocus();

        if (usageAdapter.getCount() == 0) {
            noUsage();
        }
    }

    public void resetUsage(View view) {
        usageDatabaseHelper.reset();
        usageAdapter.clear();
        usageAdapter.addAll(usageDatabaseHelper.getAll());
        usageAdapter.notifyDataSetChanged();

        noUsage();
    }

    private void noUsage() {
        totalCostTv.setText(getString(R.string.wristassist_no_usage_yet));
        resetUsageBtn.setEnabled(false);
    }
}
