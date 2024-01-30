package net.devemperor.wristassist.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.database.UsageModel;
import net.devemperor.wristassist.util.Util;

import java.util.List;
import java.util.Locale;


public class UsageAdapter extends ArrayAdapter<UsageModel> {
    final Context context;
    final List<UsageModel> objects;


    public UsageAdapter(@NonNull Context context, @NonNull List<UsageModel> objects) {
        super(context, -1, objects);
        this.context = context;
        this.objects = objects;
    }

    @NonNull
    @Override
    public View getView (int position, View convertView, @NonNull ViewGroup parent) {
        View listItem = LayoutInflater.from(context).inflate(R.layout.item_usage, parent, false);

        UsageModel dataProvider = objects.get(position);

        TextView modelNameTv = listItem.findViewById(R.id.usage_model_tv);
        modelNameTv.setText(Util.translateModelNames(dataProvider.getModelName()));
        modelNameTv.setTextSize(18 * Util.getFontMultiplier(context));

        TextView tokensTv = listItem.findViewById(R.id.usage_tokens_tv);
        tokensTv.setText(context.getString(R.string.wristassist_token_usage,
                String.format(Locale.getDefault(), "%,d", dataProvider.getTokens())));
        tokensTv.setTextSize(16 * Util.getFontMultiplier(context));

        TextView costTv = listItem.findViewById(R.id.usage_cost_tv);
        costTv.setText(context.getString(R.string.wristassist_estimated_cost,
                String.format(Locale.getDefault(), "%,.2f", dataProvider.getCost())));
        costTv.setTextSize(16 * Util.getFontMultiplier(context));

        return listItem;
    }
}