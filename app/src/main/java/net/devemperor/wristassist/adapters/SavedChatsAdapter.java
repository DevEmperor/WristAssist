package net.devemperor.wristassist.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.database.ChatHistoryModel;

import java.util.List;

public class SavedChatsAdapter extends RecyclerView.Adapter<SavedChatsAdapter.RecyclerViewHolder> {

    private final List<ChatHistoryModel> data;
    private final AdapterCallback callback;

    public interface AdapterCallback {
        void onItemClicked(Integer menuPosition, boolean longClick);
    }

    public SavedChatsAdapter(List<ChatHistoryModel> data, AdapterCallback callback) {
        this.data = data;
        this.callback = callback;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_chat, parent,false);
        return new RecyclerViewHolder(view);
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout savedChatContainer;
        final TextView savedChatTitle;

        public RecyclerViewHolder(View view) {
            super(view);
            savedChatContainer = view.findViewById(R.id.item_saved_chat_ll);
            savedChatTitle = view.findViewById(R.id.item_saved_chat_title_tv);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, final int position) {
        ChatHistoryModel dataProvider = data.get(position);

        holder.savedChatTitle.setText(dataProvider.getTitle());

        holder.savedChatContainer.setOnClickListener(v -> {
            if (callback != null) {
                callback.onItemClicked(holder.getBindingAdapterPosition(), false);
            }
        });
        holder.savedChatContainer.setOnLongClickListener(v -> {
            if (callback != null) {
                callback.onItemClicked(holder.getBindingAdapterPosition(), true);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public List<ChatHistoryModel> getData() {
        return data;
    }
}