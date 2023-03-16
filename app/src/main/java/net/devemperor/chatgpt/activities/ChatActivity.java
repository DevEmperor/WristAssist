package net.devemperor.chatgpt.activities;

import static com.theokanning.openai.service.OpenAiService.defaultClient;
import static com.theokanning.openai.service.OpenAiService.defaultObjectMapper;
import static com.theokanning.openai.service.OpenAiService.defaultRetrofit;

import android.app.Activity;
import android.app.RemoteInput;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.wear.input.RemoteInputIntentHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import net.devemperor.chatgpt.R;
import net.devemperor.chatgpt.adapters.ChatAdapter;
import net.devemperor.chatgpt.adapters.ChatItem;
import net.devemperor.chatgpt.database.ChatHistoryModel;
import net.devemperor.chatgpt.database.DatabaseHelper;

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

public class ChatActivity extends Activity {

    ListView chatLv;
    ProgressBar progressBar;
    Button askBtn;
    ImageButton saveBtn;
    TextView errorTv;
    TextView titleTv;
    ChatAdapter chatAdapter;

    OpenAiService service;
    ExecutorService thread;

    DatabaseHelper databaseHelper;

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
        saveBtn = footerView.findViewById(R.id.save_btn);
        errorTv = footerView.findViewById(R.id.error_tv);
        titleTv = headerView.findViewById(R.id.title_tv);

        databaseHelper = new DatabaseHelper(this);

        String apiKey = getSharedPreferences("net.devemperor.chatgpt", MODE_PRIVATE)
                .getString("net.devemperor.chatgpt.api_key", "noApiKey");
        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient client = defaultClient(apiKey, Duration.ofSeconds(120)).newBuilder().build();
        Retrofit retrofit = defaultRetrofit(client, mapper);
        OpenAiApi api = retrofit.create(OpenAiApi.class);

        service = new OpenAiService(api);

        chatLv.requestFocus();

        if (getIntent().getLongExtra("net.devemperor.chatgpt.chatId", -1) != -1) {
            long id = getIntent().getLongExtra("net.devemperor.chatgpt.chatId", -1);
            titleTv.setText(databaseHelper.getTitle(id));
            titleTv.setVisibility(View.VISIBLE);
            saveBtn.setVisibility(View.GONE);

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
            firstAnswerComplete = true;
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
            try {
                query(getIntent().getStringExtra("net.devemperor.chatgpt.query"));
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1337 && resultCode == RESULT_OK) {
            Bundle results = RemoteInput.getResultsFromIntent(data);
            if (results != null) {
                try {
                    query(results.getCharSequence("query").toString());
                } catch (JSONException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (requestCode == 1338 && resultCode == RESULT_OK) {
            Bundle results = RemoteInput.getResultsFromIntent(data);
            if (results != null) {
                titleTv.setText(results.getCharSequence("title"));
                titleTv.setVisibility(View.VISIBLE);

                try {
                    id = databaseHelper.add(this, new ChatHistoryModel(-1, titleTv.getText().toString(), chatAdapter.getChatItems()));
                } catch (JSONException | IOException e) {
                    throw new RuntimeException(e);
                }

                saveBtn.setVisibility(View.GONE);
                saveThisChat = true;
            }
        }
    }

    public void save(View view) {
        RemoteInput remoteInput = new RemoteInput.Builder("title").setLabel(getString(R.string.chatgpt_ask_title)).build();
        Intent intent = RemoteInputIntentHelper.createActionRemoteInputIntent();
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, Collections.singletonList(remoteInput));
        startActivityForResult(intent, 1338);
    }

    public void ask(View view) throws JSONException, IOException {
        if (errorTv.getVisibility() == View.VISIBLE) {
            query(chatAdapter.getChatItems().get(chatAdapter.getCount() - 1).getChatMessage().getContent());
        } else {
            RemoteInput remoteInput = new RemoteInput.Builder("query").setLabel(getString(R.string.chatgpt_query)).build();
            Intent intent = RemoteInputIntentHelper.createActionRemoteInputIntent();
            RemoteInputIntentHelper.putRemoteInputsExtra(intent, Collections.singletonList(remoteInput));
            startActivityForResult(intent, 1337);
        }
    }

    private void query(String query) throws JSONException, IOException {
        if (errorTv.getVisibility() != View.VISIBLE) {
            ChatItem userItem = new ChatItem(new ChatMessage("user", query), 0);
            chatAdapter.add(userItem);
            if (saveThisChat) {
                databaseHelper.edit(this, id, userItem);
            }
        }

        progressBar.setVisibility(View.VISIBLE);
        errorTv.setVisibility(View.GONE);
        askBtn.setEnabled(false);

        ChatCompletionRequest ccr = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(chatAdapter.getChatMessages())
                .build();

        thread = Executors.newSingleThreadExecutor();
        thread.execute(() -> {
            try {
                ChatCompletionResult result = service.createChatCompletion(ccr);
                String answer = result.getChoices().get(0).getMessage().getContent().trim();
                ChatItem assistantItem = new ChatItem(new ChatMessage("assistant", answer), result.getUsage().getTotalTokens());
                if (saveThisChat) {
                    databaseHelper.edit(this, id, assistantItem);
                }
                runOnUiThread(() -> {
                    chatAdapter.add(assistantItem);
                    progressBar.setVisibility(View.GONE);
                    askBtn.setEnabled(true);

                    if (!firstAnswerComplete) {
                        saveBtn.setVisibility(View.VISIBLE);
                        firstAnswerComplete = true;
                    }
                });
            } catch (RuntimeException e) {
                runOnUiThread(() -> {
                    if (Objects.requireNonNull(e.getMessage()).contains("SocketTimeoutException")) {
                        errorTv.setText(R.string.chatgpt_timeout);
                    } else if (e.getMessage().contains("Incorrect API key provided")) {
                        errorTv.setText(getString(R.string.chatgpt_invalid_api_key));
                    } else {
                        errorTv.setText(R.string.chatgpt_no_internet);
                    }
                    progressBar.setVisibility(View.GONE);
                    errorTv.setVisibility(View.VISIBLE);
                    askBtn.setEnabled(true);
                });
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}