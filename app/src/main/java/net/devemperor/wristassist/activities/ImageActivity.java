package net.devemperor.wristassist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

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

import java.util.List;

public class ImageActivity extends AppCompatActivity {

    ImagesDatabaseHelper imagesDatabaseHelper;

    WearableRecyclerView galleryWrv;
    List<ImageModel> imageData;
    ImageAdapter imageAdapter;

    int currentOpenPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imagesDatabaseHelper = new ImagesDatabaseHelper(this);

        galleryWrv = findViewById(R.id.gallery_wrv);
        galleryWrv.setEdgeItemsCenteringEnabled(true);
        galleryWrv.setHasFixedSize(true);
        galleryWrv.setLayoutManager(new GridLayoutManager(this, 3));
        imageData = imagesDatabaseHelper.getAll();
        imageData.add(0, null);

        imageAdapter = new ImageAdapter(imageData, (menuPosition, image) -> {
            if (menuPosition == 0) {
                Intent intent = new Intent(this, InputActivity.class);
                intent.putExtra("net.devemperor.wristassist.input.title", getString(R.string.wristassist_describe_image));
                intent.putExtra("net.devemperor.wristassist.input.hint", getString(R.string.wristassist_image_hint));
                intent.putExtra("net.devemperor.wristassist.input.hands_free", getSharedPreferences("net.devemperor.wristassist", MODE_PRIVATE)
                        .getBoolean("net.devemperor.wristassist.hands_free", false));
                startActivityForResult(intent, 1337);
            } else {
                currentOpenPosition = menuPosition;
                Intent intent = new Intent(this, OpenImageActivity.class);
                intent.putExtra("net.devemperor.wristassist.imageId", imageData.get(menuPosition).getId());
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, image, "image");
                startActivityForResult(intent, 1338, options.toBundle());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        if (requestCode == 1337) {
            Intent intent = new Intent(this, CreateImageActivity.class);
            intent.putExtra("net.devemperor.wristassist.prompt", data.getStringExtra("net.devemperor.wristassist.input.content"));
            startActivityForResult(intent, 1339);
        }
        if (requestCode == 1338 && data.getBooleanExtra("net.devemperor.wristassist.input.image_deleted", false)) {
            imageAdapter.getData().remove(currentOpenPosition);
            imageAdapter.notifyItemRemoved(currentOpenPosition);
        }
        if (requestCode == 1339) {
            int imageId = data.getIntExtra("net.devemperor.wristassist.imageId", -1);
            if (imageId != -1) {
                imageData.add(1, imagesDatabaseHelper.get(imageId));
                imageAdapter.notifyItemInserted(1);
            }
        }
    }
}