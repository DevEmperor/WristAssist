package net.devemperor.wristassist.util;

import android.content.Context;
import android.content.Intent;

import net.devemperor.wristassist.activities.InputTypeActivity;
import net.devemperor.wristassist.activities.InputWhisperActivity;

public class InputIntentBuilder {

    private final Context context;
    private String title;
    private String content;
    private String hint;
    private String title2;
    private String content2;
    private String hint2;
    private boolean handsFree;

    public InputIntentBuilder(Context context) {
        this.context = context;
    }

    public InputIntentBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public InputIntentBuilder setContent(String content) {
        this.content = content;
        return this;
    }

    public InputIntentBuilder setHint(String hint) {
        this.hint = hint;
        return this;
    }

    public InputIntentBuilder setTitle2(String title2) {
        this.title2 = title2;
        return this;
    }

    public InputIntentBuilder setContent2(String content2) {
        this.content2 = content2;
        return this;
    }

    public InputIntentBuilder setHint2(String hint2) {
        this.hint2 = hint2;
        return this;
    }

    public InputIntentBuilder setHandsFree(boolean handsFree) {
        this.handsFree = handsFree;
        return this;
    }

    public Intent build() {
        Intent intent;
        if (handsFree) {
            intent = new Intent(context, InputWhisperActivity.class);
        } else {
            intent = new Intent(context, InputTypeActivity.class);
        }
        intent.putExtra("net.devemperor.wristassist.input.title", title);
        intent.putExtra("net.devemperor.wristassist.input.content", content);
        intent.putExtra("net.devemperor.wristassist.input.hint", hint);
        intent.putExtra("net.devemperor.wristassist.input.title2", title2);
        intent.putExtra("net.devemperor.wristassist.input.content2", content2);
        intent.putExtra("net.devemperor.wristassist.input.hint2", hint2);
        return intent;
    }
}
