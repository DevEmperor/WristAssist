package net.devemperor.wristassist.activities;

import android.app.Activity;
import android.app.RemoteInput;
import android.content.Intent;
import android.os.Build;
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
        titleTv.setTextSize(16 * getResources().getConfiguration().fontScale);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        modifiedTv.setText(getString(R.string.wristassist_last_edit, formatter.format(databaseHelper.getModified(id))));
        modifiedTv.setTextSize(14 * getResources().getConfiguration().fontScale);

        try {
            chatCostTv.setText(getString(R.string.wristassist_chat_cost, df.format(databaseHelper.getChatCost(this, id) / 1000.0)));
            chatCostTv.setTextSize(14 * getResources().getConfiguration().fontScale);
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CharSequence result_text;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            result_text = data.getStringExtra("result_text");
        } else {
            result_text = RemoteInput.getResultsFromIntent(data).getCharSequence("result_text");
        }
        if (requestCode == 1337 && resultCode == RESULT_OK && result_text != null) {
            databaseHelper.setTitle(id, result_text.toString());
            titleTv.setText(result_text);
        }
    }

    public void editTitle(View view) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            intent = new Intent("com.google.android.wearable.action.LAUNCH_KEYBOARD");
        } else {
            intent = RemoteInputIntentHelper.createActionRemoteInputIntent();
            RemoteInput remoteInput = new RemoteInput.Builder("result_text").build();
            RemoteInputIntentHelper.putRemoteInputsExtra(intent, Collections.singletonList(remoteInput));
        }
        startActivityForResult(intent, 1337);
    }

    public void deleteChat(View view) {
        databaseHelper.delete(this, id);
        new ConfirmationOverlay().setOnAnimationFinishedListener(this::finish).showOn(this);
    }
}
