package net.devemperor.wristassist.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.database.ChatHistoryDatabaseHelper;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EditChatActivity extends Activity {

    ScrollView editChatSv;
    TextView titleTv;
    TextView modifiedTv;
    TextView chatCostTv;
    ImageButton editTitleBtn;
    ImageButton deleteChatBtn;

    ChatHistoryDatabaseHelper chatHistoryDatabaseHelper;
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_chat);

        editChatSv = findViewById(R.id.edit_chat_sv);
        titleTv = findViewById(R.id.edit_title_tv);
        modifiedTv = findViewById(R.id.modified_tv);
        chatCostTv = findViewById(R.id.chat_cost_tv);
        editTitleBtn = findViewById(R.id.edit_btn);
        deleteChatBtn = findViewById(R.id.delete_btn);

        chatHistoryDatabaseHelper = new ChatHistoryDatabaseHelper(this);
        id = getIntent().getLongExtra("net.devemperor.wristassist.chatId", -1);

        titleTv.setText(chatHistoryDatabaseHelper.getTitle(id));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss", Locale.getDefault());
        modifiedTv.setText(formatter.format(chatHistoryDatabaseHelper.getModified(id)));

        try {
            chatCostTv.setText(String.format(Locale.getDefault(), "%,d", chatHistoryDatabaseHelper.getChatCost(this, id)));
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }

        editChatSv.requestFocus();
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
        Intent data = new Intent();
        data.putExtra("net.devemperor.wristassist.chat_deleted", true);
        setResult(RESULT_OK, data);
        finish();
    }
}
