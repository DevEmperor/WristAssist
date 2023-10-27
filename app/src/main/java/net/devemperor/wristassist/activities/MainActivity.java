package net.devemperor.wristassist.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.splashscreen.SplashScreen;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.adapters.MainAdapter;
import net.devemperor.wristassist.items.MainItem;

import java.util.ArrayList;

public class MainActivity extends Activity {

    WearableRecyclerView mainWrv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        if (getIntent().getBooleanExtra("net.devemperor.wristassist.enter_api_key", false)) {
            Intent intent = new Intent(this, InputActivity.class);
            intent.putExtra("net.devemperor.wristassist.input.title", getString(R.string.wristassist_set_api_key));
            intent.putExtra("net.devemperor.wristassist.input.hint", getString(R.string.wristassist_api_key));
            startActivityForResult(intent, 1340);
        }

        if (!getSharedPreferences("net.devemperor.wristassist", MODE_PRIVATE).getBoolean("net.devemperor.wristassist.onboarding_complete", false)
            && !getIntent().getBooleanExtra("net.devemperor.wristassist.enter_api_key", false)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
        }

        super.onCreate(savedInstanceState);
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
                intent = new Intent(this, InputActivity.class);
                intent.putExtra("net.devemperor.wristassist.input.title", getString(R.string.wristassist_enter_prompt));
                intent.putExtra("net.devemperor.wristassist.input.hint", getString(R.string.wristassist_prompt));
                startActivityForResult(intent, 1337);
            } else if (menuPosition == 0) {
                intent = new Intent(this, InputActivity.class);
                intent.putExtra("net.devemperor.wristassist.input.title", getString(R.string.wristassist_enter_system_prompt));
                intent.putExtra("net.devemperor.wristassist.input.hint", getString(R.string.wristassist_system_prompt));
                intent.putExtra("net.devemperor.wristassist.input.title2", getString(R.string.wristassist_enter_prompt));
                intent.putExtra("net.devemperor.wristassist.input.hint2", getString(R.string.wristassist_prompt));
                startActivityForResult(intent, 1338);
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
        if (resultCode != RESULT_OK) return;

        Intent intent;
        if (requestCode == 1337) {
            intent = new Intent(this, ChatActivity.class);
            intent.putExtra("net.devemperor.wristassist.query", data.getStringExtra("net.devemperor.wristassist.input.content"));
            startActivity(intent);
        }
        if (requestCode == 1338) {
            intent = new Intent(this, ChatActivity.class);
            intent.putExtra("net.devemperor.wristassist.query", data.getStringExtra("net.devemperor.wristassist.input.content2"));
            intent.putExtra("net.devemperor.wristassist.system_query", data.getStringExtra("net.devemperor.wristassist.input.content"));
            startActivity(intent);
        }
        if (requestCode == 1340) {
            getSharedPreferences("net.devemperor.wristassist", MODE_PRIVATE)
                    .edit().putString("net.devemperor.wristassist.api_key", data.getStringExtra("net.devemperor.wristassist.input.content")).apply();
            getSharedPreferences("net.devemperor.wristassist", Activity.MODE_PRIVATE)
                    .edit().putBoolean("net.devemperor.wristassist.onboarding_complete", true).apply();
        }
    }
}