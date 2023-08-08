package net.devemperor.wristassist.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.LeadingMarginSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.items.ChatItem;
import net.devemperor.wristassist.util.Util;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

public class ChatAdapter extends ArrayAdapter<ChatItem> {
    final Context context;
    final List<ChatItem> objects;

    DecimalFormat df = new DecimalFormat("#.#");
    boolean showSystemMessage = false;

    public ChatAdapter(@NonNull Context context, @NonNull List<ChatItem> objects) {
        super(context, -1, objects);
        this.context = context;
        this.objects = objects;
    }

    @NonNull
    @SuppressLint("SetTextI18n")
    @Override
    public View getView (int position, View convertView, @NonNull ViewGroup parent) {
        View listItem = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);

        TextView chatItem = listItem.findViewById(R.id.chat_item_text);
        chatItem.setTextSize(context.getSharedPreferences("net.devemperor.wristassist", Context.MODE_PRIVATE).getInt("net.devemperor.wristassist.font_size", 15));

        Drawable icon;
        ChatMessage chatMessage = objects.get(position).getChatMessage();
        if (chatMessage.getRole().equals(ChatMessageRole.USER.value())) {
            icon = ContextCompat.getDrawable(context, R.drawable.twotone_person_24);
            chatItem.setText(chatMessage.getContent());
        } else if (chatMessage.getRole().equals(ChatMessageRole.ASSISTANT.value())) {
            icon = ContextCompat.getDrawable(context, R.drawable.wristassist_logo);
            chatItem.setText(chatMessage.getContent());
        } else {
            icon = ContextCompat.getDrawable(context, R.drawable.twotone_lock_24);
            chatItem.setText(R.string.wristassist_click_to_reveal);
            chatItem.setTypeface(chatItem.getTypeface(), Typeface.ITALIC);

            chatItem.setOnClickListener(v -> {
                showSystemMessage = !showSystemMessage;
                if (showSystemMessage) {
                    chatItem.setText(chatMessage.getContent());
                } else {
                    chatItem.setText(R.string.wristassist_click_to_reveal);
                }
                assert icon != null;
                setLeadingMarginSpan(chatItem, icon);
            });
        }
        assert icon != null;
        setLeadingMarginSpan(chatItem, icon);

        long totalCost = objects.get(position).getTotalCost();
        if (totalCost > 0 && context.getSharedPreferences("net.devemperor.wristassist", Context.MODE_PRIVATE)
                .getBoolean("net.devemperor.wristassist.show_cost", false)) {
            TextView chatItemCost = listItem.findViewById(R.id.chat_item_cost);
            chatItemCost.setText(df.format(totalCost / 1000.0) + " k");
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

    private void setLeadingMarginSpan(TextView textView, Drawable drawable) {
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        BitmapDrawable bitmapDrawable = new BitmapDrawable(textView.getResources(), Util.drawableToBitmap(drawable));
        bitmapDrawable.setBounds(0, 0, drawableWidth, drawableHeight);

        SpannableString spannableString = new SpannableString("   " + textView.getText());
        LeadingMarginSpan leadingMarginSpan = new LeadingMarginSpan.Standard(0, 0);
        spannableString.setSpan(leadingMarginSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ImageSpan imageSpan = new ImageSpan(bitmapDrawable, ImageSpan.ALIGN_BASELINE);
        spannableString.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);
    }
}
