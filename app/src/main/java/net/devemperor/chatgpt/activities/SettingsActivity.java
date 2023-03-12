package net.devemperor.chatgpt.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.RemoteInput;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.wear.input.RemoteInputIntentHelper;

import com.theokanning.openai.service.OpenAiService;

import net.devemperor.chatgpt.R;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends Activity {

    TextView apiKeyStubTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        apiKeyStubTv = findViewById(R.id.api_key_stub_tv);
        String currentApiKey = getSharedPreferences("net.devemperor.chatgpt", MODE_PRIVATE)
                .getString("net.devemperor.chatgpt.api_key", "");
        apiKeyStubTv.setText(currentApiKey.equals("") ? getString(R.string.chatgpt_no_api_key) : currentApiKey.substring(0, 10) + "...");
    }

    public void changeApiKey(View view) {
        RemoteInput remoteInput = new RemoteInput.Builder("api_key").setLabel(getString(R.string.chatgpt_set_api_key)).build();
        Intent intent = RemoteInputIntentHelper.createActionRemoteInputIntent();
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, Collections.singletonList(remoteInput));
        startActivityForResult(intent, 42);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 42 && resultCode == RESULT_OK) {
            Bundle results = RemoteInput.getResultsFromIntent(data);
            if (results != null) {
                String apiKey = results.getCharSequence("api_key").toString();

                ExecutorService thread = Executors.newSingleThreadExecutor();
                thread.execute(() -> {
                    try {
                        OpenAiService service = new OpenAiService(apiKey);
                        service.getModel("gpt-3.5-turbo");
                    } catch (RuntimeException e) {
                        runOnUiThread(() -> {
                            if (Objects.requireNonNull(e.getMessage()).contains("Incorrect API key provided") || e.getMessage().contains("Unexpected char")) {
                                apiKeyStubTv.setText(getString(R.string.chatgpt_invalid_api_key));
                            } else {
                                apiKeyStubTv.setText(getString(R.string.chatgpt_no_internet));
                            }
                            apiKeyStubTv.setTextColor(Color.parseColor("#cc0000"));
                        });
                        return;
                    }
                    runOnUiThread(() -> {
                        getSharedPreferences("net.devemperor.chatgpt", MODE_PRIVATE).edit()
                                .putString("net.devemperor.chatgpt.api_key", apiKey).apply();
                        apiKeyStubTv.setText(apiKey.substring(0, 10) + "...");
                        apiKeyStubTv.setTextColor(Color.parseColor("#74aa9c"));
                    });
                });
            }
        }
    }
}