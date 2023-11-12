package net.devemperor.wristassist.activities;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changelog);

        changelogSv = findViewById(R.id.changelog_sv);
        changelogTv = findViewById(R.id.changelog_tv);

        Markwon markwon = Markwon.builder(this).usePlugin(HtmlPlugin.create()).build();
        String md = getString(R.string.changelog_md);
        int versionCode = BuildConfig.VERSION_CODE;
        int lastVersionCode = getSharedPreferences("net.devemperor.wristassist", MODE_PRIVATE).getInt("net.devemperor.wristassist.last_version_code", 0);

        if (lastVersionCode < 22) md = md.concat(getString(R.string.changelog_md_22));
        if (lastVersionCode < 21) md = md.concat(getString(R.string.changelog_md_21));
        markwon.setMarkdown(changelogTv, md);
        System.out.println(md);
        getSharedPreferences("net.devemperor.wristassist", MODE_PRIVATE).edit().putInt("net.devemperor.wristassist.last_version_code", versionCode).apply();
        changelogSv.requestFocus();
    }

    public void okay(View view) {
        finish();
    }
}