package net.devemperor.wristassist.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import net.devemperor.wristassist.R;

public class WristAssistUtil {

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

    public static double calcCostChat(String model, long promptTokens, long completionTokens) {
        double inputPrice = 0;
        double outputPrice = 0;
        switch (model) {
            case "gpt-4o-mini":
                inputPrice = 0.00015;
                outputPrice = 0.0006;
                break;
            case "gpt-4o":
                inputPrice = 0.0025;
                outputPrice = 0.01;
                break;
            case "gpt-4-turbo":
                inputPrice = 0.01;
                outputPrice = 0.03;
                break;
            case "gpt-4":
                inputPrice = 0.03;
                outputPrice = 0.06;
                break;
            case "gpt-3.5-turbo":
                inputPrice = 0.0005;
                outputPrice = 0.0015;
                break;
        }
        return (inputPrice * promptTokens / 1000) + (outputPrice * completionTokens / 1000);
    }

    public static double calcCostImage(String model, String quality, String size) {
        switch (model) {
            case "dall-e-3":
                switch (quality) {
                    case "hd":
                        return 0.08;
                    case "standard":
                        return 0.04;
                }
            case "dall-e-2":
                switch (size) {
                    case "1024x1024":
                        return 0.02;
                    case "512x512":
                        return 0.018;
                    case "256x256":
                        return 0.016;
                }
        }
        return 0;
    }

    public static String translate(Context context, String origin) {
        switch (origin) {
            case "gpt-3.5-turbo":
                return "GPT-3.5 Turbo";
            case "gpt-4o-mini":
                return "GPT-4 Omni Mini";
            case "gpt-4-turbo-preview":
                return "GPT-4 Turbo";
            case "gpt-4":
                return "GPT-4";
            case "gpt-4-32k":
                return "GPT-4 32K";
            case "gpt-4o":
                return "GPT-4 Omni";
            case "dall-e-3":
                return "DALL-E 3";
            case "dall-e-2":
                return "DALL-E 2";
            case "hd":
                return "HD";
            case "standard":
                return "Standard";
            case "natural":
                return context.getString(R.string.wristassist_image_quality_natural);
            case "vivid":
                return context.getString(R.string.wristassist_image_quality_vivid);
            default:
                return origin;
        }
    }
}
