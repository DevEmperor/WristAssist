package net.devemperor.wristassist.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.items.ChatItem;
import net.devemperor.wristassist.util.WristAssistUtil;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.AsyncDrawableSpan;

public class ChatAdapter extends ArrayAdapter<ChatItem> {
    final Context context;
    final List<ChatItem> objects;

    TextToSpeech tts;
    LanguageIdentifier langId;
    boolean showSystemMessage = false;
    boolean ttsEnabled = false;
    String lastText = "";
    Markwon markwon;
    SharedPreferences sp;

    public ChatAdapter(@NonNull Context context, @NonNull List<ChatItem> objects) {
        super(context, -1, objects);
        this.context = context;
        this.objects = objects;

        sp = context.getSharedPreferences("net.devemperor.wristassist", Context.MODE_PRIVATE);
        markwon = Markwon.builder(context)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(HtmlPlugin.create())
                .usePlugin(TablePlugin.create(context))
                .usePlugin(TaskListPlugin.create(context))
                .build();

        if (!sp.getString("net.devemperor.wristassist.tts", "off").equals("off")) {
            tts = new TextToSpeech(context, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    ttsEnabled = true;
                } else {
                    Toast.makeText(context, R.string.wristassist_tts_not_available, Toast.LENGTH_SHORT).show();
                }
            });
            langId = LanguageIdentification.getClient();
        }
    }

    public void shutdownServices() {
        if (tts == null || langId == null) return;
        tts.shutdown();
        langId.close();
    }

    @NonNull
    @SuppressLint("SetTextI18n")
    @Override
    public View getView (int position, View convertView, @NonNull ViewGroup parent) {
        View listItem = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);

        TextView chatItem = listItem.findViewById(R.id.item_chat_content_tv);
        chatItem.setTextSize(sp.getInt("net.devemperor.wristassist.font_size", 15));

        ChatMessage chatMessage = objects.get(position).getChatMessage();
        chatItem.setOnClickListener(v -> launchTTS(chatMessage.getContent()));

        Drawable icon;
        chatItem.setText(chatMessage.getContent());
        if (chatMessage.getRole().equals(ChatMessageRole.USER.value())) {
            icon = ContextCompat.getDrawable(context, R.drawable.twotone_person_24);
        } else if (chatMessage.getRole().equals(ChatMessageRole.ASSISTANT.value())) {
            icon = ContextCompat.getDrawable(context, R.drawable.twotone_auto_awesome_24);
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

        return listItem;
    }

    public void launchTTS(String text) {
        if (!ttsEnabled || langId == null) {
            return;
        }

        if (tts.isSpeaking()) {
            tts.stop();
            if (lastText.equals(text)) return;
        }

        lastText = text;
        Bundle params = new Bundle();
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, sp.getInt("net.devemperor.wristassist.tts_volume", 5) / 10f);
        langId.identifyLanguage(text).addOnSuccessListener(languageCode -> {
            if (tts.isLanguageAvailable(Locale.forLanguageTag(languageCode)) < TextToSpeech.LANG_AVAILABLE) {
                Toast.makeText(context, R.string.wristassist_tts_lang_not_available, Toast.LENGTH_SHORT).show();
                return;
            }
            tts.setLanguage(Locale.forLanguageTag(languageCode));
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, null);
        }).addOnFailureListener(e -> {
            tts.setLanguage(Locale.ENGLISH);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, null);
        });
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
        final Spanned spanned = markwon.toMarkdown(textView.getText().toString());

        BitmapDrawable bitmapDrawable = new BitmapDrawable(textView.getResources(), WristAssistUtil.drawableToBitmap(drawable));
        bitmapDrawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * 1.1), (int) (drawable.getIntrinsicHeight() * 1.1));

        ImageSpan imageSpan = new ImageSpan(bitmapDrawable, AsyncDrawableSpan.ALIGN_BOTTOM);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(" ");
        builder.setSpan(imageSpan, 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(" ").append(spanned);

        textView.setText(builder);
    }
}
