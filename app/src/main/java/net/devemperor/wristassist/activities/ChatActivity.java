package net.devemperor.wristassist.activities;

import static com.theokanning.openai.service.OpenAiService.defaultClient;
import static com.theokanning.openai.service.OpenAiService.defaultObjectMapper;

import android.app.Activity;
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
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.theokanning.openai.Usage;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.adapters.ChatAdapter;
import net.devemperor.wristassist.database.ChatHistoryDatabaseHelper;
import net.devemperor.wristassist.database.ChatHistoryModel;
import net.devemperor.wristassist.database.UsageDatabaseHelper;
import net.devemperor.wristassist.items.ChatItem;
import net.devemperor.wristassist.util.WristAssistUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
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

    ChatHistoryDatabaseHelper chatHistoryDatabaseHelper;
    UsageDatabaseHelper usageDatabaseHelper;
    SharedPreferences sp;

    boolean firstAnswerComplete = false;
    boolean saveThisChat = false;
    long id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatAdapter = new ChatAdapter(this, new ArrayList<>());
        chatLv = findViewById(R.id.activity_chat_lv);

        chatLv.setAdapter(chatAdapter);
        View footerView = LayoutInflater.from(this).inflate(R.layout.layout_chat_footer, chatLv, false);
        chatLv.addFooterView(footerView);
        View headerView = LayoutInflater.from(this).inflate(R.layout.layout_chat_header, chatLv, false);
        chatLv.addHeaderView(headerView);
        progressBar = footerView.findViewById(R.id.layout_chat_footer_pb);
        askBtn = footerView.findViewById(R.id.layout_chat_footer_ask_btn);
        saveResetBtn = footerView.findViewById(R.id.layout_chat_footer_save_btn);
        errorTv = footerView.findViewById(R.id.layout_chat_footer_error_tv);
        titleTv = headerView.findViewById(R.id.layout_chat_header_title_tv);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        chatHistoryDatabaseHelper = new ChatHistoryDatabaseHelper(this);
        usageDatabaseHelper = new UsageDatabaseHelper(this);
        sp = getSharedPreferences("net.devemperor.wristassist", MODE_PRIVATE);

        String apiKey = sp.getString("net.devemperor.wristassist.api_key", "noApiKey");
        String apiHost = sp.getString("net.devemperor.wristassist.custom_server_host", "https://api.openai.com/");
        ObjectMapper mapper = defaultObjectMapper();  // replaces all control chars (#10 @ GH)
        OkHttpClient client = defaultClient(apiKey.replaceAll("[^ -~]", ""), Duration.ofSeconds(120)).newBuilder().build();
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
            id = getIntent().getLongExtra("net.devemperor.wristassist.chatId", -1);
            titleTv.setText(chatHistoryDatabaseHelper.getTitle(id));
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

            titleTv.setOnClickListener(v -> chatLv.setSelection(chatAdapter.getCount() + 1));

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
        if (resultCode != RESULT_OK) return;

        String content = data.getStringExtra("net.devemperor.wristassist.input.content");
        if (requestCode == 1337) {
            try {
                query(content);
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }
        } else if (requestCode == 1338) {
            titleTv.setText(content);
            titleTv.setVisibility(View.VISIBLE);

            try {
                id = chatHistoryDatabaseHelper.add(this, new ChatHistoryModel(-1, content, chatAdapter.getChatItems()));
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }

            saveResetBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twotone_change_circle_24));
            saveThisChat = true;
        }
    }

    public void saveReset(View view) throws JSONException, IOException {
        if (!saveThisChat) {
            Intent intent = new Intent(this, InputActivity.class);
            intent.putExtra("net.devemperor.wristassist.input.title", getString(R.string.wristassist_set_chat_title));
            intent.putExtra("net.devemperor.wristassist.input.hint", getString(R.string.wristassist_chat_title));
            startActivityForResult(intent, 1338);
        } else {
            for (int i = chatAdapter.getCount() - 1; i > ((chatAdapter.getItem(0).getChatMessage().getRole().equals(ChatMessageRole.SYSTEM.value())) ? 1 : 0); i--) {
                chatAdapter.remove(chatAdapter.getItem(i));
            }
            chatHistoryDatabaseHelper.reset(this, id, chatAdapter.getChatItems());
            firstAnswerComplete = false;
            saveResetBtn.setVisibility(View.GONE);
            Toast.makeText(this, R.string.wristassist_chat_reset, Toast.LENGTH_SHORT).show();
            query(chatAdapter.getChatItems().get(chatAdapter.getCount() - 1).getChatMessage().getContent());
        }
    }

    public void ask(View view) throws JSONException, IOException {
        if (errorTv.getVisibility() == View.VISIBLE) {
            query(chatAdapter.getChatItems().get(chatAdapter.getCount() - 1).getChatMessage().getContent());
        } else {
            Intent intent = new Intent(this, InputActivity.class);
            intent.putExtra("net.devemperor.wristassist.input.title", getString(R.string.wristassist_enter_prompt));
            intent.putExtra("net.devemperor.wristassist.input.hint", getString(R.string.wristassist_prompt));
            intent.putExtra("net.devemperor.wristassist.input.hands_free", sp.getBoolean("net.devemperor.wristassist.hands_free", false));
            startActivityForResult(intent, 1337);
        }
    }

    private void query(String query) throws JSONException, IOException {
        if (chatAdapter.getCount() == 0 || !chatAdapter.getChatItems().get(chatAdapter.getCount() - 1).getChatMessage().getRole().equals("user")) {
            ChatItem userItem = new ChatItem(new ChatMessage("user", query), 0);
            chatAdapter.add(userItem);
            if (saveThisChat) {
                chatHistoryDatabaseHelper.edit(this, id, userItem);
            }
        }

        progressBar.setVisibility(View.VISIBLE);
        errorTv.setVisibility(View.GONE);
        askBtn.setEnabled(false);
        askBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twotone_keyboard_24_off));
        saveResetBtn.setEnabled(false);
        saveResetBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twotone_save_24_off));
        if (saveThisChat) {
            saveResetBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twotone_change_circle_24_off));
        }

        String model = sp.getString("net.devemperor.wristassist.model", "gpt-4o-mini");
        if (sp.getBoolean("net.devemperor.wristassist.custom_server", false)) {
            model = sp.getString("net.devemperor.wristassist.custom_server_model", "gpt-4o-mini");
        }
        ChatCompletionRequest ccr = ChatCompletionRequest.builder()
                .model(model)
                .messages(chatAdapter.getChatMessages())
                .build();

        thread = Executors.newSingleThreadExecutor();
        String finalModel = model;
        thread.execute(() -> {
            try {
                ChatCompletionResult result = service.createChatCompletion(ccr);
                ChatMessage answer = result.getChoices().get(0).getMessage();
                Usage usage = result.getUsage();
                ChatItem assistantItem = new ChatItem(answer, usage.getTotalTokens());

                usageDatabaseHelper.edit(finalModel, usage.getTotalTokens(), WristAssistUtil.calcCostChat(finalModel, usage.getPromptTokens(), usage.getCompletionTokens()));

                if (Thread.interrupted()) {
                    return;
                }
                if (saveThisChat) {
                    chatHistoryDatabaseHelper.edit(this, id, assistantItem);
                }
                runOnUiThread(() -> {
                    if (sp.getBoolean("net.devemperor.wristassist.vibrate", true)) {
                        vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                    }

                    chatAdapter.add(assistantItem);
                    progressBar.setVisibility(View.GONE);
                    askBtn.setEnabled(true);
                    askBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twotone_keyboard_24));
                    saveResetBtn.setEnabled(true);
                    saveResetBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twotone_save_24));
                    if (saveThisChat) {
                        saveResetBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twotone_change_circle_24));
                    }

                    if (!firstAnswerComplete) {
                        saveResetBtn.setVisibility(View.VISIBLE);
                        firstAnswerComplete = true;
                    }

                    if (sp.getString("net.devemperor.wristassist.tts", "off").equals("on_auto") ||
                            (sp.getString("net.devemperor.wristassist.tts", "off").equals("adapt_to_input") &&
                                    sp.getBoolean("net.devemperor.wristassist.hands_free", false))) {
                        chatAdapter.launchTTS(answer.getContent());
                    }
                });
            } catch (RuntimeException e) {
                FirebaseCrashlytics fc = FirebaseCrashlytics.getInstance();
                fc.setCustomKey("settings", sp.getAll().toString());
                fc.setUserId(sp.getString("net.devemperor.wristassist.userid", "null"));
                fc.recordException(e);
                fc.sendUnsentReports();

                runOnUiThread(() -> {
                    if (sp.getBoolean("net.devemperor.wristassist.vibrate", true)) {
                        vibrator.vibrate(VibrationEffect.createWaveform(new long[]{50, 50, 50, 50, 50}, new int[]{-1, 0, -1, 0, -1}, -1));
                    }

                    e.printStackTrace();
                    if (Objects.requireNonNull(e.getMessage()).contains("SocketTimeoutException")) {
                        errorTv.setText(R.string.wristassist_timeout);
                    } else if (e.getMessage().contains("API key")) {
                        errorTv.setText(getString(R.string.wristassist_invalid_api_key_message));
                    } else if (e.getMessage().contains("context")) {
                        errorTv.setText(R.string.wristassist_context_exceeded);
                    } else if (e.getMessage().contains("quota")) {
                        errorTv.setText(R.string.wristassist_quota_exceeded);
                    } else if (e.getMessage().contains("does not exist")) {
                        errorTv.setText(R.string.wristassist_no_access);
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