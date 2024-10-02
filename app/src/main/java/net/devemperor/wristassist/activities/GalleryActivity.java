package net.devemperor.wristassist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.InputDeviceCompat;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.adapters.ImageAdapter;
import net.devemperor.wristassist.database.ImageModel;
import net.devemperor.wristassist.database.ImagesDatabaseHelper;
import net.devemperor.wristassist.util.InputIntentBuilder;

import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    ImagesDatabaseHelper imagesDatabaseHelper;

    WearableRecyclerView galleryWrv;
    List<ImageModel> imageData;
    ImageAdapter imageAdapter;

    int currentOpenPosition = -1;
    ActivityResultLauncher<Intent> inputLauncher;
    ActivityResultLauncher<Intent> createImageLauncher;
    ActivityResultLauncher<Intent> openImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imagesDatabaseHelper = new ImagesDatabaseHelper(this);

        galleryWrv = findViewById(R.id.activity_image_gallery_wrv);
        galleryWrv.setEdgeItemsCenteringEnabled(true);
        galleryWrv.setHasFixedSize(true);
        galleryWrv.setLayoutManager(new GridLayoutManager(this, 3));
        imageData = imagesDatabaseHelper.getAll();
        imageData.add(0, null);

        createImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                int imageId = result.getData().getIntExtra("net.devemperor.wristassist.imageId", -1);
                if (imageId != -1) {
                    imageData.add(1, imagesDatabaseHelper.get(imageId));
                    imageAdapter.notifyItemInserted(1);
                }
            }
        });
        inputLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Intent createImageIntent = new Intent(this, CreateImageActivity.class);
                createImageIntent.putExtra("net.devemperor.wristassist.prompt", result.getData().getStringExtra("net.devemperor.wristassist.input.content"));

                createImageLauncher.launch(createImageIntent);
            }
        });
        openImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                if (result.getData().getBooleanExtra("net.devemperor.wristassist.input.image_deleted", false)) {
                    imageAdapter.getData().remove(currentOpenPosition);
                    imageAdapter.notifyItemRemoved(currentOpenPosition);
                }
            }
        });

        imageAdapter = new ImageAdapter(imageData, (menuPosition, image) -> {
            if (menuPosition == 0) {
                Intent intent = new InputIntentBuilder(this)
                    .setTitle(getString(R.string.wristassist_describe_image))
                    .setHint(getString(R.string.wristassist_image_hint))
                    .setHandsFree(getSharedPreferences("net.devemperor.wristassist", MODE_PRIVATE).getBoolean("net.devemperor.wristassist.hands_free", false))
                    .build();
                inputLauncher.launch(intent);
            } else {
                currentOpenPosition = menuPosition;
                Intent intent = new Intent(this, OpenImageActivity.class);
                intent.putExtra("net.devemperor.wristassist.imageId", imageData.get(menuPosition).getId());
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, image, "image");

                openImageLauncher.launch(intent, options);
            }
        });
        galleryWrv.setAdapter(imageAdapter);

        galleryWrv.requestFocus();
        galleryWrv.setOnGenericMotionListener((v, ev) -> {
            if (ev.getAction() == MotionEvent.ACTION_SCROLL && ev.isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER)) {
                v.scrollBy(0, (int) (galleryWrv.getChildAt(0).getHeight() * -ev.getAxisValue(MotionEventCompat.AXIS_SCROLL)));
                return true;
            }
            return false;
        });
    }
}