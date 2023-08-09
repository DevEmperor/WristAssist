package net.devemperor.wristassist.activities;

import android.app.Activity;
import android.os.Bundle;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import net.devemperor.wristassist.R;
import net.devemperor.wristassist.adapters.OnboardingAdapter;

public class OnboardingActivity extends Activity {

    ViewPager2 onboardingVp;
    TabLayout onboardingTl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        onboardingVp = findViewById(R.id.onboarding_viewpager);
        onboardingTl = findViewById(R.id.onboarding_tablayout);
        onboardingVp.setAdapter(new OnboardingAdapter(this, new int[]{
                R.layout.viewpager_welcome,
                R.layout.viewpager_info,
                R.layout.viewpager_qrcode
        }));
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(onboardingTl, onboardingVp, true,
                (tab, position) -> { }
        );
        tabLayoutMediator.attach();
    }
}