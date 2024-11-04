package net.devemperor.wristassist.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import net.devemperor.wristassist.BuildConfig;
import net.devemperor.wristassist.R;

import io.noties.markwon.Markwon;
import io.noties.markwon.html.HtmlPlugin;

public class ChangelogActivity extends AppCompatActivity {

    ScrollView changelogSv;
    TextView changelogTv;
    int lastVersionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changelog);

        changelogSv = findViewById(R.id.activity_changelog_sv);
        changelogTv = findViewById(R.id.activity_changelog_content_tv);

        SharedPreferences sp = getSharedPreferences("net.devemperor.wristassist", MODE_PRIVATE);
        Markwon markwon = Markwon.builder(this).usePlugin(HtmlPlugin.create()).build();
        String md = getString(R.string.changelog_md);
        int versionCode = BuildConfig.VERSION_CODE;
        lastVersionCode = sp.getInt("net.devemperor.wristassist.last_version_code", 0);

        if (lastVersionCode < 32) md = md.concat(getString(R.string.changelog_md_32));
        if (lastVersionCode < 31) md = md.concat(getString(R.string.changelog_md_31));
        if (lastVersionCode < 30) md = md.concat(getString(R.string.changelog_md_30));
        if (lastVersionCode < 29) md = md.concat(getString(R.string.changelog_md_29));
        if (lastVersionCode < 28) md = md.concat(getString(R.string.changelog_md_28));
        if (lastVersionCode < 27) md = md.concat(getString(R.string.changelog_md_27));
        if (lastVersionCode < 26) md = md.concat(getString(R.string.changelog_md_26));
        if (lastVersionCode < 25) md = md.concat(getString(R.string.changelog_md_25));
        if (lastVersionCode < 24) {
            String newTts = "off";
            try {  // on first app launch, in newer versions the tts setting is a string, not a boolean
                if (sp.getBoolean("net.devemperor.wristassist.tts", false)) {
                    if (sp.getBoolean("net.devemperor.wristassist.auto_tts", false))
                        newTts = "on_auto";
                    else newTts = "on";
                }
            } catch (ClassCastException ignored) { }
            sp.edit().putString("net.devemperor.wristassist.tts", newTts).apply();
            md = md.concat(getString(R.string.changelog_md_24));
        }
        if (lastVersionCode < 23) md = md.concat(getString(R.string.changelog_md_23));
        if (lastVersionCode < 22) md = md.concat(getString(R.string.changelog_md_22));
        if (lastVersionCode < 21) md = md.concat(getString(R.string.changelog_md_21));

        markwon.setMarkdown(changelogTv, md);
        sp.edit().putInt("net.devemperor.wristassist.last_version_code", versionCode).apply();

        changelogSv.requestFocus();
    }

    public void okay(View view) {
        if (lastVersionCode < 27) {
            Intent intent = new Intent(this, QRCodeActivity.class);
            intent.putExtra("net.devemperor.wristassist.image_url", "https://platform.openai.com/account/billing/overview");
            startActivity(intent);
        }
        finish();
    }
}