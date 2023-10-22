package net.devemperor.wristassist.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.activities.MainActivity;
import net.devemperor.wristassist.util.Util;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {

    Activity activity;
    int[] layoutIds;

    public OnboardingAdapter(Activity activity, int[] layoutIds) {
        this.activity = activity;
        this.layoutIds = layoutIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutIds[viewType], parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 0) {
            TextView welcomeTv = holder.itemView.findViewById(R.id.welcome_tv);
            welcomeTv.setTextSize(20 * Util.getFontMultiplier(welcomeTv.getContext()));
        } else if (position == 1) {
            TextView infoTv = holder.itemView.findViewById(R.id.info_tv);
            infoTv.setTextSize(16 * Util.getFontMultiplier(infoTv.getContext()));
        } else if (position == 2) {
            ImageView qrCodeIv = holder.itemView.findViewById(R.id.qrcode_iv);
            qrCodeIv.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                intent.putExtra("net.devemperor.wristassist.enter_api_key", true);
                v.getContext().startActivity(intent);
                activity.finish();
            });
        }
    }

    @Override
    public int getItemCount() {
        return layoutIds.length;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}