package net.devemperor.wristassist.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import net.devemperor.wristassist.R;

import java.util.Objects;

public class InputActivity extends AppCompatActivity {

    ScrollView inputSv;
    TextView inputTitleTv;
    EditText inputContentEt;
    TextView inputTitle2Tv;
    EditText inputContent2Et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        inputSv = findViewById(R.id.activity_input_sv);

        String title = getIntent().getStringExtra("net.devemperor.wristassist.input.title");
        String content = getIntent().getStringExtra("net.devemperor.wristassist.input.content");
        String hint = getIntent().getStringExtra("net.devemperor.wristassist.input.hint");
        String title2 = getIntent().getStringExtra("net.devemperor.wristassist.input.title2");
        String content2 = getIntent().getStringExtra("net.devemperor.wristassist.input.content2");
        String hint2 = getIntent().getStringExtra("net.devemperor.wristassist.input.hint2");
        boolean handsFree = getIntent().getBooleanExtra("net.devemperor.wristassist.input.hands_free", false);
        inputTitleTv = findViewById(R.id.activity_input_title_tv);
        inputContentEt = findViewById(R.id.activity_input_content_et);
        inputTitle2Tv = findViewById(R.id.activity_input_title2_tv);
        inputContent2Et = findViewById(R.id.activity_input_content2_et);
        inputTitleTv.setText(title);
        inputContentEt.setText(content);
        inputContentEt.setHint(hint);

        if (title2 != null) {
            inputTitle2Tv.setText(title2);
            inputContent2Et.setText(content2);
            inputContent2Et.setHint(hint2);
            inputContent2Et.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    okay(null);
                    return true;
                }
                return false;
            });
        } else {
            inputTitle2Tv.setVisibility(View.GONE);
            inputContent2Et.setVisibility(View.GONE);
            inputContentEt.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    okay(null);
                    return true;
                }
                return false;
            });
        }

        inputSv.requestFocus();

        if (handsFree) {
            try {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                startActivityForResult(intent, 1337);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.wristassist_no_speech_recognition, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1337 && resultCode == RESULT_OK) {
            String result = Objects.requireNonNull(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)).get(0);
            Intent intent = new Intent();
            intent.putExtra("net.devemperor.wristassist.input.content", result);
            setResult(RESULT_OK, intent);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void cancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void okay(View view) {
        String content = inputContentEt.getText().toString();
        String content2 = inputContent2Et.getText().toString();

        if (inputTitle2Tv.getVisibility() == View.GONE && !content.isEmpty()) {
            Intent data = new Intent();
            data.putExtra("net.devemperor.wristassist.input.content", content);
            setResult(RESULT_OK, data);
            finish();
        } else if (!content.isEmpty() && !content2.isEmpty()) {
            Intent data = new Intent();
            data.putExtra("net.devemperor.wristassist.input.content", content);
            data.putExtra("net.devemperor.wristassist.input.content2", content2);
            setResult(RESULT_OK, data);
            finish();
        }
    }
}