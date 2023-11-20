package net.devemperor.wristassist.complication;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Icon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.wear.watchface.complications.data.ComplicationData;
import androidx.wear.watchface.complications.data.ComplicationType;
import androidx.wear.watchface.complications.data.LongTextComplicationData;
import androidx.wear.watchface.complications.data.MonochromaticImage;
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData;
import androidx.wear.watchface.complications.data.PlainComplicationText;
import androidx.wear.watchface.complications.data.ShortTextComplicationData;
import androidx.wear.watchface.complications.data.SmallImage;
import androidx.wear.watchface.complications.data.SmallImageComplicationData;
import androidx.wear.watchface.complications.data.SmallImageType;
import androidx.wear.watchface.complications.datasource.ComplicationRequest;
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.activities.MainActivity;

import kotlin.coroutines.Continuation;

public class ComplicationDataSourceService extends SuspendingComplicationDataSourceService {

    @Nullable
    @Override
    public ComplicationData onComplicationRequest(@NonNull ComplicationRequest request, @NonNull Continuation<? super ComplicationData> continuation) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("net.devemperor.wristassist.complication", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        PlainComplicationText description = new PlainComplicationText.Builder(getString(R.string.wristassist_complication_description)).build();
        switch (request.getComplicationType()) {
            case SHORT_TEXT:
                return new ShortTextComplicationData.Builder(
                    new PlainComplicationText.Builder(
                            getString(R.string.wristassist_ai)
                    ).build(), description)
                    .setTapAction(pendingIntent)
                    .build();
            case LONG_TEXT:
                return new LongTextComplicationData.Builder(
                        new PlainComplicationText.Builder(
                                getString(R.string.app_name)
                        ).build(), description)
                        .setTapAction(pendingIntent)
                        .build();
            case SMALL_IMAGE:
                return new SmallImageComplicationData.Builder(
                        new SmallImage.Builder(
                                Icon.createWithResource(this, R.drawable.wristassist_logo),
                                SmallImageType.ICON
                        ).build(), description)
                        .setTapAction(pendingIntent)
                        .build();
            case MONOCHROMATIC_IMAGE:
                return new MonochromaticImageComplicationData.Builder(
                        new MonochromaticImage.Builder(
                                Icon.createWithResource(this, R.drawable.wristassist_logo)
                        ).build(), description)
                        .setTapAction(pendingIntent)
                        .build();
        }
        return null;
    }

    @Nullable
    @Override
    public ComplicationData getPreviewData(@NonNull ComplicationType complicationType) {
        return new ShortTextComplicationData.Builder(
                new PlainComplicationText.Builder(getString(R.string.wristassist_ai)).build(),
                new PlainComplicationText.Builder(getString(R.string.wristassist_complication_description)).build()
        ).build();
    }
}
