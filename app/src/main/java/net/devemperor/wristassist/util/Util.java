package net.devemperor.wristassist.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class Util {

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static float getFontMultiplier(Context context) {
        float fs = context.getResources().getConfiguration().fontScale;
        float diff = Math.abs(fs - 1.0f) * 0.3f;
        if (fs > 1) {
            return 1.0f + diff;
        } else {
            return 1.0f - diff;
        }
    }

    public static double calcCost(String model, long promptTokens, long completionTokens) {
        double inputPrice = 0;
        double outputPrice = 0;
        switch (model) {
            case "gpt-3.5-turbo":
                inputPrice = 0.0005;
                outputPrice = 0.0015;
                break;
            case "gpt-4-turbo-preview":
                inputPrice = 0.01;
                outputPrice = 0.03;
                break;
            case "gpt-4":
                inputPrice = 0.03;
                outputPrice = 0.06;
                break;
            case "gpt-4-32k":
                inputPrice = 0.06;
                outputPrice = 0.12;
                break;
        }
        return (inputPrice * promptTokens / 1000) + (outputPrice * completionTokens / 1000);
    }

    public static String translateModelNames(String origin) {
        switch (origin) {
            case "gpt-3.5-turbo":
                return "GPT-3.5 Turbo";
            case "gpt-4-turbo-preview":
                return "GPT-4 Turbo";
            case "gpt-4":
                return "GPT-4";
            case "gpt-4-32k":
                return "GPT-4 32K";
            default:
                return origin;
        }
    }
}
