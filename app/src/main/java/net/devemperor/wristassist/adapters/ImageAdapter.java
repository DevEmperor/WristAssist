package net.devemperor.wristassist.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jsibbold.zoomage.ZoomageView;
import com.squareup.picasso.Picasso;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.database.ImageModel;

import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.RecyclerViewHolder> {

    private final List<ImageModel> data;
    private final AdapterCallback callback;

    public interface AdapterCallback {
        void onItemClicked(Integer menuPosition, ZoomageView image);
    }

    public ImageAdapter(List<ImageModel> data, AdapterCallback callback) {
        this.data = data;
        this.callback = callback;
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        final ZoomageView image;

        public RecyclerViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.open_image_iv);
        }
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, final int position) {
        if (position == 0) {
            holder.image.setImageResource(R.drawable.add_image);
        } else {
            Picasso.get().load(new File(holder.image.getContext().getFilesDir().getAbsolutePath()
                            + "/image_" + data.get(position).getId() + ".png")).into(holder.image);
        }
        holder.image.setOnClickListener(v -> {
            if (callback != null) {
                callback.onItemClicked(holder.getAdapterPosition(), holder.image);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public List<ImageModel> getData() {
        return data;
    }
}
