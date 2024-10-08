package net.devemperor.wristassist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.database.ChatHistoryDatabaseHelper;
import net.devemperor.wristassist.util.InputIntentBuilder;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EditChatActivity extends AppCompatActivity {

    ScrollView editChatSv;
    TextView titleTv;
    TextView modifiedTv;
    TextView chatCostTv;
    ImageButton editTitleBtn;
    ImageButton deleteChatBtn;

    ChatHistoryDatabaseHelper chatHistoryDatabaseHelper;
    long id;
    ActivityResultLauncher<Intent> editTitleLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_chat);

        editChatSv = findViewById(R.id.activity_edit_chat_sv);
        titleTv = findViewById(R.id.activity_edit_chat_title_tv);
        modifiedTv = findViewById(R.id.activity_edit_chat_modified_tv);
        chatCostTv = findViewById(R.id.activity_edit_chat_cost_tv);
        editTitleBtn = findViewById(R.id.activity_edit_chat_edit_btn);
        deleteChatBtn = findViewById(R.id.activity_edit_chat_delete_btn);

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

        editTitleLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                String content = result.getData().getStringExtra("net.devemperor.wristassist.input.content");
                chatHistoryDatabaseHelper.setTitle(id, content);
                titleTv.setText(content);
            }
        });

        editChatSv.requestFocus();
    }

    public void editTitle(View view) {
        Intent intent = new InputIntentBuilder(this)
            .setTitle(getString(R.string.wristassist_edit_chat_title))
            .setContent(titleTv.getText().toString())
            .build();
        editTitleLauncher.launch(intent);
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
