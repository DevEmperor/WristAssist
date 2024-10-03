package net.devemperor.wristassist.activities;

import static com.theokanning.openai.service.OpenAiService.defaultClient;
import static com.theokanning.openai.service.OpenAiService.defaultObjectMapper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.jsibbold.zoomage.ZoomageView;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.image.ImageResult;
import com.theokanning.openai.service.OpenAiService;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.database.ImageModel;
import net.devemperor.wristassist.database.ImagesDatabaseHelper;
import net.devemperor.wristassist.database.UsageDatabaseHelper;
import net.devemperor.wristassist.util.WristAssistUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class CreateImageActivity extends AppCompatActivity {

    SharedPreferences sp;
    UsageDatabaseHelper usageDatabaseHelper;
    ImagesDatabaseHelper imagesDatabaseHelper;
    OpenAiService service;
    Vibrator vibrator;

    ScrollView createImageSv;
    ProgressBar imagePb;
    TextView errorTv;
    ImageButton retryBtn;
    ZoomageView imageView;
    ImageButton shareBtn;
    TextView expiresInTv;
    ConstraintLayout saveDiscardBtns;

    String prompt;
    String model;
    String quality;
    String style;
    String size;
    ImageResult imageResult;
    Image image;
    Bitmap bitmap;
    ExecutorService thread;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_image);

        sp = getSharedPreferences("net.devemperor.wristassist", MODE_PRIVATE);
        imagesDatabaseHelper = new ImagesDatabaseHelper(this);
        usageDatabaseHelper = new UsageDatabaseHelper(this);

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
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        createImageSv = findViewById(R.id.activity_create_image_sv);
        imagePb = findViewById(R.id.activity_create_image_pb);
        errorTv = findViewById(R.id.activity_create_image_error_tv);
        retryBtn = findViewById(R.id.activity_create_image_retry_btn);
        imageView = findViewById(R.id.activity_create_image_image_zv);
        shareBtn = findViewById(R.id.activity_create_image_share_btn);
        expiresInTv = findViewById(R.id.activity_create_image_expires_tv);
        saveDiscardBtns = findViewById(R.id.activity_create_image_save_discard_btns);

        prompt = getIntent().getStringExtra("net.devemperor.wristassist.prompt");
        model = sp.getBoolean("net.devemperor.wristassist.image_model", false) ? "dall-e-3" : "dall-e-2";
        quality = sp.getBoolean("net.devemperor.wristassist.image_quality", false) ? "hd" : "standard";
        style = sp.getBoolean("net.devemperor.wristassist.image_style", false) ? "natural" : "vivid";
        size = sp.getBoolean("net.devemperor.wristassist.image_model", false) ? "1024x1024" : sp.getString("net.devemperor.wristassist.image_size", "1024x1024");

        createAndDownloadImage();
        createImageSv.requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        if (thread != null) {
            thread.shutdownNow();
        }
    }

    private void createAndDownloadImage() {
        imagePb.setVisibility(View.VISIBLE);
        errorTv.setVisibility(View.GONE);
        retryBtn.setVisibility(View.GONE);

        thread = Executors.newSingleThreadExecutor();
        thread.execute(() -> {
            timer = new Timer();
            try {
                CreateImageRequest cir = CreateImageRequest.builder()
                        .responseFormat("url")
                        .n(1)
                        .prompt(prompt)
                        .model(model)
                        .quality(quality)
                        .size(size)
                        .style(style)
                        .build();
                imageResult = service.createImage(cir);
                image = imageResult.getData().get(0);

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        long minutes = (imageResult.getCreated()*1000 + 60*60*1000 - System.currentTimeMillis()) / 60 / 1000;
                        runOnUiThread(() -> {
                            if (minutes <= 0) {
                                expiresInTv.setVisibility(View.GONE);
                                shareBtn.setVisibility(View.GONE);
                                timer.cancel();
                            } else {
                                expiresInTv.setText(getString(R.string.wristassist_image_expires_in, minutes));
                            }
                        });
                    }
                }, 0, 60*1000);

                usageDatabaseHelper.edit(model, 1, WristAssistUtil.calcCostImage(model, quality, size));

                OkHttpClient downloadClient = new OkHttpClient();
                Request request = new Request.Builder().url(image.getUrl()).build();

                Response response = downloadClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                assert response.body() != null;
                InputStream inputStream = response.body().byteStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap == null) {
                    throw new IOException("Bitmap is null");
                } else {
                    runOnUiThread(() -> {
                        if (sp.getBoolean("net.devemperor.wristassist.vibrate", true)) {
                            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                        }

                        imageView.setImageBitmap(bitmap);
                        imagePb.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        shareBtn.setVisibility(View.VISIBLE);
                        expiresInTv.setVisibility(View.VISIBLE);
                        saveDiscardBtns.setVisibility(View.VISIBLE);
                    });
                }
            } catch (RuntimeException | IOException e) {
                FirebaseCrashlytics fc = FirebaseCrashlytics.getInstance();
                fc.setCustomKey("settings", sp.getAll().toString());
                fc.setUserId(sp.getString("net.devemperor.wristassist.userid", "null"));
                fc.recordException(e);

                runOnUiThread(() -> {
                    imagePb.setVisibility(View.GONE);
                    errorTv.setVisibility(View.VISIBLE);
                    retryBtn.setVisibility(View.VISIBLE);
                    timer.cancel();

                    if (sp.getBoolean("net.devemperor.wristassist.vibrate", true)) {
                        vibrator.vibrate(VibrationEffect.createWaveform(new long[]{50, 50, 50, 50, 50}, new int[]{-1, 0, -1, 0, -1}, -1));
                    }

                    if (Objects.requireNonNull(e.getMessage()).contains("SocketTimeoutException")) {
                        errorTv.setText(R.string.wristassist_timeout);
                    } else if (e.getMessage().contains("API key")) {
                        errorTv.setText(getString(R.string.wristassist_invalid_api_key_message));
                    } else if (e.getMessage().contains("rejected")) {
                        errorTv.setText(R.string.wristassist_image_request_rejected);
                    } else if (e.getMessage().contains("quota") || e.getMessage().contains("limit")) {
                        errorTv.setText(R.string.wristassist_quota_exceeded);
                    } else if (e.getMessage().contains("does not exist")) {
                        errorTv.setText(R.string.wristassist_no_access);
                    } else {
                        errorTv.setText(R.string.wristassist_no_internet);
                    }
                });
            }
        });
    }

    public void retry(View view) {
        createAndDownloadImage();
    }

    public void shareImage(View view) {
        Intent intent = new Intent(this, QRCodeActivity.class);
        intent.putExtra("net.devemperor.wristassist.image_url", image.getUrl());
        startActivity(intent);
    }

    public void saveImage(View view) {
        Toast.makeText(this, R.string.wristassist_saving, Toast.LENGTH_SHORT).show();

        ImageModel imageModel;
        if (model.equals("dall-e-3")) {
            imageModel = new ImageModel(-1, prompt, image.getRevisedPrompt(), model, quality, size, style, imageResult.getCreated() * 1000, image.getUrl());
        } else {
            imageModel = new ImageModel(-1, prompt, null, model, null, size, null, imageResult.getCreated() * 1000, image.getUrl());
        }
        int id = imagesDatabaseHelper.add(imageModel);

        try {
            FileOutputStream out = openFileOutput("image_" + id + ".png", MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (IOException ignored) { }
        timer.cancel();

        Intent data = new Intent();
        data.putExtra("net.devemperor.wristassist.imageId", id);
        setResult(RESULT_OK, data);
        finish();
    }

    public void discardImage(View view) {
        timer.cancel();
        finish();
    }
}