package net.devemperor.wristassist.activities;

import static com.theokanning.openai.service.OpenAiService.defaultClient;
import static com.theokanning.openai.service.OpenAiService.defaultObjectMapper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.theokanning.openai.audio.CreateTranscriptionRequest;
import com.theokanning.openai.audio.TranscriptionResult;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.service.OpenAiService;

import net.devemperor.wristassist.R;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class InputWhisperActivity extends AppCompatActivity {

    TextView titleTv;
    FloatingActionButton sendBtn;
    LinearProgressIndicator progressBar;
    TextView timeTv;
    ImageView recordingIv;
    ImageView recordingIv2;
    ImageView errorIv;
    ImageView errorIv2;

    SharedPreferences sp;
    MediaRecorder recorder;
    Runnable recordTimeRunnable;
    Handler recordTimeHandler;
    ExecutorService speechApiThread;
    long elapsedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_whisper);

        titleTv = findViewById(R.id.activity_input_whisper_title_tv);
        sendBtn = findViewById(R.id.activity_input_whisper_send_btn);
        progressBar = findViewById(R.id.activity_input_whisper_pb);
        timeTv = findViewById(R.id.activity_input_whisper_time_tv);
        recordingIv = findViewById(R.id.activity_input_whisper_recording_iv);
        recordingIv2 = findViewById(R.id.activity_input_whisper_recording_iv2);
        errorIv = findViewById(R.id.activity_input_whisper_error_iv);
        errorIv2 = findViewById(R.id.activity_input_whisper_error_iv2);

        sp = getSharedPreferences("net.devemperor.wristassist", MODE_PRIVATE);

        recordTimeHandler = new Handler(Looper.getMainLooper());
        recordTimeRunnable = new Runnable() {
            @Override
            public void run() {
                elapsedTime += 100;
                timeTv.setText(String.format(Locale.getDefault(), "%02d:%02d", (int) (elapsedTime / 60000), (int) (elapsedTime / 1000) % 60));
                recordTimeHandler.postDelayed(this, 100);
            }
        };

        titleTv.setText(getIntent().getStringExtra("net.devemperor.wristassist.input.title"));

        sendBtn.setOnClickListener(v -> {
            if (recorder != null) {
                stopRecording();
            } else {
                startRecording();
            }
        });

        startRecording();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (recorder != null) {
            try {
                recorder.stop();
            } catch (RuntimeException ignored) { }
            recorder.release();
            recorder = null;

            if (recordTimeRunnable != null) {
                recordTimeHandler.removeCallbacks(recordTimeRunnable);
            }
        }

        if (speechApiThread != null) speechApiThread.shutdownNow();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1337) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                showError();
            }
        }
    }

    private void startRecording() {
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 1337);
            return;
        }

        sendBtn.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.twotone_send_24));
        errorIv.setVisibility(View.GONE);
        errorIv2.setVisibility(View.GONE);
        recordingIv.setVisibility(View.VISIBLE);
        recordingIv2.setVisibility(View.INVISIBLE);

        AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(1000);
        alphaAnimation.setRepeatCount(AlphaAnimation.INFINITE);
        alphaAnimation.setRepeatMode(AlphaAnimation.REVERSE);
        recordingIv.startAnimation(alphaAnimation);

        AlphaAnimation alphaAnimation2 = new AlphaAnimation(0f, 1f);
        alphaAnimation2.setDuration(1000);
        alphaAnimation2.setRepeatCount(AlphaAnimation.INFINITE);
        alphaAnimation2.setRepeatMode(AlphaAnimation.REVERSE);
        recordingIv2.startAnimation(alphaAnimation2);

        timeTv.setVisibility(View.VISIBLE);

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioEncodingBitRate(64000);
        recorder.setAudioSamplingRate(44100);
        recorder.setOutputFile(new File(getCacheDir(), "whisper_input_audio.mp3"));

        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            showError();
        }

        elapsedTime = 0;
        recordTimeHandler.post(recordTimeRunnable);
    }

    private void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (RuntimeException ignored) { }
            recorder.release();
            recorder = null;

            if (recordTimeRunnable != null) {
                recordTimeHandler.removeCallbacks(recordTimeRunnable);
            }

            startWhisperApiRequest();
        }
    }

    private void startWhisperApiRequest() {
        sendBtn.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        timeTv.setVisibility(View.GONE);
        recordingIv.setVisibility(View.GONE);
        recordingIv.clearAnimation();
        recordingIv2.setVisibility(View.GONE);
        recordingIv2.clearAnimation();
        errorIv.setVisibility(View.GONE);
        errorIv2.setVisibility(View.GONE);

        String apiKey = sp.getString("net.devemperor.wristassist.api_key", "noApiKey");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com/")
                .client(defaultClient(apiKey.replaceAll("[^ -~]", ""), Duration.ofSeconds(120)).newBuilder().build())
                .addConverterFactory(JacksonConverterFactory.create(defaultObjectMapper()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        OpenAiService service = new OpenAiService(retrofit.create(OpenAiApi.class));

        speechApiThread = Executors.newSingleThreadExecutor();
        speechApiThread.execute(() -> {
            try {
                CreateTranscriptionRequest request = CreateTranscriptionRequest.builder()
                        .model("whisper-1")
                        .responseFormat("verbose_json")
                        .build();
                TranscriptionResult result = service.createTranscription(request, new File(getCacheDir(), "whisper_input_audio.mp3"));

                // TODO: add usage to db

                Intent data = new Intent();
                data.putExtra("net.devemperor.wristassist.input.content", result.getText());
                setResult(RESULT_OK, data);
                finish();

            } catch (RuntimeException e) {
                if (!(e.getCause() instanceof InterruptedIOException)) {
                    FirebaseCrashlytics fc = FirebaseCrashlytics.getInstance();
                    fc.setCustomKey("settings", sp.getAll().toString());
                    fc.setUserId(sp.getString("net.devemperor.wristassist.userid", "null"));
                    fc.recordException(e);
                    fc.sendUnsentReports();

                    showError();
                }
            }
        });
    }

    private void showError() {
        runOnUiThread(() -> {
            if (sp.getBoolean("net.devemperor.wristassist.vibrate", true)) {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE))
                        .vibrate(VibrationEffect.createWaveform(new long[]{50, 50, 50, 50, 50}, new int[]{-1, 0, -1, 0, -1}, -1));
            }

            progressBar.setVisibility(View.GONE);
            timeTv.setVisibility(View.GONE);
            errorIv.setVisibility(View.VISIBLE);
            errorIv2.setVisibility(View.VISIBLE);
            recordingIv.setVisibility(View.GONE);
            recordingIv2.setVisibility(View.GONE);
            sendBtn.setEnabled(true);
            sendBtn.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.twotone_replay_24));
        });
    }
}