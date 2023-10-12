package net.devemperor.wristassist.activities;

import android.app.Activity;
import android.app.RemoteInput;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.splashscreen.SplashScreen;
import androidx.wear.input.RemoteInputIntentHelper;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.adapters.MainAdapter;
import net.devemperor.wristassist.items.MainItem;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends Activity {

    WearableRecyclerView mainWrv;
    Bundle systemQueryBundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        if (!getSharedPreferences("net.devemperor.wristassist", MODE_PRIVATE).getBoolean("net.devemperor.wristassist.onboarding_complete", false)) {
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
                openKeyboard(1337);
            } else if (menuPosition == 0) {
                openKeyboard(1338);
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

    private void openKeyboard(int returnCode) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            intent = new Intent("com.google.android.wearable.action.LAUNCH_KEYBOARD");
        } else {
            intent = RemoteInputIntentHelper.createActionRemoteInputIntent();
            RemoteInput remoteInput = new RemoteInput.Builder("result_text").build();
            RemoteInputIntentHelper.putRemoteInputsExtra(intent, Collections.singletonList(remoteInput));
        }
        startActivityForResult(intent, returnCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1337 && resultCode == RESULT_OK) {
            startChatActivity(data, null);
        }
        if (requestCode == 1338 && resultCode == RESULT_OK) {
            CharSequence result_text;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                result_text = data.getStringExtra("result_text");
            } else {
                result_text = RemoteInput.getResultsFromIntent(data).getCharSequence("result_text");
            }
            if (result_text != null) {
                systemQueryBundle.putString("net.devemperor.wristassist.system_query", result_text.toString());

                new Handler().postDelayed(() -> openKeyboard(1339), 100);
            }
        }
        if (requestCode == 1339 && resultCode == RESULT_OK) {
            startChatActivity(data, systemQueryBundle.getString("net.devemperor.wristassist.system_query"));
        }
    }

    private void startChatActivity(Intent data, String systemQuery) {
        CharSequence query;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            query = data.getStringExtra("result_text");
        } else {
            query = RemoteInput.getResultsFromIntent(data).getCharSequence("result_text");
        }
        if (query != null) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("net.devemperor.wristassist.query", query.toString());
            intent.putExtra("net.devemperor.wristassist.system_query", systemQuery);
            startActivity(intent);
        }
    }
}