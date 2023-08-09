package net.devemperor.wristassist.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.activities.MainActivity;

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
        if (position == layoutIds.length - 1) {
            ImageView qrCodeIv = holder.itemView.findViewById(R.id.qrcode_iv);
            qrCodeIv.setOnClickListener(v -> {
                activity.getSharedPreferences("net.devemperor.wristassist", Activity.MODE_PRIVATE)
                        .edit().putBoolean("net.devemperor.wristassist.onboarding_complete", true).apply();
                v.getContext().startActivity(new Intent(v.getContext(), MainActivity.class));
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