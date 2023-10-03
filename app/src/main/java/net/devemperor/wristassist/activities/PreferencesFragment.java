package net.devemperor.wristassist.activities;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.text.TextUtils;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import net.devemperor.wristassist.R;

public class PreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName("net.devemperor.wristassist");
        setPreferencesFromResource(R.xml.fragment_preferences, null);

        EditTextPreference apiKeyPreference = findPreference("net.devemperor.wristassist.api_key");
        if (apiKeyPreference != null) {
            apiKeyPreference.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
                String key = preference.getText();
                if (TextUtils.isEmpty(key)) {
                    return getString(R.string.wristassist_no_api_key);
                }
                if (key.length() <= 10) {
                    return key;
                }
                return key.substring(0, 10) + "...";
            });

            apiKeyPreference.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                editText.setSingleLine(true);
            });
        }

        new TextToSpeech(getContext(), status -> {
            if (status != TextToSpeech.SUCCESS) {
                findPreference("net.devemperor.wristassist.tts").setEnabled(false);
            }
        });
    }
}
