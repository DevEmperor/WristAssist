package net.devemperor.wristassist.activities;

import android.app.Activity;
import android.app.RemoteInput;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.wear.input.RemoteInputIntentHelper;
import androidx.wear.widget.ConfirmationOverlay;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.database.DatabaseHelper;

import org.json.JSONException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Locale;

public class EditChatActivity extends Activity {

    TextView titleTv;
    TextView modifiedTv;
    TextView chatCostTv;
    ImageButton editTitleBtn;
    ImageButton deleteChatBtn;

    DatabaseHelper databaseHelper;
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

        databaseHelper = new DatabaseHelper(this);
        id = getIntent().getLongExtra("net.devemperor.wristassist.chatId", -1);

        titleTv.setText(databaseHelper.getTitle(id));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        modifiedTv.setText(getString(R.string.wristassist_last_edit, formatter.format(databaseHelper.getModified(id))));

        try {
            chatCostTv.setText(getString(R.string.wristassist_chat_cost, df.format(databaseHelper.getChatCost(this, id) / 1000.0)));
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
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
        RemoteInput remoteInput = new RemoteInput.Builder("new_title").setLabel(getString(R.string.wristassist_ask_title)).build();
        Intent intent = RemoteInputIntentHelper.createActionRemoteInputIntent();
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, Collections.singletonList(remoteInput));
        startActivityForResult(intent, 1337);
    }

    public void deleteChat(View view) {
        databaseHelper.delete(this, id);
        new ConfirmationOverlay().setOnAnimationFinishedListener(this::finish).showOn(this);
    }
}
