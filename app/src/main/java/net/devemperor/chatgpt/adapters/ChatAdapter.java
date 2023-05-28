package net.devemperor.chatgpt.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import net.devemperor.chatgpt.R;
import net.devemperor.chatgpt.items.ChatItem;
import net.devemperor.chatgpt.util.Util;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

public class ChatAdapter extends ArrayAdapter<ChatItem> {
    final Context context;
    final List<ChatItem> objects;

    DecimalFormat df = new DecimalFormat("#.#####");

    public ChatAdapter(@NonNull Context context, @NonNull List<ChatItem> objects) {
        super(context, -1, objects);
        this.context = context;
        this.objects = objects;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        View listItem = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);

        TextView chatItem = listItem.findViewById(R.id.chat_item_text);
        chatItem.setText(objects.get(position).getChatMessage().getContent());

        ImageView chatItemIcon = listItem.findViewById(R.id.chat_item_icon);
        if (objects.get(position).getChatMessage().getRole().equals(ChatMessageRole.USER.value())) {
            chatItemIcon.setImageResource(R.drawable.twotone_person_24);
        } else {
            chatItemIcon.setImageResource(R.drawable.chatgpt_logo);
        }

        long totalCost = objects.get(position).getTotalCost();
        if (totalCost > 0) {
            TextView chatItemCost = listItem.findViewById(R.id.chat_item_cost);
            chatItemCost.setText(df.format(Util.getFiatPrice(totalCost)) + " $");
            chatItemCost.setVisibility(View.VISIBLE);
        }
        return listItem;
    }

    public void add(ChatItem newItem) {
        objects.add(newItem);
        notifyDataSetChanged();
    }

    public List<ChatItem> getChatItems() {
        return objects;
    }

    public List<ChatMessage> getChatMessages() {
        return objects.stream().map(ChatItem::getChatMessage).collect(Collectors.toList());
    }

    public int getCount() {
        return objects.size();
    }
}
