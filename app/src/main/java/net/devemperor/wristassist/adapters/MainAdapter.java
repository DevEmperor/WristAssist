package net.devemperor.wristassist.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.items.MainItem;

import java.io.IOException;
import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.RecyclerViewHolder> {

    private final List<MainItem> data;
    private final AdapterCallback callback;

    public interface AdapterCallback {
        void onItemClicked(Integer menuPosition, boolean longClick) throws IOException;
    }

    public MainAdapter(List<MainItem> data, AdapterCallback callback) {
        this.data = data;
        this.callback = callback;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent,false);
        return new RecyclerViewHolder(view);
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        final RelativeLayout menuContainer;
        final TextView menuItem;
        final ImageView menuIcon;

        public RecyclerViewHolder(View view) {
            super(view);
            menuContainer = view.findViewById(R.id.item_main_rl);
            menuItem = view.findViewById(R.id.item_main_content_tv);
            menuIcon = view.findViewById(R.id.item_main_icon_iv);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, final int position) {
        MainItem dataProvider = data.get(position);

        holder.menuItem.setText(dataProvider.getText());
        holder.menuIcon.setImageResource(dataProvider.getIcon());
        holder.menuContainer.setOnClickListener(v -> {
            if (callback != null) {
                try {
                    callback.onItemClicked(position, false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        holder.menuContainer.setOnLongClickListener(v -> {
            if (callback != null) {
                try {
                    callback.onItemClicked(position, true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}

