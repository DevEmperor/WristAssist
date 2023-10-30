package net.devemperor.wristassist.activities;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.text.TextUtils;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
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

        ListPreference model = findPreference("net.devemperor.wristassist.model");
        ListPreference apiHost = findPreference("net.devemperor.wristassist.api_host");
        if (model != null && apiHost != null) {
            apiHost.setOnPreferenceChangeListener((preference, newValue) -> {
                String newModel = model.getValue();
                if (newValue.equals("https://api.openai.com/")) {
                    model.setEntries(R.array.models_openai);
                    model.setEntryValues(R.array.models_openai_values);
                    newModel = "gpt-3.5-turbo";
                }
                if (newValue.equals("https://api.pawan.krd/")) {
                    model.setEntries(R.array.models_pawan);
                    model.setEntryValues(R.array.models_pawan_values);
                    newModel = "pai-001-light-beta";
                }
                getPreferenceManager().getSharedPreferences().edit().putString("net.devemperor.wristassist.model", newModel).apply();
                model.setValueIndex(0);
                return true;
            });
            apiHost.callChangeListener(apiHost.getValue());

            apiHost.setSummaryProvider(preference -> apiHost.getEntry());

            model.setSummaryProvider(preference -> model.getEntry());
        }

        new TextToSpeech(getContext(), status -> {
            if (status != TextToSpeech.SUCCESS) {
                findPreference("net.devemperor.wristassist.tts").setEnabled(false);
            }
        });
    }
}
