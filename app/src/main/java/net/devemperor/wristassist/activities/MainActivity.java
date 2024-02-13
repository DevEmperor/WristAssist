package net.devemperor.wristassist.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.core.splashscreen.SplashScreen;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import net.devemperor.wristassist.BuildConfig;
import net.devemperor.wristassist.R;
import net.devemperor.wristassist.adapters.MainAdapter;
import net.devemperor.wristassist.items.MainItem;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {

    WearableRecyclerView mainWrv;
    ProgressBar mainPb;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        sp = getSharedPreferences("net.devemperor.wristassist", MODE_PRIVATE);
        if (sp.getString("net.devemperor.wristassist.userid", null) == null) {
            Random random = new Random();
            sp.edit().putString("net.devemperor.wristassist.userid", String.valueOf(random.nextInt(999999999 - 100000000) + 100000000)).apply();
        }
        FirebaseCrashlytics.getInstance().setUserId(sp.getString("net.devemperor.wristassist.userid", "null"));

        if (getIntent().getBooleanExtra("net.devemperor.wristassist.enter_api_key", false)) {
            Intent intent = new Intent(this, InputActivity.class);
            intent.putExtra("net.devemperor.wristassist.input.title", getString(R.string.wristassist_set_api_key));
            intent.putExtra("net.devemperor.wristassist.input.hint", getString(R.string.wristassist_api_key));
            startActivityForResult(intent, 1340);
        } else if (!sp.getBoolean("net.devemperor.wristassist.onboarding_complete", false)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
        } else if (sp.getInt("net.devemperor.wristassist.last_version_code", 0) < BuildConfig.VERSION_CODE) {
            startActivity(new Intent(this, ChangelogActivity.class));
        } else if (getIntent().getBooleanExtra("net.devemperor.wristassist.complication", false)
                || sp.getBoolean("net.devemperor.wristassist.instant_input", false)) {
            input(false);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainWrv = findViewById(R.id.main_wrv);
        mainPb = findViewById(R.id.main_pb);

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
                intent = new Intent(this, ImageActivity.class);
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
        mainWrv.requestFocus();
        mainWrv.postDelayed(() -> {
            View view = mainWrv.getChildAt(0);
            if (view == null) return;
            mainWrv.scrollBy(0, view.getHeight());
        }, 100);
    }

    private void input(boolean withSystemMessage) {
        Intent intent = new Intent(this, InputActivity.class);
        if (withSystemMessage) {
            intent.putExtra("net.devemperor.wristassist.input.title", getString(R.string.wristassist_enter_system_prompt));
            intent.putExtra("net.devemperor.wristassist.input.hint", getString(R.string.wristassist_system_prompt));
            intent.putExtra("net.devemperor.wristassist.input.title2", getString(R.string.wristassist_enter_prompt));
            intent.putExtra("net.devemperor.wristassist.input.hint2", getString(R.string.wristassist_prompt));
            startActivityForResult(intent, 1338);
        } else {
            intent.putExtra("net.devemperor.wristassist.input.title", getString(R.string.wristassist_enter_prompt));
            intent.putExtra("net.devemperor.wristassist.input.hint", getString(R.string.wristassist_prompt));
            intent.putExtra("net.devemperor.wristassist.input.hands_free", sp.getBoolean("net.devemperor.wristassist.hands_free", false));
            startActivityForResult(intent, 1337);
        }
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
            sp.edit().putString("net.devemperor.wristassist.api_key", data.getStringExtra("net.devemperor.wristassist.input.content")).apply();
            sp.edit().putBoolean("net.devemperor.wristassist.onboarding_complete", true).apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainPb.setVisibility(View.GONE);
    }
}