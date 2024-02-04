package net.devemperor.wristassist.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.core.view.InputDeviceCompat;
import androidx.core.view.MotionEventCompat;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.adapters.SavedChatsAdapter;
import net.devemperor.wristassist.database.ChatHistoryDatabaseHelper;
import net.devemperor.wristassist.database.ChatHistoryModel;
import net.devemperor.wristassist.util.Util;

import java.util.List;

public class SavedChatsActivity extends Activity {

    WearableRecyclerView savedChatsWrv;

    ChatHistoryDatabaseHelper chatHistoryDatabaseHelper;
    SavedChatsAdapter savedChatsAdapter;

    int currentEditPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_chats);

        savedChatsWrv = findViewById(R.id.saved_chats_wrv);
        savedChatsWrv.setHasFixedSize(true);
        savedChatsWrv.setEdgeItemsCenteringEnabled(true);
        savedChatsWrv.setLayoutManager(new WearableLinearLayoutManager(this));

        chatHistoryDatabaseHelper = new ChatHistoryDatabaseHelper(this);
        List<ChatHistoryModel> chats = chatHistoryDatabaseHelper.getAllChats();

        savedChatsAdapter = new SavedChatsAdapter(chats, (chatPosition, longClick) -> {
            currentEditPosition = chatPosition;
            Intent intent;
            if (!longClick) {
                intent = new Intent(this, ChatActivity.class);
            } else {
                intent = new Intent(this, EditChatActivity.class);
            }
            intent.putExtra("net.devemperor.wristassist.chatId", chats.get(chatPosition).getId());
            startActivity(intent);
        });
        savedChatsWrv.setAdapter(savedChatsAdapter);

        TextView noSavedChats = findViewById(R.id.no_saved_chats);
        noSavedChats.setVisibility(chats.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        noSavedChats.setTextSize(16 * Util.getFontMultiplier(this));

        savedChatsWrv.requestFocus();
        savedChatsWrv.setOnGenericMotionListener((v, ev) -> {
            if (ev.getAction() == MotionEvent.ACTION_SCROLL && ev.isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER) && !chats.isEmpty()) {
                v.scrollBy(0, (int) (savedChatsWrv.getChildAt(0).getHeight() * -ev.getAxisValue(MotionEventCompat.AXIS_SCROLL)));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (savedChatsAdapter.getItemCount() != chatHistoryDatabaseHelper.getCount()) {
            savedChatsAdapter.getData().remove(currentEditPosition);
            savedChatsAdapter.notifyItemRemoved(currentEditPosition);
            findViewById(R.id.no_saved_chats).setVisibility(savedChatsAdapter.getData().isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        }
    }
}
