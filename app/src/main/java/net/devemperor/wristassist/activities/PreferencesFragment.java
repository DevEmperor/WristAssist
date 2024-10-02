package net.devemperor.wristassist.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.util.InputIntentBuilder;

import org.apache.commons.validator.routines.UrlValidator;

public class PreferencesFragment extends PreferenceFragmentCompat {

    SharedPreferences sp;
    SwitchPreference customServerPreference;
    ListPreference chatModelPreference;

    ActivityResultLauncher<Intent> customServerInputLauncher;
    ActivityResultLauncher<Intent> globalSystemPromptLauncher;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName("net.devemperor.wristassist");
        setPreferencesFromResource(R.xml.fragment_preferences, null);
        sp = getPreferenceManager().getSharedPreferences();

        customServerInputLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                String host = result.getData().getStringExtra("net.devemperor.wristassist.input.content");
                String model = result.getData().getStringExtra("net.devemperor.wristassist.input.content2");
                if (host != null && model != null) {
                    if (new UrlValidator().isValid(host)) {
                        sp.edit().putString("net.devemperor.wristassist.custom_server_host", host).apply();
                        sp.edit().putString("net.devemperor.wristassist.custom_server_model", model).apply();
                        chatModelPreference.setEnabled(false);
                    } else {
                        customServerPreference.setChecked(false);
                        chatModelPreference.setEnabled(true);
                        Toast.makeText(getContext(), R.string.wristassist_invalid_host, Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                customServerPreference.setChecked(false);
                chatModelPreference.setEnabled(true);
            }
        });

        globalSystemPromptLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                String systemQuery = result.getData().getStringExtra("net.devemperor.wristassist.input.content");
                sp.edit().putString("net.devemperor.wristassist.global_system_query", systemQuery).apply();
            }
        });

        EditTextPreference apiKeyPreference = findPreference("net.devemperor.wristassist.api_key");
        if (apiKeyPreference != null) {
            apiKeyPreference.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
                String key = preference.getText();
                if (TextUtils.isEmpty(key)) return getString(R.string.wristassist_no_api_key);
                if (key.length() <= 10) return key;
                return key.substring(0, 10) + "...";
            });

            apiKeyPreference.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                editText.setSingleLine(true);
            });
        }

        customServerPreference = findPreference("net.devemperor.wristassist.custom_server");
        if (customServerPreference != null) {
            customServerPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!((Boolean) newValue)) {
                    sp.edit().remove("net.devemperor.wristassist.custom_server_host").apply();
                    sp.edit().remove("net.devemperor.wristassist.custom_server_model").apply();
                    chatModelPreference.setEnabled(true);
                } else {
                    Intent intent = new InputIntentBuilder(getContext())
                        .setTitle(getString(R.string.wristassist_custom_host))
                        .setHint(getString(R.string.wristassist_custom_host_hint))
                        .setTitle2(getString(R.string.wristassist_custom_model))
                        .setHint2(getString(R.string.wristassist_custom_model_hint))
                        .build();
                    customServerInputLauncher.launch(intent);
                }
                return true;
            });
        }

        chatModelPreference = findPreference("net.devemperor.wristassist.model");
        if (customServerPreference.isChecked()) chatModelPreference.setEnabled(false);

        Preference globalSystemQueryPreference = findPreference("net.devemperor.wristassist.global_system_query");
        if (globalSystemQueryPreference != null) {
            globalSystemQueryPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new InputIntentBuilder(getContext())
                    .setTitle(getString(R.string.wristassist_define_global_system_prompt))
                    .setHint(getString(R.string.wristassist_system_prompt))
                    .setContent(sp.getString("net.devemperor.wristassist.global_system_query", ""))
                    .build();
                globalSystemPromptLauncher.launch(intent);
                return true;
            });
        }

        ListPreference ttsPreference = findPreference("net.devemperor.wristassist.tts");
        if (ttsPreference != null) {
            new TextToSpeech(getContext(), status -> {
                if (status != TextToSpeech.SUCCESS) {
                    ttsPreference.setEnabled(false);
                }
            });
        }

        SwitchPreference imageModelPreference = findPreference("net.devemperor.wristassist.image_model");
        SwitchPreference imageQualityPreference = findPreference("net.devemperor.wristassist.image_quality");
        SwitchPreference imageStylePreference = findPreference("net.devemperor.wristassist.image_style");
        ListPreference imageSizePreference = findPreference("net.devemperor.wristassist.image_size");
        if (imageModelPreference != null && imageQualityPreference != null && imageStylePreference != null && imageSizePreference != null) {
            imageModelPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue) {
                    imageModelPreference.setSummaryProvider(preference1 -> "DALL-E 3");
                    imageQualityPreference.setEnabled(true);
                    imageStylePreference.setEnabled(true);
                    imageSizePreference.setEnabled(false);
                } else {
                    imageModelPreference.setSummaryProvider(preference1 -> "DALL-E 2");
                    imageQualityPreference.setEnabled(false);
                    imageStylePreference.setEnabled(false);
                    imageSizePreference.setEnabled(true);
                }
                return true;
            });

            imageQualityPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue) imageQualityPreference.setSummaryProvider(preference1 -> "HD");
                else imageQualityPreference.setSummaryProvider(preference1 -> "Standard");
                return true;
            });

            imageStylePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue) imageStylePreference.setSummaryProvider(preference1 -> getString(R.string.wristassist_image_quality_natural));
                else imageStylePreference.setSummaryProvider(preference1 -> getString(R.string.wristassist_image_quality_vivid));
                return true;
            });

            if (imageModelPreference.isChecked()) {
                imageModelPreference.setSummaryProvider(preference -> "DALL-E 3");
                imageQualityPreference.setEnabled(true);
                imageStylePreference.setEnabled(true);
                imageSizePreference.setEnabled(false);
            }
            if (imageQualityPreference.isChecked()) imageQualityPreference.setSummaryProvider(preference -> "HD");
            if (imageStylePreference.isChecked()) imageStylePreference.setSummaryProvider(preference -> getString(R.string.wristassist_image_quality_natural));
            imageSizePreference.setSummaryProvider(preference -> imageSizePreference.getEntry());
        }
    }
}
