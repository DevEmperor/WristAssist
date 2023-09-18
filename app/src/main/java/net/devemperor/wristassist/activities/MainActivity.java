package net.devemperor.wristassist.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.splashscreen.SplashScreen;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.adapters.MainAdapter;
import net.devemperor.wristassist.items.MainItem;

import java.util.ArrayList;

public class MainActivity extends Activity {

    WearableRecyclerView mainWrv;
    Bundle systemQueryBundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!getSharedPreferences("net.devemperor.wristassist", MODE_PRIVATE).getBoolean("net.devemperor.wristassist.onboarding_complete", false)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
        }

        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_main);

        mainWrv = findViewById(R.id.main_wrv);
        mainWrv.setHasFixedSize(true);
        mainWrv.setEdgeItemsCenteringEnabled(true);
        mainWrv.setLayoutManager(new WearableLinearLayoutManager(this));

        ArrayList<MainItem> menuItems = new ArrayList<>();
        menuItems.add(new MainItem(R.drawable.twotone_add_24, getString(R.string.wristassist_menu_new_chat)));
        menuItems.add(new MainItem(R.drawable.twotone_chat_24, getString(R.string.wristassist_menu_saved_chats)));
        menuItems.add(new MainItem(R.drawable.twotone_settings_24, getString(R.string.wristassist_menu_settings)));
        menuItems.add(new MainItem(R.drawable.twotone_info_24, getString(R.string.wristassist_menu_about)));

        mainWrv.setAdapter(new MainAdapter(menuItems, (menuPosition, longClick) -> {
            Intent intent;
            if (menuPosition == 0 && !longClick) {
                startActivityForResult(new Intent("com.google.android.wearable.action.LAUNCH_KEYBOARD"), 1337);
            } else if (menuPosition == 0) {
                startActivityForResult(new Intent("com.google.android.wearable.action.LAUNCH_KEYBOARD"), 1338);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1337 && resultCode == RESULT_OK) {
            startChatActivity(data.getStringExtra("result_text"), null);
        }
        if (requestCode == 1338 && resultCode == RESULT_OK) {
            systemQueryBundle.putString("net.devemperor.wristassist.system_query", data.getStringExtra("result_text"));

            new Handler().postDelayed(() -> startActivityForResult(new Intent("com.google.android.wearable.action.LAUNCH_KEYBOARD"), 1339), 100);
        }
        if (requestCode == 1339 && resultCode == RESULT_OK) {
            startChatActivity(data.getStringExtra("result_text"), systemQueryBundle.getString("net.devemperor.wristassist.system_query"));
        }
    }

    private void startChatActivity(String query, String systemQuery) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("net.devemperor.wristassist.query", query);
        intent.putExtra("net.devemperor.wristassist.system_query", systemQuery);
        startActivity(intent);
    }
}