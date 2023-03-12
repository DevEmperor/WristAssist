package net.devemperor.chatgpt.activites;

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
import android.widget.ListView;
import android.widget.ProgressBar;

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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class MainActivity extends Activity {

    private static final String TOKEN = "";  // TODO: remove before commit

    ListView chatLv;
    ProgressBar progressBar;
    Button askBtn;
    ChatAdapter chatAdapter;

    OpenAiService service;
    ExecutorService thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatLv = findViewById(R.id.chat_lv);

        chatAdapter = new ChatAdapter(this, new ArrayList<>());

        chatLv.setAdapter(chatAdapter);
        View footerView = LayoutInflater.from(this).inflate(R.layout.layout_btn_ask, chatLv, false);
        chatLv.addFooterView(footerView);
        progressBar = footerView.findViewById(R.id.progress_bar);
        askBtn = footerView.findViewById(R.id.btn_ask);

        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient client = defaultClient(TOKEN, Duration.ofSeconds(120)).newBuilder().build();
        Retrofit retrofit = defaultRetrofit(client, mapper);
        OpenAiApi api = retrofit.create(OpenAiApi.class);

        service = new OpenAiService(api);

        chatLv.requestFocus();

        ask(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1337 && resultCode == RESULT_OK) {
            Bundle results = RemoteInput.getResultsFromIntent(data);
            if (results != null) {
                String query = results.getCharSequence("query").toString();
                chatAdapter.add(new ChatItem(new ChatMessage("user", query), 0));
                progressBar.setVisibility(View.VISIBLE);
                askBtn.setEnabled(false);
                ChatCompletionRequest ccr = ChatCompletionRequest.builder()
                        .model("gpt-3.5-turbo")
                        .messages(chatAdapter.getChatMessages())
                        .build();

                thread = Executors.newSingleThreadExecutor();
                thread.execute(() -> {
                    ChatCompletionResult result = service.createChatCompletion(ccr);
                    String answer = result.getChoices().get(0).getMessage().getContent().trim();
                    long cost = result.getUsage().getTotalTokens();
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        askBtn.setEnabled(true);
                        chatAdapter.add(new ChatItem(new ChatMessage("assistant", answer), cost));
//                        chatLv.smoothScrollToPosition(chatAdapter.getCount());
                    });
                });
            }
        }
    }

    public void ask(View view) {
        RemoteInput remoteInput = new RemoteInput.Builder("query").setLabel(getString(R.string.chatgpt_query)).build();
        Intent intent = RemoteInputIntentHelper.createActionRemoteInputIntent();
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, Collections.singletonList(remoteInput));
        startActivityForResult(intent, 1337);
    }
}