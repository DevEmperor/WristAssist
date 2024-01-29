package net.devemperor.wristassist.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.database.ChatHistoryDatabaseHelper;
import net.devemperor.wristassist.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EditChatActivity extends Activity {

    TextView titleTv;
    TextView modifiedTv;
    TextView chatCostTv;
    ImageButton editTitleBtn;
    ImageButton deleteChatBtn;

    ChatHistoryDatabaseHelper chatHistoryDatabaseHelper;
    long id;
    DecimalFormat df = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_chat);

        titleTv = findViewById(R.id.edit_title_tv);
        modifiedTv = findViewById(R.id.modified_tv);
        chatCostTv = findViewById(R.id.chat_cost_tv);
        editTitleBtn = findViewById(R.id.edit_btn);
        deleteChatBtn = findViewById(R.id.delete_btn);

        chatHistoryDatabaseHelper = new ChatHistoryDatabaseHelper(this);
        id = getIntent().getLongExtra("net.devemperor.wristassist.chatId", -1);

        titleTv.setText(chatHistoryDatabaseHelper.getTitle(id));
        titleTv.setTextSize(16 * Util.getFontMultiplier(this));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss", Locale.getDefault());
        modifiedTv.setText(formatter.format(chatHistoryDatabaseHelper.getModified(id)));
        modifiedTv.setTextSize(14 * Util.getFontMultiplier(this));

        try {
            chatCostTv.setText(getString(R.string.wristassist_chat_cost, df.format(chatHistoryDatabaseHelper.getChatCost(this, id) / 1000.0)));
            chatCostTv.setTextSize(14 * Util.getFontMultiplier(this));
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;
        if (requestCode == 1337) {
            String content = data.getStringExtra("net.devemperor.wristassist.input.content");
            chatHistoryDatabaseHelper.setTitle(id, content);
            titleTv.setText(content);
        }
    }

    public void editTitle(View view) {
        Intent intent = new Intent(this, InputActivity.class);
        intent.putExtra("net.devemperor.wristassist.input.title", getString(R.string.wristassist_edit_chat_title));
        intent.putExtra("net.devemperor.wristassist.input.content", titleTv.getText().toString());
        startActivityForResult(intent, 1337);
    }

    public void deleteChat(View view) {
        chatHistoryDatabaseHelper.delete(this, id);
        Toast.makeText(this, R.string.wristassist_chat_deleted, Toast.LENGTH_SHORT).show();
        finish();
    }
}
