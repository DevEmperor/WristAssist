package net.devemperor.wristassist.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import net.devemperor.wristassist.R;
import net.glxn.qrgen.android.QRCode;

public class QRCodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        String imageUrl = getIntent().getStringExtra("net.devemperor.wristassist.image_url");

        ImageView qrCodeIv = findViewById(R.id.qrcode_iv);

        Bitmap code = QRCode.from(imageUrl)
                .withSize(256, 256)
                .withColor(getColor(R.color.wristassist_purple), getColor(R.color.wristassist_black))
                .bitmap();

        code = Bitmap.createBitmap(code, 12, 12, code.getWidth() - 24, code.getHeight() - 24);
        qrCodeIv.setImageBitmap(code);
    }
}