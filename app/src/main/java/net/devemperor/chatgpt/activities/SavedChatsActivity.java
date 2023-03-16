package net.devemperor.chatgpt.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.core.view.InputDeviceCompat;
import androidx.core.view.MotionEventCompat;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import net.devemperor.chatgpt.R;
import net.devemperor.chatgpt.adapters.SavedChatsAdapter;
import net.devemperor.chatgpt.database.ChatHistoryModel;
import net.devemperor.chatgpt.database.DatabaseHelper;

import java.util.List;

public class SavedChatsActivity extends Activity {

    WearableRecyclerView savedChatsWrv;

    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_chats);

        savedChatsWrv = findViewById(R.id.saved_chats_wrv);
        savedChatsWrv.setHasFixedSize(true);
        savedChatsWrv.setEdgeItemsCenteringEnabled(true);
        savedChatsWrv.setLayoutManager(new WearableLinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);
        List<ChatHistoryModel> chats = databaseHelper.getAllChats();

        savedChatsWrv.setAdapter(new SavedChatsAdapter(chats, chatPosition -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("net.devemperor.chatgpt.chatId", chats.get(chatPosition).getId());
            startActivity(intent);
        }));

        findViewById(R.id.no_saved_chats).setVisibility(chats.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);

        savedChatsWrv.requestFocus();
        savedChatsWrv.setOnGenericMotionListener((v, ev) -> {
            if (ev.getAction() == MotionEvent.ACTION_SCROLL && ev.isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER) && !chats.isEmpty()) {
                v.scrollBy(0, (int) (savedChatsWrv.getChildAt(0).getHeight() * -ev.getAxisValue(MotionEventCompat.AXIS_SCROLL)));
                return true;
            }
            return false;
        });
    }
}
