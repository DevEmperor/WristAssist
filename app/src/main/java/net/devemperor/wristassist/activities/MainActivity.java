package net.devemperor.wristassist.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import net.devemperor.wristassist.BuildConfig;
import net.devemperor.wristassist.R;
import net.devemperor.wristassist.adapters.MainAdapter;
import net.devemperor.wristassist.items.MainItem;
import net.devemperor.wristassist.util.InputIntentBuilder;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    WearableRecyclerView mainWrv;
    ProgressBar mainPb;
    SharedPreferences sp;

    ActivityResultLauncher<Intent> inputLauncher;
    ActivityResultLauncher<Intent> inputWithSystemMessageLauncher;
    ActivityResultLauncher<Intent> editApiKeyLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        sp = getSharedPreferences("net.devemperor.wristassist", MODE_PRIVATE);
        if (sp.getString("net.devemperor.wristassist.userid", null) == null) {
            Random random = new Random();
            sp.edit().putString("net.devemperor.wristassist.userid", String.valueOf(random.nextInt(999999999 - 100000000) + 100000000)).apply();
        }
        FirebaseCrashlytics.getInstance().setUserId(sp.getString("net.devemperor.wristassist.userid", "null"));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainWrv = findViewById(R.id.activity_main_wrv);
        mainPb = findViewById(R.id.activity_main_pb);

        mainWrv.setHasFixedSize(true);
        mainWrv.setEdgeItemsCenteringEnabled(true);
        mainWrv.setLayoutManager(new WearableLinearLayoutManager(this));

        ArrayList<MainItem> menuItems = new ArrayList<>();
        menuItems.add(new MainItem(R.drawable.twotone_add_24, getString(R.string.wristassist_menu_new_chat)));
        menuItems.add(new MainItem(R.drawable.twotone_chat_24, getString(R.string.wristassist_menu_saved_chats)));
        menuItems.add(new MainItem(R.drawable.twotone_add_photo_alternate_24, getString(R.string.wristassist_menu_images)));
        menuItems.add(new MainItem(R.drawable.twotone_insert_chart_outlined_24, getString(R.string.wristassist_menu_usage)));
        menuItems.add(new MainItem(R.drawable.twotone_settings_24, getString(R.string.wristassist_menu_settings)));
        menuItems.add(new MainItem(R.drawable.twotone_info_24, getString(R.string.wristassist_menu_about)));

        mainWrv.setAdapter(new MainAdapter(menuItems, (menuPosition, longClick) -> {
            Intent intent;
            if (menuPosition == 0 && !longClick) {
                input(false);
            } else if (menuPosition == 0) {
                input(true);
            } else if (menuPosition == 1) {
                intent = new Intent(this, SavedChatsActivity.class);
                startActivity(intent);
            } else if (menuPosition == 2) {
                intent = new Intent(this, GalleryActivity.class);
                startActivity(intent);
                mainPb.setVisibility(View.VISIBLE);
            } else if (menuPosition == 3) {
                intent = new Intent(this, UsageActivity.class);
                startActivity(intent);
            } else if (menuPosition == 4) {
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            } else if (menuPosition == 5) {
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
            }
        }));

        inputLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("net.devemperor.wristassist.query", result.getData().getStringExtra("net.devemperor.wristassist.input.content"));
                startActivity(intent);
            }
        });

        inputWithSystemMessageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                String systemQuery = sp.getString("net.devemperor.wristassist.global_system_query", "");
                systemQuery = (systemQuery.isEmpty() ? "" : systemQuery + "\n") + result.getData().getStringExtra("net.devemperor.wristassist.input.content");

                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("net.devemperor.wristassist.query", result.getData().getStringExtra("net.devemperor.wristassist.input.content2"));
                intent.putExtra("net.devemperor.wristassist.system_query", systemQuery);
                startActivity(intent);
            }
        });

        editApiKeyLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
           if (result.getResultCode() == RESULT_OK && result.getData() != null) {
               sp.edit().putString("net.devemperor.wristassist.api_key", result.getData().getStringExtra("net.devemperor.wristassist.input.content")).apply();
               sp.edit().putBoolean("net.devemperor.wristassist.onboarding_complete", true).apply();
           }
        });

        if (getIntent().getBooleanExtra("net.devemperor.wristassist.enter_api_key", false)) {
            Intent intent = new InputIntentBuilder(this)
                    .setTitle(getString(R.string.wristassist_set_api_key))
                    .setHint(getString(R.string.wristassist_api_key))
                    .build();
            editApiKeyLauncher.launch(intent);
        } else if (!sp.getBoolean("net.devemperor.wristassist.onboarding_complete", false)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
        } else if (sp.getInt("net.devemperor.wristassist.last_version_code", 0) < BuildConfig.VERSION_CODE) {
            startActivity(new Intent(this, ChangelogActivity.class));
        } else if (getIntent().getBooleanExtra("net.devemperor.wristassist.complication", false)
                || sp.getBoolean("net.devemperor.wristassist.instant_input", false)) {
            input(false);
        }

        mainWrv.requestFocus();
        mainWrv.postDelayed(() -> {
            View view = mainWrv.getChildAt(0);
            if (view == null) return;
            mainWrv.scrollBy(0, view.getHeight());
        }, 100);
    }

    private void input(boolean withSystemMessage) {
        if (withSystemMessage) {
            Intent intent = new InputIntentBuilder(this)
                .setTitle(getString(R.string.wristassist_enter_system_prompt))
                .setHint(getString(R.string.wristassist_system_prompt))
                .setTitle2(getString(R.string.wristassist_enter_prompt))
                .setHint2(getString(R.string.wristassist_prompt))
                .build();
            inputWithSystemMessageLauncher.launch(intent);
        } else {
            Intent intent = new InputIntentBuilder(this)
                .setTitle(getString(R.string.wristassist_enter_prompt))
                .setHint(getString(R.string.wristassist_prompt))
                .setHandsFree(sp.getBoolean("net.devemperor.wristassist.hands_free", false))
                .build();
            inputLauncher.launch(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainPb.setVisibility(View.GONE);
    }
}