package net.devemperor.wristassist.activities;

import static com.theokanning.openai.service.OpenAiService.defaultClient;
import static com.theokanning.openai.service.OpenAiService.defaultObjectMapper;

import android.app.Activity;
import android.app.RemoteInput;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.wear.input.RemoteInputIntentHelper;
import androidx.wear.widget.ConfirmationOverlay;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.adapters.ChatAdapter;
import net.devemperor.wristassist.database.ChatHistoryModel;
import net.devemperor.wristassist.database.DatabaseHelper;
import net.devemperor.wristassist.items.ChatItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class ChatActivity extends Activity {

    ListView chatLv;
    ProgressBar progressBar;
    ImageButton askBtn;
    ImageButton saveResetBtn;
    TextView errorTv;
    TextView titleTv;
    ChatAdapter chatAdapter;

    OpenAiService service;
    ExecutorService thread;

    Vibrator vibrator;

    DatabaseHelper databaseHelper;
    SharedPreferences sp;

    boolean firstAnswerComplete = false;
    boolean saveThisChat = false;
    long id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatAdapter = new ChatAdapter(this, new ArrayList<>());
        chatLv = findViewById(R.id.chat_lv);

        chatLv.setAdapter(chatAdapter);
        View footerView = LayoutInflater.from(this).inflate(R.layout.layout_chat_footer, chatLv, false);
        chatLv.addFooterView(footerView);
        View headerView = LayoutInflater.from(this).inflate(R.layout.layout_chat_header, chatLv, false);
        chatLv.addHeaderView(headerView);
        progressBar = footerView.findViewById(R.id.progress_bar);
        askBtn = footerView.findViewById(R.id.ask_btn);
        saveResetBtn = footerView.findViewById(R.id.save_btn);
        errorTv = footerView.findViewById(R.id.error_tv);
        titleTv = headerView.findViewById(R.id.title_tv);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        databaseHelper = new DatabaseHelper(this);
        sp = getSharedPreferences("net.devemperor.wristassist", MODE_PRIVATE);

        String apiKey = sp.getString("net.devemperor.wristassist.api_key", "noApiKey");
        String apiHost = sp.getString("net.devemperor.wristassist.api_host", "https://api.openai.com/");
        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient client = defaultClient(apiKey, Duration.ofSeconds(120)).newBuilder().build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(apiHost)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        OpenAiApi api = retrofit.create(OpenAiApi.class);

        service = new OpenAiService(api);

        chatLv.requestFocus();

        if (getIntent().getLongExtra("net.devemperor.wristassist.chatId", -1) != -1) {
            long id = getIntent().getLongExtra("net.devemperor.wristassist.chatId", -1);
            titleTv.setText(databaseHelper.getTitle(id));
            titleTv.setVisibility(View.VISIBLE);
            saveResetBtn.setVisibility(View.VISIBLE);
            saveResetBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twotone_change_circle_24));

            JSONArray chatObject;
            try {
                String filePath = getFilesDir().getAbsolutePath() + "/chat_" + id + ".json";
                BufferedReader in = new BufferedReader(new FileReader(filePath));
                chatObject = new JSONArray(in.readLine());
                in.close();

                for (int i = 0; i < chatObject.length(); i++) {
                    JSONObject chatMessage = chatObject.optJSONObject(i);
                    ChatItem chatItem = new ChatItem(new ChatMessage(chatMessage.getString("role"),
                            chatMessage.getString("content")), chatMessage.getInt("cost"));
                    chatAdapter.add(chatItem);
                }
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }
            if (chatAdapter.getCount() > 1) {
                firstAnswerComplete = true;
                saveResetBtn.setVisibility(View.VISIBLE);
            }
            saveThisChat = true;
            this.id = id;

            if (chatAdapter.getChatItems().get(chatAdapter.getCount() - 1).getChatMessage().getRole().equals("user")) {
                try {
                    query(chatAdapter.getChatItems().get(chatAdapter.getCount() - 1).getChatMessage().getContent());
                } catch (JSONException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            String systemQuery = getIntent().getStringExtra("net.devemperor.wristassist.system_query");
            if (systemQuery != null) {
                ChatItem systemItem = new ChatItem(new ChatMessage("system", systemQuery), 0);
                chatAdapter.add(systemItem);
            }

            try {
                query(getIntent().getStringExtra("net.devemperor.wristassist.query"));
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (thread != null) {
            thread.shutdownNow();
        }
        chatAdapter.shutdownServices();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CharSequence result_text = RemoteInput.getResultsFromIntent(data).getCharSequence("result_text");
        if (requestCode == 1337 && resultCode == RESULT_OK && result_text != null) {
            try {
                query(result_text.toString());
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }
        } else if (requestCode == 1338 && resultCode == RESULT_OK) {
            titleTv.setText(result_text);
            titleTv.setVisibility(View.VISIBLE);

            try {
                id = databaseHelper.add(this, new ChatHistoryModel(-1, titleTv.getText().toString(), chatAdapter.getChatItems()));
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }

            saveResetBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twotone_change_circle_24));
            saveThisChat = true;
        }
    }

    private void openKeyboard(int returnCode) {
        RemoteInput remoteInput = new RemoteInput.Builder("result_text").build();
        Intent intent = RemoteInputIntentHelper.createActionRemoteInputIntent();
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, Collections.singletonList(remoteInput));
        startActivityForResult(intent, returnCode);
    }

    public void saveReset(View view) throws JSONException, IOException {
        if (!saveThisChat) {
            openKeyboard(1338);
        } else {
            for (int i = chatAdapter.getCount() - 1; i > ((chatAdapter.getItem(0).getChatMessage().getRole().equals(ChatMessageRole.SYSTEM.value())) ? 1 : 0); i--) {
                chatAdapter.remove(chatAdapter.getItem(i));
            }
            databaseHelper.reset(this, id, chatAdapter.getChatItems());
            firstAnswerComplete = false;
            saveResetBtn.setVisibility(View.GONE);
            new ConfirmationOverlay().showOn(this);
            query(chatAdapter.getChatItems().get(chatAdapter.getCount() - 1).getChatMessage().getContent());
        }
    }

    public void ask(View view) throws JSONException, IOException {
        if (errorTv.getVisibility() == View.VISIBLE) {
            query(chatAdapter.getChatItems().get(chatAdapter.getCount() - 1).getChatMessage().getContent());
        } else {
            openKeyboard(1337);
        }
    }

    private void query(String query) throws JSONException, IOException {
        if (chatAdapter.getCount() == 0 || !chatAdapter.getChatItems().get(chatAdapter.getCount() - 1).getChatMessage().getRole().equals("user")) {
            ChatItem userItem = new ChatItem(new ChatMessage("user", query), 0);
            chatAdapter.add(userItem);
            if (saveThisChat) {
                databaseHelper.edit(this, id, userItem);
            }
        }

        progressBar.setVisibility(View.VISIBLE);
        errorTv.setVisibility(View.GONE);
        askBtn.setEnabled(false);
        askBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twotone_keyboard_24_off));
        saveResetBtn.setEnabled(false);
        if (saveThisChat) {
            saveResetBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twotone_change_circle_24_off));
        }

        ChatCompletionRequest ccr = ChatCompletionRequest.builder()
                .model(sp.getString("net.devemperor.wristassist.model", "gpt3.5-turbo"))
                .messages(chatAdapter.getChatMessages())
                .build();

        thread = Executors.newSingleThreadExecutor();
        thread.execute(() -> {
            try {
                ChatCompletionResult result = service.createChatCompletion(ccr);
                String answer = result.getChoices().get(0).getMessage().getContent().trim();
                long cost = result.getUsage().getTotalTokens();
                ChatItem assistantItem = new ChatItem(new ChatMessage("assistant", answer), cost);
                sp.edit().putLong("net.devemperor.wristassist.total_tokens", sp.getLong("net.devemperor.wristassist.total_tokens", 0) + cost).apply();
                if (Thread.interrupted()) {
                    return;
                }
                if (saveThisChat) {
                    databaseHelper.edit(this, id, assistantItem);
                }
                runOnUiThread(() -> {
                    if (sp.getBoolean("net.devemperor.wristassist.vibrate", true)) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                    }

                    chatAdapter.add(assistantItem);
                    progressBar.setVisibility(View.GONE);
                    askBtn.setEnabled(true);
                    askBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twotone_keyboard_24));
                    saveResetBtn.setEnabled(true);
                    if (saveThisChat) {
                        saveResetBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twotone_change_circle_24));
                    }

                    if (!firstAnswerComplete) {
                        saveResetBtn.setVisibility(View.VISIBLE);
                        firstAnswerComplete = true;
                    }

                    if (sp.getBoolean("net.devemperor.wristassist.auto_tts", false)) {
                        chatAdapter.launchTTS(answer);
                    }
                });
            } catch (RuntimeException e) {
                runOnUiThread(() -> {
                    e.printStackTrace();
                    if (Objects.requireNonNull(e.getMessage()).contains("SocketTimeoutException")) {
                        errorTv.setText(R.string.wristassist_timeout);
                    } else if (e.getMessage().contains("API key")) {
                        errorTv.setText(getString(R.string.wristassist_invalid_api_key_message));
                    } else {
                        errorTv.setText(R.string.wristassist_no_internet);
                    }
                    progressBar.setVisibility(View.GONE);
                    errorTv.setVisibility(View.VISIBLE);
                    askBtn.setEnabled(true);
                    askBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twotone_keyboard_24));
                    saveResetBtn.setEnabled(true);
                    saveResetBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twotone_change_circle_24));
                });
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}