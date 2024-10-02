package net.devemperor.wristassist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.InputDeviceCompat;
import androidx.core.view.MotionEventCompat;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.adapters.SavedChatsAdapter;
import net.devemperor.wristassist.database.ChatHistoryDatabaseHelper;
import net.devemperor.wristassist.database.ChatHistoryModel;

import java.util.List;

public class SavedChatsActivity extends AppCompatActivity {

    WearableRecyclerView savedChatsWrv;

    ChatHistoryDatabaseHelper chatHistoryDatabaseHelper;
    SavedChatsAdapter savedChatsAdapter;

    int currentEditPosition = -1;
    ActivityResultLauncher<Intent> editChatLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_chats);

        savedChatsWrv = findViewById(R.id.activity_saved_chats_wrv);
        savedChatsWrv.setHasFixedSize(true);
        savedChatsWrv.setEdgeItemsCenteringEnabled(true);
        savedChatsWrv.setLayoutManager(new WearableLinearLayoutManager(this));

        chatHistoryDatabaseHelper = new ChatHistoryDatabaseHelper(this);
        List<ChatHistoryModel> chats = chatHistoryDatabaseHelper.getAllChats();

        TextView noSavedChats = findViewById(R.id.activity_saved_chats_no_saved_chats_tv);
        noSavedChats.setVisibility(chats.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);

        editChatLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null
                    && result.getData().getBooleanExtra("net.devemperor.wristassist.chat_deleted", false)) {
                savedChatsAdapter.getData().remove(currentEditPosition);
                savedChatsAdapter.notifyItemRemoved(currentEditPosition);
                noSavedChats.setVisibility(savedChatsAdapter.getData().isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
            }
        });

        savedChatsAdapter = new SavedChatsAdapter(chats, (chatPosition, longClick) -> {
            currentEditPosition = chatPosition;
            Intent intent;
            if (!longClick) {
                intent = new Intent(this, ChatActivity.class);
            } else {
                intent = new Intent(this, EditChatActivity.class);
            }
            intent.putExtra("net.devemperor.wristassist.chatId", chats.get(chatPosition).getId());
            editChatLauncher.launch(intent);
        });
        savedChatsWrv.setAdapter(savedChatsAdapter);

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
