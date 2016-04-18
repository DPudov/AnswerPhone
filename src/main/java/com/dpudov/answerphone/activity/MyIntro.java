package com.dpudov.answerphone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dpudov.answerphone.R;
import com.dpudov.answerphone.fragments.IntroSlide;
import com.github.paolorotolo.appintro.AppIntro2;

/**
 * Created by DPudov on 17.04.2016.
 */
public class MyIntro extends AppIntro2 {
    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        addSlide(IntroSlide.newInstance(R.layout.intro_welcome));
        addSlide(IntroSlide.newInstance(R.layout.intro_answerphone));
        addSlide(IntroSlide.newInstance(R.layout.intro_messaging));
        addSlide(IntroSlide.newInstance(R.layout.intro_get_started));
    }

    private void loadMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDonePressed() {
        loadMainActivity();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onSlideChanged() {

    }
}
