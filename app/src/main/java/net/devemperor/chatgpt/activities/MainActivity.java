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
import net.devemperor.chatgpt.adapters.MainItem;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends Activity {

    WearableRecyclerView mainWrv;

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
        menuItems.add(new MainItem(R.drawable.twotone_settings_24, getString(R.string.chatgpt_menu_settings)));
        menuItems.add(new MainItem(R.drawable.twotone_info_24, getString(R.string.chatgpt_menu_about)));

        mainWrv.setAdapter(new MainAdapter(menuItems, menuPosition -> {
            Intent intent;
            if (menuPosition == 0) {
                RemoteInput remoteInput = new RemoteInput.Builder("query").setLabel(getString(R.string.chatgpt_query)).build();
                intent = RemoteInputIntentHelper.createActionRemoteInputIntent();
                RemoteInputIntentHelper.putRemoteInputsExtra(intent, Collections.singletonList(remoteInput));
                startActivityForResult(intent, 1337);
            } else if (menuPosition == 1) {
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            } else if (menuPosition == 2) {
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
            }
        }));
        mainWrv.requestFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1337 && resultCode == RESULT_OK) {
            Bundle results = RemoteInput.getResultsFromIntent(data);
            if (results != null) {
                String query = results.getCharSequence("query").toString();
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("net.devemperor.chatgpt.query", query);
                startActivity(intent);
            }
        }
    }
}