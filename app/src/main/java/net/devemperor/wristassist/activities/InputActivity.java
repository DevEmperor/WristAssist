package net.devemperor.wristassist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import net.devemperor.wristassist.R;

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

        inputSv = findViewById(R.id.input_sv);

        String title = getIntent().getStringExtra("net.devemperor.wristassist.input.title");
        String content = getIntent().getStringExtra("net.devemperor.wristassist.input.content");
        String hint = getIntent().getStringExtra("net.devemperor.wristassist.input.hint");
        String title2 = getIntent().getStringExtra("net.devemperor.wristassist.input.title2");
        String content2 = getIntent().getStringExtra("net.devemperor.wristassist.input.content2");
        String hint2 = getIntent().getStringExtra("net.devemperor.wristassist.input.hint2");
        inputTitleTv = findViewById(R.id.input_title_tv);
        inputContentEt = findViewById(R.id.input_content_et);
        inputTitle2Tv = findViewById(R.id.input_title2_tv);
        inputContent2Et = findViewById(R.id.input_content2_et);
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