package net.devemperor.wristassist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.database.ImageModel;
import net.devemperor.wristassist.database.ImagesDatabaseHelper;
import net.devemperor.wristassist.util.WristAssistUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class OpenImageActivity extends AppCompatActivity {

    int imageId;
    ImagesDatabaseHelper imagesDatabaseHelper;
    ImageModel imageModel;
    Timer timer = new Timer();

    ScrollView openImageSv;
    ImageView imageView;
    ImageButton shareBtn;
    TextView expiresInTv;
    TextView promptTv;
    TextView revisedPromptTv;
    TextView modelTv;
    TextView qualityTv;
    TextView sizeTv;
    TextView styleTv;
    TextView createdTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_image);

        imageId = getIntent().getIntExtra("net.devemperor.wristassist.imageId", -1);
        imagesDatabaseHelper = new ImagesDatabaseHelper(this);
        imageModel = imagesDatabaseHelper.get(imageId);

        openImageSv = findViewById(R.id.open_image_sv);
        imageView = findViewById(R.id.item_gallery_image_zv);
        shareBtn = findViewById(R.id.activity_create_image_share_btn);
        expiresInTv = findViewById(R.id.activity_create_image_expires_tv);
        promptTv = findViewById(R.id.open_image_prompt_tv);
        revisedPromptTv = findViewById(R.id.open_image_revised_prompt_tv);
        modelTv = findViewById(R.id.open_image_model_tv);
        qualityTv = findViewById(R.id.open_image_quality_tv);
        sizeTv = findViewById(R.id.open_image_size_tv);
        styleTv = findViewById(R.id.open_image_style_tv);
        createdTv = findViewById(R.id.open_image_created_tv);

        Picasso.get().load(new File(getFilesDir().getAbsolutePath() + "/image_" + imageModel.getId() + ".png")).into(imageView);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss", Locale.getDefault());
        promptTv.setText(imageModel.getPrompt());
        modelTv.setText(WristAssistUtil.translate(this, imageModel.getModel()));
        createdTv.setText(formatter.format(imageModel.getCreated()));
        sizeTv.setText(imageModel.getSize());

        if (imageModel.getRevisedPrompt() != null && imageModel.getQuality() != null && imageModel.getStyle() != null) {
            revisedPromptTv.setText(imageModel.getRevisedPrompt());
            qualityTv.setText(WristAssistUtil.translate(this, imageModel.getQuality()));
            styleTv.setText(WristAssistUtil.translate(this, imageModel.getStyle()));
        } else {
            findViewById(R.id.open_image_revised_prompt_descriptor_tv).setVisibility(TextView.GONE);
            revisedPromptTv.setVisibility(TextView.GONE);
            findViewById(R.id.open_image_quality_descriptor_tv).setVisibility(TextView.GONE);
            qualityTv.setVisibility(TextView.GONE);
            findViewById(R.id.open_image_style_descriptor_tv).setVisibility(TextView.GONE);
            styleTv.setVisibility(TextView.GONE);
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long minutes = (imageModel.getCreated() + 60*60*1000 - System.currentTimeMillis()) / 60 / 1000;
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

        openImageSv.requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    public void shareImage(View view) {
        Intent intent = new Intent(this, QRCodeActivity.class);
        intent.putExtra("net.devemperor.wristassist.image_url", imageModel.getUrl());
        startActivity(intent);
    }

    public void deleteImage(View view) {
        timer.cancel();
        imagesDatabaseHelper.delete(imageId);
        Intent data = new Intent();
        data.putExtra("net.devemperor.wristassist.input.image_deleted", true);
        setResult(RESULT_OK, data);
        finish();
    }
}