package net.devemperor.chatgpt.activities;

import android.app.Activity;
import android.app.RemoteInput;
import android.content.Intent;
import android.os.Bundle;

import androidx.wear.input.RemoteInputIntentHelper;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import net.devemperor.chatgpt.R;
import net.devemperor.chatgpt.adapters.MainAdapter;
import net.devemperor.chatgpt.items.MainItem;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends Activity {

    WearableRecyclerView mainWrv;
    Bundle systemQueryBundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainWrv = findViewById(R.id.main_wrv);
        mainWrv.setHasFixedSize(true);
        mainWrv.setEdgeItemsCenteringEnabled(true);
        mainWrv.setLayoutManager(new WearableLinearLayoutManager(this));

        ArrayList<MainItem> menuItems = new ArrayList<>();
        menuItems.add(new MainItem(R.drawable.twotone_add_24, getString(R.string.chatgpt_menu_new_chat)));
        menuItems.add(new MainItem(R.drawable.twotone_chat_24, getString(R.string.chatgpt_menu_saved_chats)));
        menuItems.add(new MainItem(R.drawable.twotone_settings_24, getString(R.string.chatgpt_menu_settings)));
        menuItems.add(new MainItem(R.drawable.twotone_info_24, getString(R.string.chatgpt_menu_about)));

        mainWrv.setAdapter(new MainAdapter(menuItems, (menuPosition, longClick) -> {
            Intent intent;
            if (menuPosition == 0 && !longClick) {
                queryKeyboard(1337, "query", R.string.chatgpt_query);
            } else if (menuPosition == 0) {
                queryKeyboard(1338, "system_query", R.string.chatgpt_system_query);
            } else if (menuPosition == 1) {
                intent = new Intent(this, SavedChatsActivity.class);
                startActivity(intent);
            } else if (menuPosition == 2) {
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            } else if (menuPosition == 3) {
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
            }
        }));
        mainWrv.requestFocus();
        mainWrv.postDelayed(() -> mainWrv.scrollBy(0, mainWrv.getChildAt(0).getHeight()), 100);
    }

    private void queryKeyboard(int requestCode, String resultKey, int labelResId) {
        RemoteInput remoteInput = new RemoteInput.Builder(resultKey).setLabel(getString(labelResId)).build();
        Intent intent = RemoteInputIntentHelper.createActionRemoteInputIntent();
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, Collections.singletonList(remoteInput));
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1337 && resultCode == RESULT_OK) {
            startChatActivity(data, null);
        }
        if (requestCode == 1338 && resultCode == RESULT_OK) {
            Bundle results = RemoteInput.getResultsFromIntent(data);
            if (results != null) {
                String systemQuery = results.getCharSequence("system_query").toString();
                systemQueryBundle.putString("net.devemperor.chatgpt.system_query", systemQuery);

                queryKeyboard(1339, "query", R.string.chatgpt_query);
            }
        }
        if (requestCode == 1339 && resultCode == RESULT_OK) {
            startChatActivity(data, systemQueryBundle.getString("net.devemperor.chatgpt.system_query"));
        }
    }

    private void startChatActivity(Intent data, String systemQuery) {
        Bundle results = RemoteInput.getResultsFromIntent(data);
        if (results != null) {
            String query = results.getCharSequence("query").toString();
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("net.devemperor.chatgpt.query", query);
            intent.putExtra("net.devemperor.chatgpt.system_query", systemQuery);
            startActivity(intent);
        }
    }
}