package net.devemperor.chatgpt.activities;

import android.app.Activity;
import android.app.RemoteInput;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.wear.input.RemoteInputIntentHelper;
import androidx.wear.widget.ConfirmationOverlay;

import net.devemperor.chatgpt.R;
import net.devemperor.chatgpt.database.DatabaseHelper;

import java.util.Collections;

public class EditChatActivity extends Activity {

    TextView titleTv;
    ImageButton editTitleBtn;
    ImageButton deleteChatBtn;

    DatabaseHelper databaseHelper;
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_chat);

        titleTv = findViewById(R.id.edit_title_tv);
        editTitleBtn = findViewById(R.id.edit_btn);
        deleteChatBtn = findViewById(R.id.delete_btn);

        databaseHelper = new DatabaseHelper(this);
        id = getIntent().getLongExtra("net.devemperor.chatgpt.chatId", -1);

        titleTv.setText(databaseHelper.getTitle(id));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1337 && resultCode == RESULT_OK) {
            Bundle results = RemoteInput.getResultsFromIntent(data);
            if (results != null) {
                String newTitle = results.getCharSequence("new_title").toString();
                databaseHelper.setTitle(id, newTitle);
                titleTv.setText(newTitle);
            }
        }
    }

    public void editTitle(View view) {
        RemoteInput remoteInput = new RemoteInput.Builder("new_title").setLabel(getString(R.string.chatgpt_ask_title)).build();
        Intent intent = RemoteInputIntentHelper.createActionRemoteInputIntent();
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, Collections.singletonList(remoteInput));
        startActivityForResult(intent, 1337);
    }

    public void deleteChat(View view) {
        databaseHelper.delete(id);
        new ConfirmationOverlay().setOnAnimationFinishedListener(this::finish).showOn(this);
    }
}
